package de.moritzruth.spigot_ttt.utils

import com.comphenix.packetwrapper.WrapperPlayServerTitle
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import org.bukkit.entity.Player

fun Player.sendActionBarMessage(
    message: String,
    fadeIn: Int = secondsToTicks(0.2),
    stay: Int = secondsToTicks(5),
    fadeOut: Int = secondsToTicks(1)
) {
    val wrapper = WrapperPlayServerTitle()
    wrapper.action = EnumWrappers.TitleAction.ACTIONBAR
    wrapper.title = WrappedChatComponent.fromText(message)
    wrapper.fadeIn = fadeIn
    wrapper.fadeOut = fadeOut
    wrapper.stay = stay

    wrapper.sendPacket(this)
}

fun Player.teleportToWorldSpawn() {
    teleport(world.spawnLocation)
}
