package de.moritzruth.spigot_ttt.items

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata
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
import de.moritzruth.spigot_ttt.utils.startItemDamageProgress
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

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
            .addPacketListener(object : PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
                override fun onPacketSending(event: PacketEvent) {
                    val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
                    val packet = WrapperPlayServerEntityMetadata(event.packet)

                    val playerOfPacket = plugin.server.onlinePlayers.find { it.entityId == packet.entityID } ?: return
                    val tttPlayerOfPacket = PlayerManager.getTTTPlayer(playerOfPacket) ?: return
                    if (tttPlayerOfPacket.alive) {
                        // https://wiki.vg/Entity_metadata#Entity_Metadata_Format
                        try {
                            val modifiers = packet.metadata[0].value as Byte // TODO: Fix this
                            packet.metadata[0].setValue(
                                    if (isc.get(tttPlayer).enabled) modifiers or 0x40
                                    else modifiers and 0b10111111.toByte()
                            )
                        } catch (ignored: Exception) {}
                    }
                }
            })
    }

    override fun reset(tttPlayer: TTTPlayer) {
        setEnabled(tttPlayer, false)

        isc.get(tttPlayer).progressTask?.cancel()
    }

    fun use(tttPlayer: TTTPlayer, item: ItemStack) {
        val state = isc.get(tttPlayer)
        if (state.progressTask != null) return

        setEnabled(tttPlayer, true)

        state.progressTask = startItemDamageProgress(item, 6.0, fromRight = true) {
            setEnabled(tttPlayer, false)

            state.progressTask = startItemDamageProgress(item, 30.0) {
                state.progressTask = null
            }
        }
    }

    private fun setEnabled(tttPlayer: TTTPlayer, value: Boolean) {
        val state = isc.get(tttPlayer)

        if (state.enabled != value) {
            state.enabled = value

            // Toggle sending the entity metadata
            PlayerManager.tttPlayers.forEach {
                if (it !== tttPlayer) {
                    tttPlayer.player.hidePlayer(plugin, it.player)
                    tttPlayer.player.showPlayer(plugin, it.player)
                }
            }
        }
    }

    override val listener = object : Listener {
        @EventHandler
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(Radar)) event.isCancelled = true
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(Radar)) return
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

            if (event.action.isRightClick) {
                use(tttPlayer, event.item!!)
            }

            event.isCancelled = true
        }
    }

    class State: IState {
        var enabled: Boolean = false
        var progressTask: BukkitTask? = null
    }
}
