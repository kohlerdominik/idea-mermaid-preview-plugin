package com.github.kohlerdominik.ideamermaidpreview.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.plugins.textmate.TextMateService

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
        LOG.info("Creating Mermaid editor for: ${file.path}")
        logFileTypeDetails(file)
        logTextMateDescriptor(file)
        // Check if JCEF is supported
        if (!MermaidPreviewEditor.isJcefSupported()) {
            // Fall back to text editor only if JCEF is not supported
            return TextEditorProvider.getInstance().createEditor(project, file)
        }
        
        // Create text editor
        val textEditor = TextEditorProvider.getInstance().createEditor(project, file) as TextEditor
        
        // Create preview editor
        val previewEditor = MermaidPreviewEditor(file)
        
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

    private companion object {
        val LOG: Logger = Logger.getInstance(MermaidSplitEditorProvider::class.java)
    }

    private fun logTextMateDescriptor(file: VirtualFile) {
        try {
            val descriptor = TextMateService.getInstance().getLanguageDescriptorByFileName(file.name)
            val hasDescriptor = descriptor != null
            val extDescriptor = TextMateService.getInstance().getLanguageDescriptorByExtension(file.extension)
            val hasExtDescriptor = extDescriptor != null
            val scopeByName = descriptor?.let { readRootScopeName(it) } ?: "<none>"
            val scopeByExt = extDescriptor?.let { readRootScopeName(it) } ?: "<none>"
            val doc = FileDocumentManager.getInstance().getDocument(file)
            val firstLine = doc?.getLineStartOffset(0)?.let { start ->
                val end = doc.getLineEndOffset(0)
                doc.getText(com.intellij.openapi.util.TextRange(start, end)).trim()
            } ?: ""
            val hasClassDiagram = firstLine.contains("classDiagram")
            LOG.info(
                "TextMate descriptor: present=$hasDescriptor, byExtension=$hasExtDescriptor, " +
                    "scopeByName=$scopeByName, scopeByExt=$scopeByExt, hasClassDiagram=$hasClassDiagram"
            )
        } catch (e: Exception) {
            LOG.warn("TextMate descriptor log failed", e)
        }
    }

    private fun readRootScopeName(descriptor: Any): String {
        return try {
            val method = descriptor.javaClass.methods.firstOrNull { it.name == "getRootScopeName" }
            val value = method?.invoke(descriptor)
            value?.toString() ?: "<none>"
        } catch (_: Exception) {
            try {
                val field = descriptor.javaClass.getDeclaredField("rootScopeName")
                field.isAccessible = true
                field.get(descriptor)?.toString() ?: "<none>"
            } catch (_: Exception) {
                "<unknown>"
            }
        }
    }

    private fun logFileTypeDetails(file: VirtualFile) {
        try {
            val fileType = file.fileType
            val languageId = (fileType as? LanguageFileType)?.language?.id ?: "<none>"
            LOG.info(
                "Mermaid file type: name=${fileType.name}, class=${fileType.javaClass.name}, " +
                    "ext=${file.extension}, language=$languageId"
            )
        } catch (e: Exception) {
            LOG.warn("Mermaid file type log failed", e)
        }
    }
}
