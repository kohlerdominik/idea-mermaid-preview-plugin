package com.github.kohlerdominik.ideamermaidpreview.textmate

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import org.jetbrains.plugins.textmate.api.TextMateBundleProvider
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.JarFile
import java.net.JarURLConnection

class MermaidTextMateBundleProvider : TextMateBundleProvider {
    override fun getBundles(): List<TextMateBundleProvider.PluginBundle> {
        val pluginId = PluginId.getId(PLUGIN_ID)
        val plugin = PluginManagerCore.getPlugin(pluginId)
        if (plugin == null) {
            LOG.warn("TextMate bundle not registered: plugin not found: $PLUGIN_ID")
            return emptyList()
        }

        val bundlePath = resolveBundlePath()
        if (bundlePath == null) {
            LOG.warn("TextMate bundle not registered: bundle resources not found")
            return emptyList()
        }

        LOG.info("Registering TextMate bundle from: ${bundlePath.toAbsolutePath()}")
        return listOf(TextMateBundleProvider.PluginBundle("mermaid", bundlePath))
    }

    private fun resolveBundlePath(): Path? {
        extractedPath?.let { cached ->
            if (Files.exists(cached)) {
                return cached
            }
        }

        val resourceUrl = MermaidTextMateBundleProvider::class.java.classLoader
            .getResource("bundles/mermaid/package.json") ?: return null

        if (resourceUrl.protocol == "file") {
            val filePath = Paths.get(resourceUrl.toURI()).parent
            if (filePath != null && Files.exists(filePath)) {
                extractedPath = filePath
                return filePath
            }
        }

        if (resourceUrl.protocol == "jar") {
            val connection = resourceUrl.openConnection() as? JarURLConnection ?: return null
            val jarFileUrl = connection.jarFileURL
            val jarFile = JarFile(Paths.get(jarFileUrl.toURI()).toFile())
            val outputDir = Files.createTempDirectory(Paths.get(PathManager.getTempPath()), "mermaid-bundle-")
            val prefix = "bundles/mermaid/"

            jarFile.use { jar ->
                val entries = jar.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (!entry.name.startsWith(prefix)) {
                        continue
                    }
                    val relative = entry.name.removePrefix(prefix)
                    if (relative.isEmpty()) {
                        continue
                    }
                    val target = outputDir.resolve(relative)
                    if (entry.isDirectory) {
                        Files.createDirectories(target)
                        continue
                    }
                    Files.createDirectories(target.parent)
                    jar.getInputStream(entry).use { input ->
                        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }

            extractedPath = outputDir
            return outputDir
        }

        return null
    }

    private companion object {
        const val PLUGIN_ID = "com.github.kohlerdominik.ideamermaidpreview"
        val LOG: Logger = Logger.getInstance(MermaidTextMateBundleProvider::class.java)
        @Volatile
        var extractedPath: Path? = null
    }
}
