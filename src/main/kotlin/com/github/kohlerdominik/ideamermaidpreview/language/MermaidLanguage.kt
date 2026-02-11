package com.github.kohlerdominik.ideamermaidpreview.language

import com.intellij.lang.Language

/**
 * Language definition for Mermaid diagram syntax.
 */
object MermaidLanguage : Language("Mermaid") {
    private fun readResolve(): Any = MermaidLanguage
}
