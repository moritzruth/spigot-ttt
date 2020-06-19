package de.moritzruth.spigot_ttt.game.items.impl.weapons.guns

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.utils.sendActionBarMessage
import org.bukkit.ChatColor

object SidekickDeagle: Gun(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    shopInfo = ShopInfo(
        buyableBy = roles(Role.JACKAL),
        buyLimit = 1,
        price = 1
    ),
    displayName = "${ChatColor.AQUA}${ChatColor.BOLD}Sidekick Deagle",
    itemLore = listOf(
        "",
        "${ChatColor.GOLD}Mache einen Spieler zu deinem Sidekick",
        "",
        "${ChatColor.RED}Nur ein Schuss"
    ),
    appendLore = false,
    damage = 0.1, // Not really
    cooldown = 1.0,
    magazineSize = 1,
    reloadTime = 0.0,
    material = Resourcepack.Items.sidekickDeagle,
    shootSound = Resourcepack.Sounds.Item.Weapon.Deagle.fire,
    reloadSound = Resourcepack.Sounds.Item.Weapon.Deagle.reload
) {
    class Instance: Gun.Instance(SidekickDeagle) {
        override fun reload() {
            requireCarrier().player.sendActionBarMessage("${ChatColor.RED}Du kannst diese Waffe nicht nachladen")
        }

        override fun onHit(tttPlayer: TTTPlayer, hitTTTPlayer: TTTPlayer) {
            hitTTTPlayer.changeRole(Role.SIDEKICK)
        }

        override fun onBeforeShoot(): Boolean {
            val tttPlayer = requireCarrier()
            if (tttPlayer.role != Role.JACKAL) {
                tttPlayer.player.sendActionBarMessage("${ChatColor.RED}Diese Waffe kann nur der Jackal benutzen")
                return false
            }

            return super.onBeforeShoot()
        }
    }
}


