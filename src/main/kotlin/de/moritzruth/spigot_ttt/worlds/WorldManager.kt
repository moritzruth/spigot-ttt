package de.moritzruth.spigot_ttt.worlds

import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.ConfigurationFile
import java.io.File

object WorldManager {
    data class SourceWorld(val dir: File) {
        val name: String = dir.name
        val config = ConfigurationFile(dir.resolve("config.yml"))
    }

    val worldsDir = plugin.dataFolder.resolve("./worlds").also { it.mkdirs() }
    val sourceWorlds = worldsDir.listFiles(File::isDirectory)!!.map { SourceWorld(it) }

    val tttWorlds = mutableSetOf<TTTWorld>()

    fun removeNeglectedWorlds() {
        plugin.server.worldContainer.listFiles { file ->
            file.isDirectory && file.name.startsWith(TTTWorld.WORLD_PREFIX) &&
                    tttWorlds.find { it.actualWorldName == file.name } != null
        }!!.forEach { it.deleteRecursively() }
    }
}
