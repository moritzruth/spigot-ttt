package de.moritzruth.spigot_ttt.game.items.impl

import com.connorlinfoot.actionbarapi.ActionBarAPI
import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.TTTItemListener
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.clearHeldItemSlot
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

object Teleporter: TTTItem, Buyable {
    override val type = TTTItem.Type.SPECIAL
    override val itemStack = ItemStack(Resourcepack.Items.teleporter).applyMeta {
        setDisplayName("${ChatColor.LIGHT_PURPLE}${ChatColor.BOLD}Teleporter")

        lore = listOf(
            "",
            "${ChatColor.GOLD}Tausche die Positionen zweier Spieler",
            "",
            "${ChatColor.AQUA}F -> Mode wechseln",
            "",
            "${ChatColor.RED}Kann nur einmal verwendet werden"
        )
    }
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)
    override val price = 1
    override val buyLimit = 1
    val isc = InversedStateContainer(State::class)

    private fun getRandomPlayerToTeleport(vararg exclude: TTTPlayer): TTTPlayer? {
        return try {
            PlayerManager.tttPlayers.filter { !exclude.contains(it) && it.alive && !it.player.isSneaking }.random()
        } catch (e: NoSuchElementException) {
            null
        }
    }

    override val listener = object : TTTItemListener(this, true) {
        @EventHandler
        fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) = handle(event) { tttPlayer ->
            val state = isc.getOrCreate(tttPlayer)
            state.teleportSelf = !state.teleportSelf

            if (state.teleportSelf) {
                ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.AQUA}Mode: Teleportiere dich selbst")
            } else {
                ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.AQUA}Mode: Teleportiere jemand anderen")
            }
        }

        override fun onRightClick(data: ClickEventData) {
            val tttPlayer = data.tttPlayer
            val state = isc.getOrCreate(tttPlayer)

            val firstPlayer = if (state.teleportSelf) {
                if (!tttPlayer.player.isOnGround) {
                    ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}${ChatColor.BOLD}Du musst auf dem Boden stehen")
                    null
                } else if (tttPlayer.player.isSneaking) {
                    ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.RED}${ChatColor.BOLD}Du darfst nicht sneaken")
                    null
                } else tttPlayer
            } else getRandomPlayerToTeleport(tttPlayer)

            if (firstPlayer != null) {
                val secondPlayer = getRandomPlayerToTeleport(tttPlayer, firstPlayer)

                if (secondPlayer != null) {
                    val firstLocation = firstPlayer.player.location
                    firstPlayer.player.teleport(secondPlayer.player.location)
                    secondPlayer.player.teleport(firstLocation)

                    tttPlayer.player.inventory.clearHeldItemSlot()

                    GameManager.world.playSound(firstPlayer.player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1F)
                    GameManager.world.playSound(secondPlayer.player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1F)
                    return
                }
            }

            // Teleport failed
            tttPlayer.player.playSound(tttPlayer.player.location, Resourcepack.Sounds.error, 1F, 1F)
        }
    }

    class State: IState {
        var teleportSelf = false
    }
}
