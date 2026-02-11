package com.github.kohlerdominik.ideamermaidpreview.editor

import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.textmate.TextMateBundleProvider

class MermaidBundleProvider : TextMateBundleProvider {
    override fun getBundles(plugin: PluginDescriptor): List<TextMateBundleProvider.PluginBundle> {
        val bundle = plugin.pluginPath.resolve("bundles/mermaid")
        return listOf(TextMateBundleProvider.PluginBundle("mermaid", bundle))
    }
}
