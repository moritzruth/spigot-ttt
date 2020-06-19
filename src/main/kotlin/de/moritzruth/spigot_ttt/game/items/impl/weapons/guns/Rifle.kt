package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.items.SpawnProbability
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Rifle: Gun(
    type = Type.HEAVY_WEAPON,
    instanceType = Instance::class,
    spawnProbability = SpawnProbability.NORMAL,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Rifle",
    damage = heartsToHealth(0.8),
    cooldown = 0.15,
    magazineSize = 40,
    reloadTime = 2.0,
    material = Resourcepack.Items.rifle,
    shootSound = Resourcepack.Sounds.Item.Weapon.Rifle.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Rifle.reload
) {
    class Instance: Gun.Instance(Rifle)
}


