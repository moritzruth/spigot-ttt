package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.corpses.CorpseClickEvent
import de.moritzruth.spigot_ttt.game.corpses.TTTCorpse
import de.moritzruth.spigot_ttt.game.items.ClickEvent
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.*
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object BoomBody: TTTItem<BoomBody.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL),
        buyLimit = 1,
        price = 2
    ),
    templateItemStack = ItemStack(Resourcepack.Items.boomBody).applyMeta {
        setDisplayName("${ChatColor.DARK_RED}${ChatColor.BOLD}Boom Body")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Spawnt eine Fake-Leiche, die ",
            "${ChatColor.GOLD}explodiert, wenn sie identifiziert wird.",
            "${ChatColor.GOLD}Rolle und Spieler auswählbar"
        )
        hideInfo()
    }
) {
    class Instance: TTTItem.Instance(BoomBody) {
        var choosePlayerInventory: Inventory? = null

        override fun onRightClick(event: ClickEvent) {
            choosePlayerInventory = createPlayerHeadInventory(
                "${ChatColor.RED}${ChatColor.BOLD}Boom Body - ${ChatColor.RESET}Spieler",
                PlayerManager.tttPlayers.map { it.player }
            ).also { carrier!!.player.openInventory(it) }
        }
    }

    val boomBodies = mutableSetOf<TTTCorpse>()

    override val listener = object : TTTItemListener<Instance>(this) {
        @EventHandler
        fun onCorpseClick(event: CorpseClickEvent) {
            if (boomBodies.contains(event.tttCorpse)) {
                boomBodies.remove(event.tttCorpse)
                event.isCancelled = true
                createKillExplosion(event.tttCorpse.spawnedBy!!, event.tttCorpse.location, 5.0)
            }
        }

        @EventHandler
        fun onInventoryClick(event: InventoryClickEvent) = handle(event) { tttPlayer ->
            val instance = getInstance(tttPlayer) ?: return@handle
            if (event.clickedInventory !== instance.choosePlayerInventory) return@handle

            event.isCancelled = true

            val item = event.currentItem
            if (item != null && event.click == ClickType.LEFT) {
                tttPlayer.player.closeInventory()

                val corpsePlayer = plugin.server.getPlayer((item.itemMeta as SkullMeta).owningPlayer!!.uniqueId)!!
                val corpseTTTPlayer = TTTPlayer.of(corpsePlayer)

                if (corpseTTTPlayer == null) {
                    tttPlayer.player.sendActionBarMessage("${ChatColor.RED}Das hat nicht funktioniert")
                } else {
                    GameManager.world.playSound(
                        tttPlayer.player.location,
                        Resourcepack.Sounds.playerDeath,
                        SoundCategory.PLAYERS,
                        1F,
                        1F
                    )

                    boomBodies.add(TTTCorpse.spawnFake(
                        Role.INNOCENT,
                        corpseTTTPlayer,
                        tttPlayer,
                        tttPlayer.player.location
                    ))

                    tttPlayer.removeItem(BoomBody)
                }
            }
        }
    }
}
