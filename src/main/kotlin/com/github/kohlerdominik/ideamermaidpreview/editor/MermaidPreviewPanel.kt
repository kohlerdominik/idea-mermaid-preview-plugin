package com.github.kohlerdominik.ideamermaidpreview.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import com.intellij.util.Alarm
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
    private val project: Project,
    private val file: VirtualFile
) : Disposable {
    
    private val browser = JBCefBrowser()
    private val panel = JPanel(BorderLayout())
    private val updateAlarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)
    private var textEditor: TextEditor? = null
    private var documentListener: DocumentListener? = null
    
    @Volatile
    private var isPageLoaded = false
    
    val component: JComponent
        get() = panel
    
    init {
        panel.add(browser.component, BorderLayout.CENTER)
        
        // Add load handler to track when page is ready
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(browser: CefBrowser?, frame: CefFrame?, httpStatusCode: Int) {
                if (frame?.isMain == true) {
                    isPageLoaded = true
                    // Initial render after page loads
                    textEditor?.let { renderCurrentContent(it.editor.document) }
                }
            }
        }, browser.cefBrowser)
        
        loadHtmlContent()
    }
    
    /**
     * Attach this preview to a text editor to enable reactive updates.
     */
    fun attachToEditor(editor: TextEditor) {
        textEditor = editor
        
        // Remove old listener if exists
        documentListener?.let { editor.editor.document.removeDocumentListener(it) }
        
        // Create and add new document listener
        val listener = object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                scheduleUpdate(editor.editor.document)
            }
        }
        documentListener = listener
        editor.editor.document.addDocumentListener(listener, this)
        
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
        }, 300)
    }
    
    /**
     * Render the current document content in the browser.
     */
    private fun renderCurrentContent(document: Document) {
        if (!isPageLoaded) return
        
        val content = document.text
        val escapedContent = content
            .replace("\\", "\\\\")
            .replace("`", "\\`")
            .replace("$", "\\$")
            .replace("\n", "\\n")
        
        val theme = getTheme()
        val script = "window.renderMermaid(`$escapedContent`, '$theme');"
        
        UIUtil.invokeLaterIfNeeded {
            browser.cefBrowser.executeJavaScript(script, browser.cefBrowser.url, 0)
        }
    }
    
    /**
     * Get the current IDE theme for Mermaid.
     */
    private fun getTheme(): String {
        return if (UIUtil.isUnderDarcula()) "dark" else "default"
    }
    
    /**
     * Load the HTML content with Mermaid.js.
     */
    private fun loadHtmlContent() {
        val htmlContent = javaClass.getResource("/mermaid/index.html")?.readText() 
            ?: createDefaultHtml()
        
        browser.loadHTML(htmlContent)
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
        Disposer.dispose(browser)
    }
}
