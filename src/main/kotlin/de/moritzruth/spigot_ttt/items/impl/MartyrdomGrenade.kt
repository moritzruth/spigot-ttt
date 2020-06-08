package de.moritzruth.spigot_ttt.items.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.createKillExplosion
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

object MartyrdomGrenade: TTTItem, Buyable {
    val DISPLAY_NAME = "${ChatColor.DARK_PURPLE}${ChatColor.BOLD}Märtyriumsgranate"

    override val type = TTTItem.Type.SPECIAL
    override val itemStack = ItemStack(ResourcePack.Items.martyrdomGrenade).applyMeta {
        hideInfo()
        setDisplayName(DISPLAY_NAME)

        lore = listOf(
            "",
            "${ChatColor.GOLD}Lasse bei deinem Tod",
            "${ChatColor.GOLD}eine Granate fallen"
        )
    }
    override val buyableBy = roles(Role.TRAITOR)
    override val buyLimit: Int? = null
    override val price = 1
    val isc = InversedStateContainer(State::class)

    override val listener = object : Listener {
        @EventHandler
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(MartyrdomGrenade)) event.isCancelled = true
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(MartyrdomGrenade)) return
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
            event.isCancelled = true

            val state = isc.getOrCreate(tttPlayer)
            state.enabled = !state.enabled

            event.item!!.applyMeta {
                if (state.enabled) {
                    setDisplayName(DISPLAY_NAME + "${ChatColor.RESET} - ${ChatColor.GREEN}Aktiviert")
                } else {
                    setDisplayName(DISPLAY_NAME)
                }
            }
        }

        @EventHandler
        fun onTTTPlayerDeath(event: TTTPlayerDeathEvent) {
            val state = isc.get(event.tttPlayer) ?: return

            state.explodeTask = plugin.server.scheduler.runTaskLater(plugin, fun() {
                GameManager.world.playSound(
                    event.location,
                    ResourcePack.Sounds.grenadeExplode,
                    SoundCategory.PLAYERS,
                    1F,
                    1F
                )

                createKillExplosion(event.tttPlayer, event.location, 2.5)
            }, secondsToTicks(3).toLong())
        }
    }

    class State: IState {
        var enabled = false
        var explodeTask: BukkitTask? = null

        override fun reset(tttPlayer: TTTPlayer) {
            explodeTask?.cancel()
            explodeTask = null
        }
    }
}
