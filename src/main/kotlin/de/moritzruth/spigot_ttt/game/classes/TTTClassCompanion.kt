package de.moritzruth.spigot_ttt.game.classes

import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

abstract class TTTClassCompanion(
    val displayName: String,
    val chatColor: ChatColor,
    private val instanceType: KClass<out TTTClass>,
    val defaultItems: Set<TTTItem<*>> = emptySet()
) {
    val coloredDisplayName = "$chatColor$displayName"
    fun createInstance(tttPlayer: TTTPlayer): TTTClass {
        val instance = instanceType.primaryConstructor!!.call()
        instance.tttPlayer = tttPlayer
        return instance
    }

    open val listener: Listener? = null
}
