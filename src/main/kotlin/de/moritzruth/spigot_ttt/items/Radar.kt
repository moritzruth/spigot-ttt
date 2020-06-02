package de.moritzruth.spigot_ttt.items

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.IState
import de.moritzruth.spigot_ttt.game.players.InversedStateContainer
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.isRightClick
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.*

object Radar: TTTItem, Buyable {
    override val itemStack = ItemStack(CustomItems.radar).applyMeta {
        setDisplayName("${ChatColor.DARK_AQUA}${ChatColor.BOLD}Radar")
    }
    override val type = TTTItem.Type.SPECIAL
    override val buyableBy = EnumSet.of(TTTPlayer.Role.TRAITOR, TTTPlayer.Role.JACKAL)
    override val price = 2

    val isc = InversedStateContainer(State::class)

    init {
        ProtocolLibrary
            .getProtocolManager()
            .addPacketListener(
                object : PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
            override fun onPacketSending(event: PacketEvent?) {

            }
        })
    }

    override fun reset(tttPlayer: TTTPlayer) {
        stopGlowing(tttPlayer)
    }

    fun use(tttPlayer: TTTPlayer) {
    }

    private fun startGlowing(tttPlayer: TTTPlayer) {
    }

    private fun stopGlowing(tttPlayer: TTTPlayer) {

    }

    override val listener = object : Listener {
        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(Radar)) return
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

            if (event.action.isRightClick) {
                use(tttPlayer)
            }

            event.isCancelled = true
        }
    }

    class State: IState {
        var progressTask: BukkitTask? = null
    }
}
