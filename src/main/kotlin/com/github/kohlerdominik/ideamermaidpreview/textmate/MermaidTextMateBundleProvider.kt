package com.github.kohlerdominik.ideamermaidpreview.textmate

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import org.jetbrains.plugins.textmate.api.TextMateBundleProvider
import java.nio.file.Files

class MermaidTextMateBundleProvider : TextMateBundleProvider {
    override fun getBundles(): List<TextMateBundleProvider.PluginBundle> {
        val pluginId = PluginId.getId(PLUGIN_ID)
        val plugin = PluginManagerCore.getPlugin(pluginId)
        if (plugin == null) {
            LOG.warn("TextMate bundle not registered: plugin not found: $PLUGIN_ID")
            return emptyList()
        }

        val bundlePath = plugin.pluginPath.resolve("bundles/mermaid")
        if (!Files.exists(bundlePath)) {
            LOG.warn("TextMate bundle path missing: ${bundlePath.toAbsolutePath()}")
            return emptyList()
        }

        LOG.info("Registering TextMate bundle from: ${bundlePath.toAbsolutePath()}")
        return listOf(TextMateBundleProvider.PluginBundle("mermaid", bundlePath))
    }

    private companion object {
        const val PLUGIN_ID = "com.github.kohlerdominik.ideamermaidpreview"
        val LOG: Logger = Logger.getInstance(MermaidTextMateBundleProvider::class.java)
    }
}
