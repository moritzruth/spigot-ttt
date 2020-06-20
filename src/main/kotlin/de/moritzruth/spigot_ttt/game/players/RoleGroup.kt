package de.moritzruth.spigot_ttt.game.players

import java.util.*

enum class RoleGroup(
    val primaryRole: Role,
    val additionalRoles: EnumSet<Role> = EnumSet.noneOf(Role::class.java),
    val canUseTeamChat: Boolean,
    val knowEachOther: Boolean
) {
    INNOCENT(Role.INNOCENT, EnumSet.of(Role.DETECTIVE), canUseTeamChat = false, knowEachOther = false),
    JACKAL(Role.JACKAL, EnumSet.of(Role.SIDEKICK), canUseTeamChat = true, knowEachOther = true),
    TRAITOR(Role.TRAITOR, canUseTeamChat = true, knowEachOther = true);

    fun bothAre(firstTTTPlayer: TTTPlayer, secondTTTPlayer: TTTPlayer) =
        firstTTTPlayer.role.group == this && secondTTTPlayer.role.group == this

    companion object {
        fun getGroupOf(role: Role) = values().find { it.primaryRole == role || it.additionalRoles.contains(role) }!!
    }
}
