package de.moritzruth.spigot_ttt.utils

import de.moritzruth.spigot_ttt.plugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigurationFile(private val file: File): YamlConfiguration() {
    constructor(name: String) : this(plugin.dataFolder.resolve("$name.yml"))

    init {
        try {
            load(file)
        } catch (e: Exception) {}
    }

    fun save() {
        save(file)
    }
}
