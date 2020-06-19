package de.moritzruth.spigot_ttt.game.items.impl

import de.moritzruth.spigot_ttt.Resourcepack
import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.corpses.TTTCorpse
import de.moritzruth.spigot_ttt.game.items.ClickEvent
import de.moritzruth.spigot_ttt.game.items.TTTItem
import de.moritzruth.spigot_ttt.game.items.TTTItemListener
import de.moritzruth.spigot_ttt.game.players.PlayerManager
import de.moritzruth.spigot_ttt.game.players.Role
import de.moritzruth.spigot_ttt.game.players.TTTPlayer
import de.moritzruth.spigot_ttt.game.players.roles
import de.moritzruth.spigot_ttt.plugin
import de.moritzruth.spigot_ttt.utils.applyMeta
import de.moritzruth.spigot_ttt.utils.createPlayerHeadInventory
import de.moritzruth.spigot_ttt.utils.hideInfo
import de.moritzruth.spigot_ttt.utils.sendActionBarMessage
import org.bukkit.ChatColor
import org.bukkit.SoundCategory
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

object FakeCorpse: TTTItem<FakeCorpse.Instance>(
    type = Type.SPECIAL,
    instanceType = Instance::class,
    shopInfo = ShopInfo(
        buyableBy = roles(Role.TRAITOR, Role.JACKAL),
        buyLimit = 3,
        price = 2
    ),
    templateItemStack = ItemStack(Resourcepack.Items.fakeCorpse).applyMeta {
        setDisplayName("${ChatColor.YELLOW}${ChatColor.BOLD}Fake-Leiche")
        lore = listOf(
            "",
            "${ChatColor.GOLD}Spawnt eine Fake-Leiche",
            "${ChatColor.GOLD}Rolle und Spieler auswählbar"
        )
        hideInfo()
    }
) {
    private val INVENTORY_TITLE = "${ChatColor.RED}${ChatColor.BOLD}Fake-Leiche"

    class Instance: TTTItem.Instance(FakeCorpse) {
        var chosenRole: Role? = null
        var choosePlayerInventory: Inventory? = null

        override fun onRightClick(event: ClickEvent) {
            carrier!!.player.openInventory(chooseRoleInventory)
        }
    }

    private val chooseRoleInventory = plugin.server.createInventory(
        null,
        InventoryType.HOPPER,
        "${INVENTORY_TITLE}${ChatColor.RESET} - Rolle"
    ).apply {
        addItem(*Role.values()
            .map {
                ItemStack(it.iconItemMaterial).applyMeta {
                    setDisplayName(it.coloredDisplayName)
                    hideInfo()
                }
            }
            .toTypedArray())
    }

    override val listener = object : TTTItemListener<Instance>(this) {
        @EventHandler
        fun onInventoryClick(event: InventoryClickEvent) = handle(event) { tttPlayer ->
            val instance = getInstance(tttPlayer) ?: return@handle

            if (
                !setOf(
                    instance.choosePlayerInventory,
                    chooseRoleInventory
                ).contains(event.clickedInventory)
            ) return@handle
            event.isCancelled = true

            val item = event.currentItem

            if (item != null && event.click == ClickType.LEFT) {
                when (event.clickedInventory) {
                    chooseRoleInventory -> {
                        instance.chosenRole = Role.values()[event.slot]
                        val choosePlayerInventory = createPlayerHeadInventory(
                            "${INVENTORY_TITLE}${ChatColor.RESET} - Spieler",
                            PlayerManager.tttPlayers.map { it.player }
                        )
                        instance.choosePlayerInventory = choosePlayerInventory
                        tttPlayer.player.openInventory(choosePlayerInventory)
                    }
                    instance.choosePlayerInventory -> {
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

                            TTTCorpse.spawnFake(
                                instance.chosenRole!!,
                                corpseTTTPlayer,
                                tttPlayer,
                                tttPlayer.player.location
                            )

                            tttPlayer.removeItem(FakeCorpse)
                        }
                    }
                }
            }
        }
    }
}
