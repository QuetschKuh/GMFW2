package de.lolsu.gmfw.players;

import de.lolsu.gmfw.main.Gamemode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Team {

    public final int id;
    private List<Player> players;
    private List<Player> playersAlive;

    public Team(int id) {
        this.id = id;
        this.players = new ArrayList<>();
        this.playersAlive = new ArrayList<>();
    }

    /*
    * Getters
    * */

    /** The amount of players in the team */
    public int size() { return players.size(); }
    /** The amount of live players in the team */
    public int alive() { return playersAlive.size(); }
    /** Whether this team contains no players at all */
    public boolean isEmpty() { return players.isEmpty(); }
    /** Whether this team contains live players */
    public boolean isAlive() { return !playersAlive.isEmpty(); }
    /** Whether all players in ths team are dead */
    public boolean isDead() { return playersAlive.isEmpty(); }
    /** Whether the team can accept more players */
    public boolean isFull() { return players.size() >= Gamemode.getPlayerMan().maxPerTeam; }
    /** Whether the team contains the given player */
    public boolean contains(Player player) { return players.contains(player); }
    /** Retrieves a list of all player names in the team */
    public List<String> names() { return players.stream().map(Player::getDisplayName).collect(Collectors.toList()); }
    /** Retrieves a list of all player names in the team with the given prefix prepended to them */
    public List<String> names(String prefix) { return players.stream().map(p -> prefix + p.getDisplayName()).collect(Collectors.toList()); }
    /** Retrieves a copy of the players registered to the team */
    public List<Player> getPlayers() { return new ArrayList<>(players); }


    /*
    * Modifiers
    * */

    /** Instantiates the live player list with the current players in the team */
    public void initialize() {

    }

    /**
     * Adds the given player to the team
     * @return false if the team is already full, true if the player was successfully added
     * */
    public boolean addPlayer(Player player) {
        if(isFull()) return false;
        players.add(player);
        playersAlive.add(player);
        return true;
    }

    /**
     * Adds multiple players to the team
     * @return false if the team doesn't have enough space for that many players, true if the players were successfully added
     * */
    public boolean addPlayers(Player... players) {
        if(size() + players.length >= Gamemode.getPlayerMan().maxPerTeam) return false;
        this.players.addAll(Arrays.asList(players));
        this.playersAlive.addAll(Arrays.asList(players));
        return true;
    }

    /** Removes a player from the team */
    public void removePlayer(Player player) {
        players.remove(player);
        playersAlive.remove(player);
    }

    /** Removes all given players from the team */
    public void removePlayers(Player... players) {
        List<Player> toRemove = Arrays.asList(players);
        this.players.removeAll(toRemove);
        this.playersAlive.removeAll(toRemove);
    }

    /** Removes a player from the live player list */
    public void killPlayer(Player p) {
        playersAlive.remove(p);
    }

    /** Teleports all alive players to the given location */
    public void teleportAll(Location location) {
        for(Player p : playersAlive) p.teleport(location);
    }

}
