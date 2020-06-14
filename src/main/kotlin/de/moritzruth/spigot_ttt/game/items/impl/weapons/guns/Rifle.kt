package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.items.Spawning
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Rifle: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Rifle",
    damage = heartsToHealth(0.8),
    cooldown = 0.15,
    magazineSize = 40,
    reloadTime = 2.0,
    itemMaterial = Resourcepack.Items.rifle,
    shootSound = Resourcepack.Sounds.Item.Weapon.Rifle.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Rifle.reload
), Spawning {
    override val type = TTTItem.Type.HEAVY_WEAPON

    class State: Gun.State(magazineSize)
}


