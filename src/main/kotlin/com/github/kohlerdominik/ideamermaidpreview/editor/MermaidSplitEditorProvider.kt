package com.github.kohlerdominik.ideamermaidpreview.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * File editor provider for Mermaid files.
 * Creates a split-view editor with text on the left and preview on the right.
 */
class MermaidSplitEditorProvider : FileEditorProvider, DumbAware {

    private val supportedExtensions = setOf("mmd", "mermaid")
    
    override fun accept(project: Project, file: VirtualFile): Boolean {
        val extension = file.extension?.lowercase()
        return extension in supportedExtensions
    }
    
    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        // Check if JCEF is supported
        if (!MermaidPreviewEditor.isJcefSupported()) {
            // Fall back to text editor only if JCEF is not supported
            return TextEditorProvider.getInstance().createEditor(project, file)
        }
        
        // Create text editor
        val textEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor
        
        // Create preview editor
        val previewEditor = MermaidPreviewEditor(project, file)
        
        // Attach preview to text editor for reactive updates
        previewEditor.attachToEditor(textEditor)
        
        // Create split editor
        return TextEditorWithPreview(
            textEditor,
            previewEditor,
            "Mermaid Editor"
        )
    }
    
    override fun getEditorTypeId(): String = "mermaid-split-editor"
    
    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}
