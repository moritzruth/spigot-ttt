package de.moritzruth.spigot_ttt.items.weapons.guns

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.items.Spawning
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Deagle: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.BLUE}${ChatColor.BOLD}Deagle",
    damage = heartsToHealth(3.0),
    cooldown = 1.4,
    magazineSize = 8,
    reloadTime = 3.0,
    itemMaterial = CustomItems.deagle
), Spawning {
    override val type = TTTItem.Type.PISTOL_LIKE

    class State: Gun.State(magazineSize)
}


