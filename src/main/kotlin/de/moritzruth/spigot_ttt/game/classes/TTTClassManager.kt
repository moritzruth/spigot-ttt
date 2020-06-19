package de.moritzruth.spigot_ttt.game.classes

import de.moritzruth.spigot_ttt.game.classes.impl.*
import java.util.*

object TTTClassManager {
    private val TTT_CLASSES = setOf(
        Warrior, Gambler, Stuntman, Ninja, Oracle
    )

    val listeners = TTT_CLASSES.mapNotNull { it.listener }

    fun createClassesIterator(count: Int): Iterator<TTTClassCompanion> {
        val set: MutableSet<TTTClassCompanion> = TTT_CLASSES.toMutableSet()

        val playersWithoutClass = count - TTT_CLASSES.size
        if (playersWithoutClass > 0) set.addAll(Collections.nCopies(playersWithoutClass, TTTClass.None))

        return set.shuffled().iterator()
    }
}
