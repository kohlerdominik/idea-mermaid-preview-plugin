package com.github.kohlerdominik.ideamermaidpreview.file

import com.intellij.openapi.fileTypes.LanguageFileType
import com.github.kohlerdominik.ideamermaidpreview.language.MermaidLanguage
import javax.swing.Icon

/**
 * File type for Mermaid diagram files.
 */
object MermaidFileType : LanguageFileType(MermaidLanguage) {
    override fun getName(): String = "Mermaid"
    
    override fun getDescription(): String = "Mermaid diagram file"
    
    override fun getDefaultExtension(): String = "mmd"
    
    override fun getIcon(): Icon? = null
}
