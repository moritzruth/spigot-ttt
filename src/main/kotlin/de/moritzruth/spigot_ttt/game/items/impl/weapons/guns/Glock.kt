package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.items.SpawnProbability
import de.moritzruth.spigot_ttt.utils.heartsToHealth
import org.bukkit.ChatColor

object Glock: Gun(
    type = Type.PISTOL_LIKE,
    instanceType = Instance::class,
    displayName = "${ChatColor.YELLOW}${ChatColor.BOLD}Glock",
    damage = heartsToHealth(1.5),
    spawnProbability = SpawnProbability.NORMAL,
    cooldown = 0.3,
    magazineSize = 20,
    reloadTime = 2.0,
    material = Resourcepack.Items.glock,
    shootSound = Resourcepack.Sounds.Item.Weapon.Glock.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Glock.reload
) {
    class Instance: Gun.Instance(Glock)
}


