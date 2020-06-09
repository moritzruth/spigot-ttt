package de.moritzruth.spigot_ttt.items.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.TTTItemListener
import de.moritzruth.spigot_ttt.game.GameEndEvent
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.Selectable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.startItemDamageProgress
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerToggleSprintEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask

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
    private const val COOLDOWN = 10.0

    override val type = TTTItem.Type.SPECIAL
    override val price = 2
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)
    override val buyLimit = 1

    val isc = InversedStateContainer(State::class)

    override fun onSelect(tttPlayer: TTTPlayer) {}
    override fun onDeselect(tttPlayer: TTTPlayer) = disable(tttPlayer)

    private fun enable(tttPlayer: TTTPlayer) {
        val state = isc.getOrCreate(tttPlayer)

        tttPlayer.player.apply {
            isSprinting = false
            walkSpeed = 0.1F

            // To prevent jumping (amplifier 200)
            addPotionEffect(PotionEffect(PotionEffectType.JUMP, 1000000, 200, false, false))

            playSound(location, ResourcePack.Sounds.Item.CloakingDevice.on, SoundCategory.PLAYERS, 1F, 1F)
        }

        tttPlayer.invisible = true
        state.enabled = true
    }

    private fun disable(tttPlayer: TTTPlayer) {
        val state = isc.getOrCreate(tttPlayer)

        tttPlayer.player.apply {
            walkSpeed = 0.2F
            removePotionEffect(PotionEffectType.JUMP)
            playSound(location, ResourcePack.Sounds.Item.CloakingDevice.off, SoundCategory.PLAYERS, 1F, 1F)
        }

        tttPlayer.invisible = false
        state.enabled = false
    }

    override val listener = object : TTTItemListener(this, true) {
        @EventHandler
        fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) = handle(event) { tttPlayer ->
            if (event.isSprinting && isc.getOrCreate(tttPlayer).enabled) event.isCancelled = true
        }

        @EventHandler
        fun onTTTPlayerDeath(event: TTTPlayerDeathEvent) = isc.get(event.tttPlayer)?.cooldownTask?.cancel()

        @EventHandler
        fun onGameEnd(event: GameEndEvent) = isc.forEveryState { state, _ -> state.cooldownTask?.cancel() }

        override fun onRightClick(data: ClickEventData) {
            val state = isc.getOrCreate(data.tttPlayer)
            if (state.cooldownTask != null) return

            if (state.enabled) {
                disable(data.tttPlayer)
                state.cooldownTask = startItemDamageProgress(data.itemStack, COOLDOWN) { state.cooldownTask = null }
            } else {
                enable(data.tttPlayer)
            }
        }
    }

    class State: IState {
        var enabled: Boolean = false
        var cooldownTask: BukkitTask? = null
    }
}
