package de.moritzruth.spigot_ttt

import de.moritzruth.spigot_ttt.game.GameManager
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

class TTTPlugin: JavaPlugin() {
    init {
        pluginInstance = this
    }

    override fun onEnable() {
        saveDefaultConfig()

        CommandManager.initializeCommands()
        GameManager.initialize()

        TTTListener.register()

        // This is required to prevent a lot of bugs
        server.spawnRadius = 0
    }

    override fun onDisable() {
        GameManager.resetWorld()
    }

    fun broadcast(message: String, withPrefix: Boolean = true) {
        if (withPrefix) {
            server.broadcastMessage(prefix + message)
        } else {
            server.broadcastMessage(message)
        }
    }

    companion object {
        val prefix = "${ChatColor.WHITE}●${ChatColor.RESET} "
    }
}

private var pluginInstance: TTTPlugin? = null

val plugin: TTTPlugin
    get() = pluginInstance ?: throw Error("The plugin is not initialized yet (How are you even calling this???)")

const val COMMAND_RESPONSE_PREFIX = "➤ "
