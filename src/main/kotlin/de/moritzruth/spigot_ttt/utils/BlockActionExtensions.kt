package de.moritzruth.spigot_ttt.utils

import org.bukkit.event.block.Action

val Action.isRightClick get() = this == Action.RIGHT_CLICK_AIR || this == Action.RIGHT_CLICK_BLOCK
val Action.isLeftClick get() = this == Action.LEFT_CLICK_AIR || this == Action.LEFT_CLICK_BLOCK
