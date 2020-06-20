package de.moritzruth.spigot_ttt.game.worlds

object WorldManager {
    lateinit var tttWorlds: Set<TTTWorld>

    fun initialize() {
        tttWorlds = TTTWorld.createForSourceWorlds()
    }
}
