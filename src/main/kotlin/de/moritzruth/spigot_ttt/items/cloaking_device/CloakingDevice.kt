package de.moritzruth.spigot_ttt.items.cloaking_device

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.*
import de.moritzruth.spigot_ttt.items.BuyableItem
import de.moritzruth.spigot_ttt.items.SelectableItem
import de.moritzruth.spigot_ttt.items.TTTItem
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

object CloakingDevice: TTTItem, BuyableItem, SelectableItem {
    override val displayName = "${ChatColor.GRAY}${ChatColor.MAGIC}###${ChatColor.RESET}${ChatColor.GRAY} Cloaking Device ${ChatColor.MAGIC}###"
    override val itemStack = ItemStack(CustomItems.cloakingDevice)
    override val type = TTTItem.Type.SPECIAL
    override val listener = CloakingDeviceListener
    override val spawning = false
    override val price = 2
    override val buyableBy = EnumSet.of(DETECTIVE, TRAITOR, JACKAL)

    init {
        val meta = itemStack.itemMeta!!

        meta.setDisplayName(displayName)
        meta.lore = listOf(
            "",
            "${ChatColor.GOLD}Macht dich unsichtbar"
        )

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        itemStack.itemMeta = meta
    }

    override fun onSelect(tttPlayer: TTTPlayer) {}
    override fun onDeselect(tttPlayer: TTTPlayer) = setEnabled(tttPlayer, false)

    fun getState(tttPlayer: TTTPlayer) = tttPlayer.stateContainer.get(CloakingDeviceState::class) { CloakingDeviceState() }

    fun setEnabled(tttPlayer: TTTPlayer, value: Boolean?) {
        val state = getState(tttPlayer)
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
}
