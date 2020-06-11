package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.plugin
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Paths

class ConfigurationFile(name: String): YamlConfiguration() {
    private val filePath = Paths.get(plugin.dataFolder.absolutePath, "$name.yml").toAbsolutePath().toString()

    init {
        try {
            load(filePath)
        } catch (e: Exception) {}
    }

    fun save() {
        save(filePath)
    }
}
