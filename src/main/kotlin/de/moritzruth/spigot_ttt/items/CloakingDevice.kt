package de.moritzruth.spigot_ttt.items

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.IState
import de.moritzruth.spigot_ttt.game.players.InversedStateContainer
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.*
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSprintEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

object CloakingDevice: TTTItem, Buyable, Selectable {
    override val itemStack = ItemStack(CustomItems.cloakingDevice).applyMeta {
        setDisplayName("${ChatColor.GRAY}${ChatColor.MAGIC}###${ChatColor.RESET}${ChatColor.GRAY} Cloaking Device ${ChatColor.MAGIC}###")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Macht dich unsichtbar"
        )
        addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }
    override val type = TTTItem.Type.SPECIAL
    override val price = 2
    override val buyableBy = EnumSet.of(DETECTIVE, TRAITOR, JACKAL)

    val isc = InversedStateContainer(State::class)

    override fun onSelect(tttPlayer: TTTPlayer) {}
    override fun onDeselect(tttPlayer: TTTPlayer) =
        setEnabled(tttPlayer, false)

    fun setEnabled(tttPlayer: TTTPlayer, value: Boolean?) {
        val state = isc.get(tttPlayer)
        if (state.enabled == value) return

        if (value ?: !state.enabled) {
            tttPlayer.player.apply {
                isSprinting = false
                walkSpeed = 0.1F

                // To prevent jumping (amplifier 200)
                addPotionEffect(PotionEffect(PotionEffectType.JUMP, 1000000, 200, false, false))
            }

            tttPlayer.invisible = true
            state.enabled = true
        } else {
            tttPlayer.player.apply {
                walkSpeed = 0.2F
                removePotionEffect(PotionEffectType.JUMP)
            }

            tttPlayer.invisible = false
            state.enabled = false
        }

        // TODO: Play sound
    }

    override val listener = object : Listener {
        @EventHandler
        fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) {
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

            if (event.isSprinting && isc.get(tttPlayer).enabled) {
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(CloakingDevice)) return
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

            setEnabled(tttPlayer, null)
        }
    }

    class State: IState {
        var enabled: Boolean = false
    }
}
