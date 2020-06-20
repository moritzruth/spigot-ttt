package de.moritzruth.spigot_ttt.game.classes

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.classes.impl.*
import java.util.*

object TTTClassManager {
    private val TTT_CLASSES = setOf(
        Warrior, Gambler, Stuntman, Ninja, Oracle
    )

    val listeners = TTT_CLASSES.mapNotNull { it.listener }

    fun createClassesIterator(count: Int): Iterator<TTTClassCompanion> {
        val classes: MutableSet<TTTClassCompanion> = TTT_CLASSES.toMutableSet()
        classes.removeAll { GameManager.tttWorld!!.config.getStringList("blocked-classes").contains(it.name) }

        val playersWithoutClass = count - TTT_CLASSES.size
        if (playersWithoutClass > 0) classes.addAll(Collections.nCopies(playersWithoutClass, TTTClass.None))

        return classes.shuffled().iterator()
    }
}
