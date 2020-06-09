package de.moritzruth.spigot_ttt.items.weapons.guns.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.items.Spawning
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Deagle: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.BLUE}${ChatColor.BOLD}Deagle",
    damage = heartsToHealth(3.0),
    cooldown = 1.4,
    magazineSize = 8,
    reloadTime = 3.0,
    itemMaterial = ResourcePack.Items.deagle,
    shootSound = ResourcePack.Sounds.Item.Weapon.Deagle.fire,
    reloadSound = ResourcePack.Sounds.Item.Weapon.Deagle.reload
), Spawning {
    override val type = TTTItem.Type.PISTOL_LIKE

    class State: Gun.State(magazineSize)
}


