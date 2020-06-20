package de.moritzruth.spigot_ttt.game.worlds

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.ConfigurationFile
import org.bukkit.World
import org.bukkit.WorldCreator
import java.io.File
import java.io.FileNotFoundException

class TTTWorld(private val sourceWorldDir: File) {
    init {
        if (!sourceWorldDir.exists()) throw FileNotFoundException()
    }

    private val name: String = sourceWorldDir.name
    val config = ConfigurationFile(sourceWorldDir.resolve("config.yml"))

    private val actualWorldName = "${WORLD_PREFIX}${name}"
    private val worldDir = plugin.server.worldContainer.resolve("./$actualWorldName")
    var world: World? = plugin.server.getWorld(actualWorldName); private set
    val spawnLocations = SpawnLocationsManager(this)

    init {
        if (world == null) {
            if (worldDir.exists()) worldDir.deleteRecursively()
        } else GameManager.tttWorld = this
    }

    fun load() {
        if (world != null) throw IllegalStateException("The world is already loaded")

        sourceWorldDir.copyRecursively(worldDir)
        loadWorld()
    }

    private fun loadWorld() {
        world = plugin.server.getWorld(actualWorldName)
                ?: plugin.server.createWorld(WorldCreator.name(actualWorldName))
    }

    fun unload() {
        if  (world == null) throw IllegalStateException("The world is not loaded")
        unloadWorld()
        worldDir.deleteRecursively()
    }

    private fun unloadWorld() {
        world!!.players.forEach { it.teleport(plugin.server.getWorld("world")!!.spawnLocation) }
        plugin.server.unloadWorld(actualWorldName, false)
        world = null
    }

    companion object {
        fun createForSourceWorlds(): Set<TTTWorld> =
            WORLDS_DIR.listFiles(File::isDirectory)!!.map { TTTWorld(it) }.toSet()

        const val WORLD_PREFIX = "tempworld_"
        private val WORLDS_DIR = plugin.dataFolder.resolve("./worlds").also { it.mkdirs() }
    }
}
