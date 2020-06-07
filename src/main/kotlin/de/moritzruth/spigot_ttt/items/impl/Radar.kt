package de.moritzruth.spigot_ttt.items.impl

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.isRightClick
import de.moritzruth.spigot_ttt.utils.secondsToTicks
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
    private val DISPLAY_NAME = "${ChatColor.DARK_AQUA}${ChatColor.BOLD}Radar"

    override val itemStack = ItemStack(CustomItems.radar).applyMeta {
        setDisplayName(DISPLAY_NAME)
        lore = listOf(
            "",
            "${ChatColor.GOLD}Zeigt dir alle 30 Sekunden",
            "${ChatColor.GOLD}für 10 Sekunden die Positionen",
            "${ChatColor.GOLD}aller Spieler"
        )

        hideInfo()
    }
    override val type = TTTItem.Type.SPECIAL
    override val buyableBy: EnumSet<Role> = EnumSet.of(Role.TRAITOR, Role.DETECTIVE, Role.JACKAL)
    override val price = 2

    val isc = InversedStateContainer(State::class)

    override fun reset(tttPlayer: TTTPlayer) {
        setGlowingEnabled(tttPlayer, false)
        isc.get(tttPlayer)?.task?.cancel()
    }

    fun use(tttPlayer: TTTPlayer, itemStack: ItemStack) {
        val state = isc.getOrCreate(tttPlayer)
        if (state.enabled) return
        state.enabled = true

        startLoop(tttPlayer, state)
        itemStack.applyMeta {
            setDisplayName(DISPLAY_NAME + "${ChatColor.RESET} - ${ChatColor.GREEN}Aktiv")
        }
    }

    private fun startLoop(tttPlayer: TTTPlayer, state: State) {
        setGlowingEnabled(tttPlayer, true)

        state.task = plugin.server.scheduler.runTaskLater(plugin, fun() {
            setGlowingEnabled(tttPlayer, false)

            state.task = plugin.server.scheduler.runTaskLater(plugin, fun() {
                startLoop(tttPlayer, state)
            }, secondsToTicks(30).toLong())
        }, secondsToTicks(10).toLong())
    }

    private fun setGlowingEnabled(tttPlayer: TTTPlayer, value: Boolean) {
        val state = isc.getOrCreate(tttPlayer)

        if (state.glowingEnabled != value) {
            state.glowingEnabled = value

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

    override val packetListener = object : PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
        override fun onPacketSending(event: PacketEvent) {
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return
            val packet = WrapperPlayServerEntityMetadata(event.packet)

            val playerOfPacket = plugin.server.onlinePlayers.find { it.entityId == packet.entityID } ?: return
            val tttPlayerOfPacket = PlayerManager.getTTTPlayer(playerOfPacket) ?: return
            if (tttPlayerOfPacket.alive) {
                // https://wiki.vg/Entity_metadata#Entity_Metadata_Format
                try {
                    val modifiers = packet.metadata[0].value as Byte
                    packet.metadata[0].value = if (isc.get(tttPlayer)?.glowingEnabled == true) modifiers or 0x40
                    else modifiers and 0b10111111.toByte()
                } catch (ignored: Exception) {
                    // Idk why this throws exceptions, but it works anyways
                }
            }
        }
    }

    class State: IState {
        var enabled = false
        var glowingEnabled = false
        var task: BukkitTask? = null
    }
}
