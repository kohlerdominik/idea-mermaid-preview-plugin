package com.github.kohlerdominik.ideamermaidpreview.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.util.Alarm
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.ui.ColorUtil
import com.intellij.util.ui.UIUtil
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandlerAdapter
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Panel that displays a live preview of Mermaid diagrams using JBCefBrowser.
 */
class MermaidPreviewPanel(
) : Disposable {

    private var browser: JBCefBrowser? = null
    private val panel = JPanel(BorderLayout())
    private val updateAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private var textEditor: TextEditor? = null
    private val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            scheduleUpdate(event.document)
        }
    }
    
    @Volatile
    private var isPageLoaded = false
    
    val component: JComponent
        get() = panel
    
    init {
        scheduleBrowserInit()
    }
    
    /**
     * Attach this preview to a text editor to enable reactive updates.
     */
    fun attachToEditor(editor: TextEditor) {
        textEditor?.editor?.document?.removeDocumentListener(documentListener)
        textEditor = editor
        editor.editor.document.addDocumentListener(documentListener, this)

        scheduleBrowserInit()
        
        // Initial render if page is already loaded
        if (isPageLoaded) {
            renderCurrentContent(editor.editor.document)
        }
    }
    
    /**
     * Schedule a throttled update to the preview.
     */
    private fun scheduleUpdate(document: Document) {
        updateAlarm.cancelAllRequests()
        updateAlarm.addRequest({
            renderCurrentContent(document)
        }, UPDATE_DELAY_MS)
    }

    private fun scheduleBrowserInit() {
        if (browser != null) return

        ApplicationManager.getApplication().invokeLater({
            if (Disposer.isDisposed(this) || browser != null) return@invokeLater
            if (!JBCefApp.isSupported()) return@invokeLater

            val createdBrowser = JBCefBrowser()
            browser = createdBrowser
            panel.add(createdBrowser.component, BorderLayout.CENTER)
            panel.revalidate()
            panel.repaint()

            registerLoadHandler(createdBrowser)
            loadHtmlContent(createdBrowser)
        }, ModalityState.any())
    }

    private fun registerLoadHandler(cefBrowser: JBCefBrowser) {
        cefBrowser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (frame?.isMain == true) {
                    isPageLoaded = true
                    textEditor?.let { renderCurrentContent(it.editor.document) }
                }
            }
        }, cefBrowser.cefBrowser)
    }
    
    /**
     * Render the current document content in the browser.
     */
    private fun renderCurrentContent(document: Document) {
        val activeBrowser = browser ?: run {
            scheduleBrowserInit()
            return
        }
        if (!isPageLoaded) return

        val escapedContent = document.text
            .replace("\\", "\\\\")
            .replace("`", "\\`")
            .replace("$", "\\$")
            .replace("\n", "\\n")
        
        val theme = getTheme()
        val script = "window.renderMermaid(`$escapedContent`, '$theme');"
        
        UIUtil.invokeLaterIfNeeded {
            activeBrowser.cefBrowser.executeJavaScript(script, activeBrowser.cefBrowser.url, 0)
        }
    }
    
    /**
     * Get the current IDE theme for Mermaid.
     */
    private fun getTheme(): String {
        val background = EditorColorsManager.getInstance().globalScheme.defaultBackground
        return if (ColorUtil.isDark(background)) "dark" else "default"
    }
    
    /**
     * Load the HTML content with Mermaid.js.
     */
    private fun loadHtmlContent(cefBrowser: JBCefBrowser) {
        val resource = javaClass.getResource("/mermaid/index.html")
        if (resource != null) {
            // Use the resource URL as the base URL so that relative paths (like mermaid.min.js) resolve correctly
            cefBrowser.loadHTML(resource.readText(), resource.toExternalForm())
        } else {
            cefBrowser.loadHTML(createDefaultHtml())
        }
    }
    
    /**
     * Create a default HTML template if the resource file is not found.
     */
    private fun createDefaultHtml(): String {
        thisLogger().warn("Mermaid HTML resource not found, using default template")
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <script src="https://cdn.jsdelivr.net/npm/mermaid@11/dist/mermaid.min.js"></script>
                <style>
                    body { margin: 0; padding: 20px; overflow: auto; }
                    #diagram { min-height: 100vh; }
                    .error { color: red; padding: 20px; }
                </style>
            </head>
            <body>
                <div id="diagram"></div>
                <script>
                    mermaid.initialize({ startOnLoad: false, theme: 'default' });
                    
                    window.renderMermaid = function(code, theme) {
                        mermaid.initialize({ startOnLoad: false, theme: theme || 'default' });
                        const diagram = document.getElementById('diagram');
                        
                        if (!code || code.trim() === '') {
                            diagram.innerHTML = '<div class="error">No diagram content</div>';
                            return;
                        }
                        
                        try {
                            mermaid.render('preview', code).then(result => {
                                diagram.innerHTML = result.svg;
                            }).catch(err => {
                                diagram.innerHTML = '<div class="error">Syntax error: ' + err.message + '</div>';
                            });
                        } catch (err) {
                            diagram.innerHTML = '<div class="error">Error: ' + err.message + '</div>';
                        }
                    };
                </script>
            </body>
            </html>
        """.trimIndent()
    }
    
    override fun dispose() {
        updateAlarm.cancelAllRequests()
        browser?.let { Disposer.dispose(it) }
    }

    private companion object {
        const val UPDATE_DELAY_MS = 300
    }
}
