package de.moritzruth.spigot_ttt.items.weapons.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.DamageInfo
import de.moritzruth.spigot_ttt.game.players.DeathReason
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.LoreHelper
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.clearHeldItemSlot
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.removeTTTItemNextTick
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object Knife: TTTItem, Buyable {
    override val itemStack = ItemStack(ResourcePack.Items.knife).applyMeta {
        setDisplayName("${ChatColor.RED}${ChatColor.BOLD}Knife")
        lore = listOf(
            "",
            "${ChatColor.GRAY}Schaden: ${LoreHelper.damage(null)}",
            "",
            "${ChatColor.RED}Nur einmal verwendbar",
            "${ChatColor.RED}Nur aus nächster Nähe"
        )
        hideInfo()
    }
    override val buyableBy = roles(Role.TRAITOR, Role.JACKAL)
    override val price = 1
    override val type = TTTItem.Type.MELEE
    override val buyLimit = 1

    override val listener = object : TTTItemListener(this, false) {
        override fun onLeftClick(data: Data<PlayerInteractEvent>) {
            data.event.player.inventory.clearHeldItemSlot()
            data.event.player.playSound(data.event.player.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1F, 1F)
        }

        @EventHandler(ignoreCancelled = true)
        override fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) = handle(event) { damagerTTTPlayer, damagedTTTPlayer ->
            val distance = damagerTTTPlayer.player.location.distance(damagedTTTPlayer.player.location)

            if (distance > 1.5) event.isCancelled = true else {
                damagedTTTPlayer.damageInfo = DamageInfo(damagerTTTPlayer, DeathReason.Item(Knife))
                event.damage = 1000.0

                damagerTTTPlayer.player.playSound(damagerTTTPlayer.player.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1F, 1F)
                damagerTTTPlayer.player.inventory.removeTTTItemNextTick(Knife)
            }
        }
    }
}


