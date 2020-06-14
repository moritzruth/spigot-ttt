package de.moritzruth.spigot_ttt.game.classes

import de.moritzruth.spigot_ttt.game.classes.impl.Gambler
import de.moritzruth.spigot_ttt.game.classes.impl.Ninja
import de.moritzruth.spigot_ttt.game.classes.impl.Stuntman
import de.moritzruth.spigot_ttt.game.classes.impl.Warrior
import java.util.*

object TTTClassManager {
    private val TTT_CLASSES = setOf(
        Warrior, Gambler, Stuntman, Ninja
    // Oracle is disabled because of the bug with the radar
    )

    val listeners = TTT_CLASSES.mapNotNull { it.listener }

    fun createClassesIterator(count: Int): Iterator<TTTClass?> {
        val set: MutableSet<TTTClass?> = TTT_CLASSES.toMutableSet()

        val playersWithoutClass = count - TTT_CLASSES.size
        if (playersWithoutClass > 0) {
            set.addAll(Collections.nCopies(playersWithoutClass, null))
        }

        return set.shuffled().iterator()
    }
}
