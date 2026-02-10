package com.github.kohlerdominik.ideamermaidpreview.file

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.PlainTextLanguage
import javax.swing.Icon

/**
 * File type for Mermaid diagram files (.mmd and .mermaid extensions).
 */
object MermaidFileType : LanguageFileType(PlainTextLanguage.INSTANCE) {
    override fun getName(): String = "Mermaid"
    
    override fun getDescription(): String = "Mermaid diagram file"
    
    override fun getDefaultExtension(): String = "mmd"
    
    override fun getIcon(): Icon? = null
}
