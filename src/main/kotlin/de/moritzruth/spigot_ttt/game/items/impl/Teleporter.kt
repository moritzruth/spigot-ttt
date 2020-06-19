package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.items.ClickEvent
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.sendActionBarMessage
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

object Teleporter: TTTItem<Teleporter.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    templateItemStack = ItemStack(Resourcepack.Items.teleporter).applyMeta {
        setDisplayName("${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Teleporter")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Tausche die Positionen zweier Spieler",
            "",
            "${ChatColor.AQUA}F -> Mode wechseln",
            "",
            "${ChatColor.RED}Kann nur einmal verwendet werden"
        )
    },
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL),
        price = 1,
        buyLimit = 1
    )
) {
    class Instance: TTTItem.Instance(Teleporter) {
        private var teleportSelf = false

        override fun onRightClick(event: ClickEvent) {
            val tttPlayer = carrier!!
            val firstPlayer = if (teleportSelf) {
                if (!tttPlayer.player.isOnGround) {
                    tttPlayer.player.sendActionBarMessage(
                        "${ChatColor.RED}${ChatColor.BOLD}Du musst auf dem Boden stehen"
                    )
                    null
                } else if (tttPlayer.player.isSneaking) {
                    tttPlayer.player.sendActionBarMessage(
                        "${ChatColor.RED}${ChatColor.BOLD}Du darfst nicht sneaken"
                    )
                    null
                } else tttPlayer
            } else getRandomPlayerToTeleport(tttPlayer)

            if (firstPlayer != null) {
                val secondPlayer = getRandomPlayerToTeleport(tttPlayer, firstPlayer)

                if (secondPlayer != null) {
                    val firstLocation = firstPlayer.player.location
                    firstPlayer.player.teleport(secondPlayer.player.location)
                    secondPlayer.player.teleport(firstLocation)

                    tttPlayer.removeItem(Teleporter)

                    GameManager.world.playSound(firstPlayer.player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1F)
                    GameManager.world.playSound(secondPlayer.player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1F)
                    return
                }
            }

            // Teleport failed
            tttPlayer.player.playSound(tttPlayer.player.location, Resourcepack.Sounds.error, 1F, 1F)
        }

        override fun onHandSwap() {
            teleportSelf = !teleportSelf

            carrier!!.player.sendActionBarMessage(
                if (teleportSelf) "${ChatColor.AQUA}Mode: Teleportiere dich selbst"
                else "${ChatColor.AQUA}Mode: Teleportiere jemand anderen"
            )
        }
    }

    private fun getRandomPlayerToTeleport(vararg exclude: TTTPlayer): TTTPlayer? {
        return try {
            PlayerManager.tttPlayers.filter { !exclude.contains(it) && it.alive && !it.player.isSneaking }.random()
        } catch (e: NoSuchElementException) {
            null
        }
    }
}
