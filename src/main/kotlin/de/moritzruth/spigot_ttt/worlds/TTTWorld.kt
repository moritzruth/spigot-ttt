package de.moritzruth.spigot_ttt.worlds

import de.moritzruth.spigot_ttt.plugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.bukkit.WorldCreator

class TTTWorld(val sourceWorld: WorldManager.SourceWorld) {
    var state: State = State.NOT_COPIED; private set
    enum class State {
        NOT_COPIED,
        COPYING,
        COPIED,
        LOADED,
        UNLOADING
    }

    val id = WorldManager.tttWorlds.count()
    val actualWorldName = "${WORLD_PREFIX}${id}"
    private val worldDir = plugin.server.worldContainer.resolve("./$actualWorldName")

    init {
        WorldManager.tttWorlds.add(this)
    }

    suspend fun copy() {
        if (state != State.NOT_COPIED) throw IllegalStateException("The world was already copied")
        state = State.COPYING

        coroutineScope {
            launch(Dispatchers.IO) {
                sourceWorld.dir.copyRecursively(worldDir)
            }
        }
    }

    fun load() {
        if (state != State.COPIED) throw IllegalStateException("The world was not copied yet or already loaded")
        plugin.server.createWorld(WorldCreator.name(actualWorldName))
    }

    suspend fun save() {
        if (state != State.LOADED) throw IllegalStateException("The world must be loaded")

        coroutineScope {
            launch(Dispatchers.IO) {
                val tempWorldDir = WorldManager.worldsDir.resolve("./${sourceWorld.name}_$id")
                worldDir.copyRecursively(tempWorldDir)
                sourceWorld.dir.deleteRecursively()
                tempWorldDir.renameTo(sourceWorld.dir)
                state = State.NOT_COPIED
            }
        }
    }

    suspend fun unloadAndRemove() {
        if  (state != State.LOADED) throw IllegalStateException("The world must be loaded")
        state = State.UNLOADING
        plugin.server.unloadWorld(actualWorldName, false)

        coroutineScope {
            launch(Dispatchers.IO) {
                worldDir.deleteRecursively()
                state = State.NOT_COPIED
            }
        }
    }

    companion object {
        const val WORLD_PREFIX = "tempworld_"
    }
}
