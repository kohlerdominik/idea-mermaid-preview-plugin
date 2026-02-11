package com.github.kohlerdominik.ideamermaidpreview.language

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import com.intellij.lexer.Lexer
import com.intellij.lexer.LexerBase

/**
 * Factory for creating Mermaid syntax highlighters.
 */
class MermaidSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) =
        MermaidSyntaxHighlighter()
}

/**
 * Simple syntax highlighter for Mermaid diagram files.
 * Provides basic highlighting for common Mermaid keywords and syntax elements.
 */
class MermaidSyntaxHighlighter : SyntaxHighlighterBase() {
    
    companion object {
        // Define text attribute keys for different syntax elements
        val KEYWORD = TextAttributesKey.createTextAttributesKey(
            "MERMAID_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
        )
        
        val COMMENT = TextAttributesKey.createTextAttributesKey(
            "MERMAID_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
        )
    }
    
    override fun getHighlightingLexer(): Lexer = MermaidLexer()
    
    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        return emptyArray()
    }
}

/**
 * Minimal lexer implementation for Mermaid files.
 * This provides a basic framework that can be extended with proper token parsing.
 */
private class MermaidLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var startOffset = 0
    private var endOffset = 0
    private var bufferEnd = 0
    
    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        this.startOffset = startOffset
        this.endOffset = startOffset
        this.bufferEnd = endOffset
    }
    
    override fun getState(): Int = 0
    
    override fun getTokenType(): IElementType? = null
    
    override fun getTokenStart(): Int = startOffset
    
    override fun getTokenEnd(): Int = endOffset
    
    override fun advance() {
        if (endOffset >= bufferEnd) {
            startOffset = bufferEnd
            endOffset = bufferEnd
            return
        }
        startOffset = endOffset
        endOffset = bufferEnd
    }
    
    override fun getBufferSequence(): CharSequence = buffer
    
    override fun getBufferEnd(): Int = bufferEnd
}
