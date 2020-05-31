package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.plugin
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Path

class ConfigurationFile(name: String): YamlConfiguration() {
    private val filePath = Path.of(plugin.dataFolder.absolutePath, "$name.yml").toAbsolutePath().toString()

    init {
        try {
            load(filePath)
        } catch (e: Exception) {}
    }

    fun save() {
        save(filePath)
    }
}
