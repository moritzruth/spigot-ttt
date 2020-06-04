package de.moritzruth.spigot_ttt.items.weapons.guns.deagle

import de.moritzruth.spigot_ttt.CustomItems
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.DETECTIVE
import de.moritzruth.spigot_ttt.game.players.TTTPlayer.Role.TRAITOR
import de.moritzruth.spigot_ttt.items.Buyable
import de.moritzruth.spigot_ttt.items.TTTItem
import de.moritzruth.spigot_ttt.items.weapons.guns.Gun
import org.bukkit.ChatColor
import java.util.*

object GoldenDeagle: Gun(
    stateClass = State::class,
    displayName = "${ChatColor.GOLD}${ChatColor.BOLD}Golden Deagle",
    damage = INFINITE_DAMAGE,
    cooldown = 1.4,
    magazineSize = 2,
    reloadTime = 10.0,
    itemMaterial = CustomItems.goldenDeagle,
    recoil = 10
), Buyable {
    override val buyableBy = EnumSet.of(TRAITOR, DETECTIVE)
    override val price = 3
    override val type = TTTItem.Type.PISTOL_LIKE

    class State: Gun.State(magazineSize)
}


