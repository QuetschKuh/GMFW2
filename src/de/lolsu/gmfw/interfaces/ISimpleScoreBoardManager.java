package de.lolsu.gmfw.interfaces;

import de.lolsu.gmfw.api.SimpleScoreBoard;
import org.bukkit.entity.Player;

import java.util.HashMap;

public abstract class ISimpleScoreBoardManager {

    public SimpleScoreBoard scoreboard;

    public abstract void createScoreboard();

    public void addPlayer(Player player) { scoreboard.addPlayer(player); }
    public void removePlayer(Player player) { scoreboard.removePlayer(player); }

    public abstract HashMap<String, String> getVariables();

}
