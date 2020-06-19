package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.items.ClickEvent
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.utils.applyMeta
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerToggleSprintEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask

object CloakingDevice: TTTItem<CloakingDevice.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    templateItemStack = ItemStack(Resourcepack.Items.cloakingDevice).applyMeta {
        setDisplayName("${ChatColor.GRAY}${ChatColor.MAGIC}###${ChatColor.RESET}${ChatColor.GRAY} Cloaking Device ${ChatColor.MAGIC}###")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Macht dich unsichtbar"
        )
        addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    },
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL),
        buyLimit = 1,
        price = 2
    )
) {
    private const val WALK_SPEED_DECREASE = 0.1F
    private const val COOLDOWN = 10.0

    class Instance: TTTItem.Instance(CloakingDevice) {
        var enabled = false
        private var cooldownTask: BukkitTask? = null

        override fun onRightClick(event: ClickEvent) {
            if (cooldownTask == null) setEnabled(carrier!!, !enabled)
        }

        override fun onCarrierRemoved(oldCarrier: TTTPlayer) {
            setEnabled(oldCarrier, false)
        }

        private fun setEnabled(tttPlayer: TTTPlayer, value: Boolean) {
            if (value == enabled) return

            if (value) {
                tttPlayer.walkSpeed -= WALK_SPEED_DECREASE
                tttPlayer.player.apply {
                    isSprinting = false

                    addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 1000000, 0, false, false))
                    playSound(location, Resourcepack.Sounds.Item.CloakingDevice.on, SoundCategory.PLAYERS, 1F, 1F)
                }
            } else {
                tttPlayer.walkSpeed += WALK_SPEED_DECREASE
                tttPlayer.player.apply {
                    removePotionEffect(PotionEffectType.INVISIBILITY)
                    playSound(location, Resourcepack.Sounds.Item.CloakingDevice.off, SoundCategory.PLAYERS, 1F, 1F)
                }

                // TODO: Show progress in level bar
            }

            enabled = value
        }
    }

    override val listener = object : TTTItemListener<Instance>(CloakingDevice) {
        @EventHandler
        fun onPlayerToggleSprint(event: PlayerToggleSprintEvent) = handleWithInstance(event) { instance ->
            if (event.isSprinting && instance.enabled) event.isCancelled = true
        }
    }
}
