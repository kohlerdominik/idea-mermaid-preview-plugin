package com.github.kohlerdominik.ideamermaidpreview.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefApp
import java.beans.PropertyChangeListener
import javax.swing.JComponent

/**
 * Preview editor for Mermaid diagrams using JBCefBrowser.
 * This editor loads the Mermaid rendering HTML and updates it reactively.
 */
class MermaidPreviewEditor(
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor {
    
    private val preview = MermaidPreviewPanel(project, file)
    
    override fun getComponent(): JComponent = preview.component
    
    override fun getPreferredFocusedComponent(): JComponent = preview.component
    
    override fun getName(): String = "Mermaid Preview"
    
    override fun setState(state: FileEditorState) {}
    
    override fun isModified(): Boolean = false
    
    override fun isValid(): Boolean = true
    
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    
    override fun getFile(): VirtualFile = file
    
    override fun dispose() {
        Disposer.dispose(preview)
    }
    
    /**
     * Attach the preview to a text editor to enable reactive updates.
     */
    fun attachToEditor(textEditor: TextEditor) {
        preview.attachToEditor(textEditor)
    }
    
    companion object {
        /**
         * Check if JCEF is supported on this platform.
         */
        fun isJcefSupported(): Boolean = JBCefApp.isSupported()
    }
}
