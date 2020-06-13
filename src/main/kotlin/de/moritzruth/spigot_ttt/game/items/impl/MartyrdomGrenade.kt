package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.GameEndEvent
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.game.items.Buyable
import de.moritzruth.spigot_ttt.game.items.PASSIVE
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.createKillExplosion
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.secondsToTicks
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

object MartyrdomGrenade: TTTItem, Buyable {
    override val type = TTTItem.Type.SPECIAL
    override val itemStack = ItemStack(Resourcepack.Items.martyrdomGrenade).applyMeta {
        hideInfo()
        setDisplayName("${ChatColor.DARK_PURPLE}${ChatColor.BOLD}Märtyriumsgranate $PASSIVE")

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

    override fun onBuy(tttPlayer: TTTPlayer) {
        isc.getOrCreate(tttPlayer)
    }

    override val listener = object : TTTItemListener(this, true) {
        @EventHandler
        fun onTTTPlayerDeath(event: TTTPlayerDeathEvent) {
            val state = isc.get(event.tttPlayer) ?: return

            state.explodeTask = plugin.server.scheduler.runTaskLater(plugin, fun() {
                GameManager.world.playSound(
                    event.location,
                    Resourcepack.Sounds.grenadeExplode,
                    SoundCategory.PLAYERS,
                    1F,
                    1F
                )

                createKillExplosion(event.tttPlayer, event.location, 2.5)
            }, secondsToTicks(3).toLong())
        }

        @EventHandler
        fun onGameEnd(event: GameEndEvent) = isc.forEveryState { state, _ ->
            state.explodeTask?.cancel()
            state.explodeTask = null
        }
    }

    class State: IState {
        var explodeTask: BukkitTask? = null
    }
}
