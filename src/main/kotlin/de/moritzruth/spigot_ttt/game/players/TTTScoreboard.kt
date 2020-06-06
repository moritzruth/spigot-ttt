package de.moritzruth.spigot_ttt.game.players

import de.moritzruth.spigot_ttt.game.GameManager
import de.moritzruth.spigot_ttt.game.GamePhase
import de.moritzruth.spigot_ttt.game.Timers
import de.moritzruth.spigot_ttt.plugin
import org.bukkit.ChatColor
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.RenderType
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

class TTTScoreboard(private val tttPlayer: TTTPlayer) {
    val scoreboard: Scoreboard = plugin.server.scoreboardManager!!.newScoreboard

    fun initialize() {
        scoreboard.registerNewTeam(ValueTeam.ROLE.teamName).addEntry(ValueTeam.ROLE.entry)
        scoreboard.registerNewTeam(ValueTeam.PHASE_AND_TIME.teamName).addEntry(ValueTeam.PHASE_AND_TIME.entry)
        scoreboard.registerNewTeam(ValueTeam.CREDITS.teamName).addEntry(ValueTeam.CREDITS.entry)

        scoreboard.registerNewObjective(
                INACTIVE_OBJECTIVE,
                "dummy",
                "${ChatColor.GOLD}TTT",
                RenderType.INTEGER
        ).apply {
            val lines = mutableListOf(
                    " ".repeat(20),
                    "${ChatColor.GRAY}Inaktiv",
                    " "
            )

            lines.reversed().forEachIndexed { index, line -> getScore(line).score = index }
        }

        scoreboard.registerNewObjective(
                ACTIVE_OBJECTIVE,
                "dummy",
                "${ChatColor.GOLD}TTT",
                RenderType.INTEGER
        ).apply {
            val lines = mutableListOf(
                    " ".repeat(20),
                    ValueTeam.PHASE_AND_TIME.entry,
                    "  ",
                    ValueTeam.ROLE.entry,
                    " "
            )

            lines.reversed().forEachIndexed { index, line -> getScore(line).score = index }
        }

        scoreboard.registerNewObjective(
                ACTIVE_WITH_CREDITS_OBJECTIVE,
        "dummy",
        "${ChatColor.GOLD}TTT",
        RenderType.INTEGER
        ).apply {
            val lines = mutableListOf(
                " ".repeat(20),
                ValueTeam.PHASE_AND_TIME.entry,
                "  ",
                ValueTeam.ROLE.entry,
                ValueTeam.CREDITS.entry,
                " "
            )

            lines.reversed().forEachIndexed { index, line -> getScore(line).score = index }
        }

        scoreboard.registerNewTeam(SPECIAL_TEAM_NAME).apply {
            setAllowFriendlyFire(true)
            setCanSeeFriendlyInvisibles(true)
            setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS)
        }

        scoreboard.registerNewTeam(DEFAULT_TEAM_NAME).apply {
            setAllowFriendlyFire(true)
            setCanSeeFriendlyInvisibles(false)
            setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.ALWAYS)
            setOption(Team.Option.DEATH_MESSAGE_VISIBILITY, Team.OptionStatus.NEVER)
            setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
        }

        updateEverything()
        showCorrectSidebarScoreboard()
    }

    fun updatePhaseAndTime() {
        val phase = GameManager.phase

        if (phase === null) {
            setValue(ValueTeam.PHASE_AND_TIME, "Inaktiv")
        } else {
            val remainingSeconds = Timers.remainingSeconds
            val seconds = remainingSeconds % 60
            val minutes = (remainingSeconds - seconds) / 60
            val minutesString = minutes.toString().padStart(2, '0')
            val secondsString = seconds.toString().padStart(2, '0')

            setValue(ValueTeam.PHASE_AND_TIME, "${ChatColor.GOLD}${phase.displayName}: ${ChatColor.WHITE}$minutesString:$secondsString")
        }
    }

    fun updateRole() {
        when (GameManager.phase) {
            null -> setValue(ValueTeam.ROLE, "")
            GamePhase.PREPARING -> setValue(ValueTeam.ROLE, "Du bist: ${ChatColor.MAGIC}xxxxxxxx")
            GamePhase.COMBAT, GamePhase.OVER -> setValue(ValueTeam.ROLE, "Du bist: " + tttPlayer.role.coloredDisplayName.toUpperCase())
        }
    }

    fun updateTeams() {
        val defaultTeam = scoreboard.getTeam(DEFAULT_TEAM_NAME)!!
        val phase = GameManager.phase

        if (phase === GamePhase.COMBAT) {
            if (tttPlayer.role.group == RoleGroup.JACKAL || tttPlayer.role === Role.TRAITOR) {
                val specialTeam = scoreboard.getTeam(SPECIAL_TEAM_NAME)!!
                specialTeam.color = tttPlayer.role.chatColor
                defaultTeam.color = Role.INNOCENT.chatColor

                PlayerManager.tttPlayers.forEach {
                    if (RoleGroup.JACKAL.bothAre(tttPlayer, it) ||
                        RoleGroup.TRAITOR.bothAre(tttPlayer, it)) {
                        specialTeam.addEntry(it.player.displayName)
                    } else {
                        defaultTeam.addEntry(it.player.displayName)
                    }
                }

                return
            }
        }

        defaultTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, if (phase === null) Team.OptionStatus.ALWAYS else Team.OptionStatus.NEVER)

        PlayerManager.tttPlayers.forEach {
            defaultTeam.addEntry(it.player.displayName)
        }
    }

    fun updateCredits() {
        setValue(ValueTeam.CREDITS, "${ChatColor.GREEN}Credits: ${ChatColor.WHITE}${tttPlayer.credits}")
    }

    fun updateEverything() {
        updatePhaseAndTime()
        updateRole()
        updateTeams()
        updateCredits()
    }

    fun showCorrectSidebarScoreboard() {
        setSidebar(when {
            GameManager.phase === null -> INACTIVE_OBJECTIVE
            GameManager.phase !== GamePhase.PREPARING && tttPlayer.role.canOwnCredits -> ACTIVE_WITH_CREDITS_OBJECTIVE
            else -> ACTIVE_OBJECTIVE
        })
    }

    private fun setSidebar(objectiveName: String) {
        scoreboard.getObjective(objectiveName)!!.displaySlot = DisplaySlot.SIDEBAR
    }

    private fun setValue(valueTeam: ValueTeam, value: String) {
        scoreboard.getTeam(valueTeam.teamName)!!.prefix = value
    }

    private enum class ValueTeam(val teamName: String, val entry: String) {
        PHASE_AND_TIME("_phase-and-time", "${ChatColor.AQUA}"),
        ROLE("_role", "${ChatColor.BLACK}"),
        CREDITS("_credits", "${ChatColor.GOLD}")
    }

    companion object {
        private const val SPECIAL_TEAM_NAME = "special"
        private const val DEFAULT_TEAM_NAME = "default"
        private const val INACTIVE_OBJECTIVE = "1"
        private const val ACTIVE_OBJECTIVE = "2"
        private const val ACTIVE_WITH_CREDITS_OBJECTIVE = "3"
    }
}
