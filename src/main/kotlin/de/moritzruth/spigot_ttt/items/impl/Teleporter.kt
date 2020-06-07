package de.moritzruth.spigot_ttt.items.impl

import com.connorlinfoot.actionbarapi.ActionBarAPI
import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.clearHeldItemSlot
import de.moritzruth.spigot_ttt.utils.isRightClick
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

object Teleporter: TTTItem, Buyable {
    override val type = TTTItem.Type.SPECIAL
    override val itemStack = ItemStack(ResourcePack.Items.teleporter).applyMeta {
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

    override val listener = object : Listener {
        @EventHandler
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(Teleporter)) event.isCancelled = true
        }

        @EventHandler
        fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
            if (!event.isRelevant(Teleporter)) return

            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
            val state = isc.getOrCreate(tttPlayer)

            state.teleportSelf = !state.teleportSelf

            if (state.teleportSelf) {
                ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.AQUA}Mode: Teleportiere dich selbst")
            } else {
                ActionBarAPI.sendActionBar(tttPlayer.player, "${ChatColor.AQUA}Mode: Teleportiere jemand anderen")
            }
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(Teleporter)) return
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

            event.isCancelled = true

            if (event.action.isRightClick) {
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
                tttPlayer.player.playSound(tttPlayer.player.location, ResourcePack.Sounds.error, 1F, 1F)
            }
        }
    }

    class State: IState {
        var teleportSelf = false
    }
}