package de.moritzruth.spigot_ttt.items.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.Selectable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSprintEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object CloakingDevice: TTTItem,
    Buyable,
    Selectable {
    override val itemStack = ItemStack(ResourcePack.Items.cloakingDevice).applyMeta {
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
    override val buyLimit = 1

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

                playSound(location, ResourcePack.Sounds.Item.CloakingDevice.on, SoundCategory.PLAYERS, 1F, 1F)
            }

            tttPlayer.invisible = true
            state.enabled = true
        } else {
            tttPlayer.player.apply {
                walkSpeed = 0.2F
                removePotionEffect(PotionEffectType.JUMP)
                playSound(location, ResourcePack.Sounds.Item.CloakingDevice.off, SoundCategory.PLAYERS, 1F, 1F)
            }

            tttPlayer.invisible = false
            state.enabled = false
        }
    }

    override val listener = object : TTTItemListener(this, true) {
        @EventHandler
        fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) = handle(event) { tttPlayer ->
            if (event.isSprinting && isc.getOrCreate(tttPlayer).enabled) event.isCancelled = true
        }

        override fun onRightClick(data: Data<PlayerInteractEvent>) {
            setEnabled(data.tttPlayer, null)
        }
    }

    class State: IState {
        var enabled: Boolean = false
    }
}
