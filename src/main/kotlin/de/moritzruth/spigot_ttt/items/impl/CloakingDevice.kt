package de.moritzruth.spigot_ttt.items.impl

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.Selectable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.isRightClick
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSprintEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object CloakingDevice: TTTItem,
    Buyable,
    Selectable {
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
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)

    val isc = InversedStateContainer(State::class)

    override fun onSelect(tttPlayer: TTTPlayer) {}
    override fun onDeselect(tttPlayer: TTTPlayer) = setEnabled(tttPlayer, false)

    fun setEnabled(tttPlayer: TTTPlayer, value: Boolean?) {
        val state = isc.getOrCreate(tttPlayer)
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
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (event.isRelevant(CloakingDevice)) event.isCancelled = true
        }

        @EventHandler
        fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) {
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

            if (event.isSprinting && isc.getOrCreate(tttPlayer).enabled) {
                event.isCancelled = true
            }
        }

        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(CloakingDevice)) return
            val tttPlayer = PlayerManager.getTTTPlayer(event.player) ?: return

            if (event.action.isRightClick) setEnabled(
                tttPlayer,
                null
            )
            event.isCancelled = true
        }
    }

    class State: IState {
        var enabled: Boolean = false
    }
}
