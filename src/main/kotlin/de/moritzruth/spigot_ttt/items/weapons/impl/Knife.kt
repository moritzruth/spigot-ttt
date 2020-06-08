package de.moritzruth.spigot_ttt.items.weapons.impl

import de.moritzruth.spigot_ttt.ResourcePack
import de.moritzruth.spigot_ttt.game.players.*
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.isRelevant
import de.moritzruth.spigot_ttt.items.weapons.LoreHelper
import de.moritzruth.spigot_ttt.utils.*
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
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

    override val listener = object : Listener {
        @EventHandler
        fun onPlayerInteract(event: PlayerInteractEvent) {
            if (!event.isRelevant(Knife)) return

            if (event.action.isLeftClick) {
                event.player.inventory.clearHeldItemSlot()
                event.player.playSound(event.player.location, Sound.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1F, 1F)
            }
        }

        @EventHandler(ignoreCancelled = true)
        fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
            if (!event.isRelevant(Knife)) return

            val damagerTTTPlayer = PlayerManager.getTTTPlayer(event.damager as Player) ?: return
            val damagedTTTPlayer = PlayerManager.getTTTPlayer(event.entity as Player) ?: return

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


