package de.lolsu.gmfw.api;

import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.main.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Handles a scoreboard for you in a more simple manner.
 * */
public class SimpleScoreBoard {

    public Player currentPlayer;

    private final String title;
    private final List<SSBEntry> scoreboard;
    private final Map<Player, Scoreboard> scoreboards;

    private int taskId = -1;

    public SimpleScoreBoard(String title) {
        this.title = title;
        this.scoreboard = new ArrayList<>();
        this.scoreboards = new HashMap<>();
    }

    private Scoreboard getCopy() {
        Scoreboard board = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
        Objective sidebar = board.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
        sidebar.setDisplayName(title);

        int index = 95;
        int safe = 0;
        for(SSBEntry entry : scoreboard) {
            switch (entry.type) {
                case 0x00: // Space
                    sidebar.getScore("§" + safe).setScore(index);
                    safe++;
                    break;

                case 0x01: // Line
                    sidebar.getScore(entry.a).setScore(index);
                    break;

                case 0x02: // Team
                    Team team = board.registerNewTeam(entry.a);
                    team.setPrefix(entry.b);
                    team.addEntry("§" + safe);
                    sidebar.getScore("§" + safe).setScore(index);
                    safe++;
                    break;
            }
            index -= 5;
        }

        return board;
    }

    /**
     * Adds a line to the scoreboard.<br>
     * @param line The line to be added
     * @return The current instance of the {@code SimpleScoreBoard} so you can immediately continue operating
     * */
    public SimpleScoreBoard addLine(String line) {
        scoreboard.add(new SSBEntry(line));
        return this;
    }

    public SimpleScoreBoard addVariable(String variableName) {
        scoreboard.add(new SSBEntry(variableName, ""));
        return this;
    }

    /**
     * Adds a variable (team) to the scoreboard.<br>
     * You need to update the variables for each player individually using the function called by the updater and the {@link #currentPlayer}
     * @param variableName The unique name of your variable (team)
     * @param prefix The constant prefix of the variable (team)
     * @return The current instance of the {@code SimpleScoreBoard} so you can immediately continue operating
     * */
    public SimpleScoreBoard addVariable(String variableName, String prefix) {
        scoreboard.add(new SSBEntry(variableName, prefix));
        return this;
    }

    public SimpleScoreBoard addSpace() {
        scoreboard.add(new SSBEntry());
        return this;
    }

    /**
     * Adds a player to the scoreboard.<br>
     * Players are automatically removed if they disconnect.
     * @param player The player to whom the scoreboard should also be sent
     * */
    public void addPlayer(Player player) {
        Scoreboard board = getCopy();
        player.setScoreboard(board);
        scoreboards.put(player, board);
    }

    /**
     * Same as {@link #addPlayer(Player)} but with multiple players
     * @param players The collection of players to add
     * */
    public void addPlayers(Collection<? extends Player> players) {
        for(Player player : players)
            addPlayer(player);
    }

    /**
     * Removes a player from the scoreboard.<br>
     * Automatically executed when a player disconnects.
     * @param player The player who shouldn't see the scoreboard anymore
     * */
    public void removePlayer(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        scoreboards.remove(player);
    }


    /**
     * Updates every players scoreboard every second.<br>
     * Can only be called once and ignores every call after that.<br>
     * To cancel the updater use {@link #stopUpdater()}
     * @param variableGetter Your function for getting the updated variables of each player using {@link #currentPlayer the current player}
     * */
    public void startUpdater(Callable<HashMap<String, String>> variableGetter) {
        if(taskId != -1 && (Bukkit.getScheduler().isCurrentlyRunning(taskId) || Bukkit.getScheduler().isQueued(taskId))) return;

        taskId = new BukkitRunnable() {
            @Override public void run() {
                List<Player> toRemove = new ArrayList<>();
                for(Player p : scoreboards.keySet()) {
                    if(!p.isOnline()) { toRemove.add(p); continue; }

                    currentPlayer = p;

                    HashMap<String, String> vars;
                    try {
                        vars = variableGetter.call();
                    } catch (Exception exception) {
                        vars = new HashMap<>();
                        Logger.error("Couldn't execute callable used to retrieve variables for scoreboard", exception);
                    }

                    for(Map.Entry<String, String> var : vars.entrySet())
                        scoreboards.get(p).getTeam(var.getKey()).setSuffix(var.getValue());
                }
                for(Player p : toRemove)
                    scoreboards.remove(p);
            }
        }.runTaskTimer(Gamemode.getPlugin(), 0L, 20L).getTaskId();
    }

    /**
     * Cancels the updater and therefore freezes the scoreboard of all assigned players.
     * Functions as a sort of destroy for the scoreboard.
     * */
    public void stopUpdater() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    private class SSBEntry {
        public byte type;
        public String a;
        public String b;

        /** Space */
        public SSBEntry() {
            type = 0x00;
        }

        /** Line */
        public SSBEntry(String line) {
            type = 0x01;
            a = line;
        }

        /** Team */
        public SSBEntry(String name, String prefix) {
            type = 0x02;
            a = name;
            b = prefix;
        }
    }

}