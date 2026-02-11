package com.github.kohlerdominik.ideamermaidpreview.language

import com.intellij.lang.Language

object MermaidLanguage : Language("Mermaid") {
    private fun readResolve(): Any = MermaidLanguage
    override fun getDisplayName(): String = "Mermaid"
}
