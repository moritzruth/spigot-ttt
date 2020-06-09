package de.moritzruth.spigot_ttt.items.weapons.guns.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.items.Spawning
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Rifle: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Rifle",
    damage = heartsToHealth(0.5),
    cooldown = 0.1,
    magazineSize = 40,
    reloadTime = 2.0,
    itemMaterial = ResourcePack.Items.rifle,
    shootSound = ResourcePack.Sounds.Item.Weapon.Rifle.fire,
    reloadSound = ResourcePack.Sounds.Item.Weapon.Rifle.reload
), Spawning {
    override val type = TTTItem.Type.HEAVY_WEAPON

    class State: Gun.State(magazineSize)
}


