package de.lolsu.gmfw.managers;

import de.lolsu.gmfw.api.Chars;
import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.config.ConfVar;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.netty.PacketReader;
import de.lolsu.gmfw.phases.MapHandler;
import de.lolsu.gmfw.players.Team;
import de.lolsu.gmfw.statistics.StatManager;
import de.lolsu.gmfw.statistics.Stats;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerManager {

    /** The maximum amount of players allowed on the server */
    public int maxPlayers;
    /** The maximum amount of players allowed per team */
    public int maxPerTeam;
    /** The amount of teams in total */
    public int teamAmount;

    /**  */
    private List<Player> players = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();

    public Map<Player, Team> playerTeamMap = new HashMap<>();
    private List<Team> teams = new ArrayList<>();

    /** A map containing all players and their respective {@link PacketReader} used for custom events */
    private Map<Player, PacketReader> packetReaderMap = new HashMap<>();


    /** A map containing all players and the player that last damaged them. Used to make up for spigots flawed .getKiller() */
    public Map<Player, Player> lastDamager = new HashMap<>();
    /** A map containing all players and the time they last got damaged. Used to make up for spigots flawed .getKiller() */
    public Map<Player, Long> lastTimeDamaged = new HashMap<>();

    public PlayerManager() {
        // Calculate variables
        maxPlayers = Gamemode.getConfig().getInt("Size.Total", 12);
        maxPerTeam = Gamemode.getConfig().getInt("Size.Team", 2);
        teamAmount = (int) Math.ceil((double) maxPlayers / maxPerTeam);

        // Init teams
        for(int i = 0; i < teamAmount; i++) teams.add(new Team(i));
    }

    /* *********** *
     * * Getters * *
     * *********** */

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    public List<Player> getSpectators() {
        return new ArrayList<>(spectators);
    }
    public int getPlayersAmount() {
        return getPlayers().size();
    }
    public boolean isLobbyFull() {
        return getPlayersAmount() >= maxPlayers;
    }

    /** Inverse of {@link #isSpectator(Player)}. Checks if the player isn't a spectator */
    public boolean isAlive(Player player) {
        return !spectators.contains(player);
    }
    /** Inverse of {@link #isAlive(Player)}. Checks if the player is a spectator */
    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    /* ************* *
     * * Modifiers * *
     * ************* */

    public interface PlayerModifier {
        void run(Player player);
    }

    /**
     * Runs the given function for all online players<br>
     * Example using lambda: {@code PlayerManager.forAll(player -> player.setGameMode(GameMode.SURVIVAL));}
     * */
    public static void forAll(PlayerModifier function) {
        for(Player player : Bukkit.getOnlinePlayers())
            function.run(player);
    }

    /**
     * Registers a player
     * */
    public void register(Player player) {

        // Load stats
        StatManager.loadFromFile(player);

        // Packet reader
        injectPacketReader(player);

        Gamemode.getScoreBoard().addPlayer(player);

        players.add(player);
    }

    /**
     * Unregisters a player
     * */
    public void unregister(Player player) {

        // Save stats
        StatManager.saveAndRemove(player);

        // Packet reader
        removePacketReader(player);

        players.remove(player);
    }

    /**
     * Puts a player into a custom spectator mode.<br>
     * In this mode players are technically in adventure, but hidden from all non-spectators, able to fly, and unable to interact with anything.<br>
     * They also get a few spectator items, such as a slimeball to leave the game
     * */
    public void spec(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        spectators.add(player);
        updateSpecVisibility();
    }

    /** Undoes {@link #spec(Player)} */
    public void unspec(Player player) {
        player.setGameMode(ConfVar.MAP_GAMEMODE.getObject(GameMode.class));
        player.setFlying(false);
        player.setAllowFlight(false);
        spectators.remove(player);
        updateSpecVisibility();
    }

    /** Hides all spectators from other players and shows all non-spectators to other players. */
    public void updateSpecVisibility() {
        for(Player p0 : Bukkit.getOnlinePlayers()) {
            if(spectators.contains(p0)) PlayerManager.forAll(p1 -> p1.hidePlayer(p0));
            else PlayerManager.forAll(p1 -> p1.showPlayer(p0));
        }
    }

    public void kill(Player player) {
        kill(player, getKiller(player));
    }

    /**
     * Kills a player, removing them from the game forever
     * */
    public void kill(Player player, Player killer) {
        // Drop player inventory and play effects (before we reset the player)
        dropInventory(player);
        player.getWorld().strikeLightningEffect(player.getLocation());

        // Reset and teleport the player
        reset(player);
        player.teleport(MapHandler.getConfig().getLocation("Spawns.Spec", Bukkit.getWorld(MapHandler.getMap())));
        spec(player);

        // Kill from team
        playerTeamMap.get(player).killPlayer(player);

        // Send titlebar, avoid overwriting player won titlebar by checking for state
        if(GamestateManager.state == GamestateManager.State.INGAME)
            Messenger.sendTitleBar(player, ChatColor.RED + "You died.", ChatColor.GOLD + Chars.shrug, 5, 15, 20);

        // Update statistics and broadcast message
        if(killer != null) {
            Messenger.broadcastNoPre(ChatColor.GOLD + player.getDisplayName() + ChatColor.RESET + " got obliterated by " + ChatColor.RED + killer.getDisplayName());
            StatManager.increase(killer, Stats.KILLS);
        } else {
            Messenger.broadcastNoPre(ChatColor.GOLD + player.getDisplayName() + ChatColor.RED + " died.");
        }
        StatManager.increase(player, Stats.DEATHS);

        if(getTeamsAlive().size() <= 1) Gamemode.getStateMan().onGameEnd();
    }

    /**
     * Basically death but without changing location of the player or dropping their inventory
     * */
    public void reset(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        for(PotionEffect pe : player.getActivePotionEffects()) player.removePotionEffect(pe.getType());
        player.setFireTicks(0);

        player.setMaxHealth(20);
        player.setSaturation(20);
        player.setHealth(20);

        player.setExp(0);
        player.setLevel(0);
    }

    /**
     * Basically death but with 'keep inventory: true'
     * */
    public void softReset(Player player) {
        for(PotionEffect pe : player.getActivePotionEffects()) player.removePotionEffect(pe.getType());
        player.setFireTicks(0);

        player.setMaxHealth(20);
        player.setSaturation(20);
        player.setHealth(20);
    }

    /* ******************************* *
     * * Protected/Private functions * *
     * ******************************* */

    /**
     * Injects a player with a {@link PacketReader packet reader} and adds it to the map.<br>
     * This is a separate function to allow easy overriding in case of more custom events/a custom PacketReader class.
     * */
    protected void injectPacketReader(Player player) {
        packetReaderMap.put(player, new PacketReader(player));
    }

    /**
     * Uninjects a player from their {@link PacketReader packet reader} and removes it from the map.<br>
     * This is a separate function to allow easy overriding in case of more custom events/a custom PacketReader class.
     * */
    protected void removePacketReader(Player player) {
        packetReaderMap.remove(player).uninject();
    }

    /**
     * Gets the killer of a certain player based on the lastDamager and lastTimeDamaged HashMaps
     * @return null if the last time the given player was damages is longer ago than 30 seconds, otherwise the last player that damaged the given player
     * */
    protected Player getKiller(Player player) {
        return (lastDamager.containsKey(player) && lastDamager.get(player) != player && System.currentTimeMillis() < lastTimeDamaged.get(player) + 30000) ? lastDamager.get(player) : null;
    }

    protected void dropInventory(Player player) {
        boolean dropAll = ConfVar.DEATH_DROPS_ALL.getState();
        ArrayList<Material> whitelist = (ArrayList<Material>) ConfVar.DEATH_DROPS_WHITELIST.getObject();
        if(dropAll || !whitelist.isEmpty())
            for(ItemStack i : player.getInventory().getContents())
                if(i != null && (dropAll || whitelist.contains(i.getType()))) player.getWorld().dropItemNaturally(player.getLocation(), i);
    }

    /*
    * Team Stuff
    * */

    /** Gets the team a given player is currently in */
    public Team getTeam(Player player) {
        return playerTeamMap.get(player);
    }

    /** Returns a copy of the registered teams */
    public List<Team> getTeams() { return new ArrayList<>(teams); }

    /** Returns a list with all currently alive teams */
    public List<Team> getTeamsAlive() {
        return teams.stream().filter(Team::isAlive).collect(Collectors.toList());
    }

    /** Gets the amount of teams in the game */
    public int getTeamsAmount() { return teams.size(); }

    /** Adds the given player to the team with the given id, removes them from the old team if needed */
    public boolean addToTeam(int team, Player player) {
        return addToTeam(teams.get(team), player);
    }

    /** Adds the given player to the given team, removes them from the old team if needed */
    public boolean addToTeam(Team team, Player player) {
        // Only if team isn't full or sum
        if(team.addPlayer(player)) {
            // Remove from old team
            if(playerTeamMap.containsKey(player))
                playerTeamMap.get(player).removePlayer(player);
            // Update map
            playerTeamMap.put(player, team);
            return true;
        }

        // Couldn't add player to team
        return false;
    }

    /** Adds the given players to the given team, removes them from the old team if needed */
    public boolean addToTeam(Team team, Player... players) {
        // Remove from previous teams
        for(Player player : players)
            if(playerTeamMap.containsKey(player))
                playerTeamMap.get(player).removePlayer(player);

        // Add to new team
        if(team.addPlayers(players)) {
            for (Player player : players) playerTeamMap.put(player, team);
            return true;
        }

        return false;
    }

    public void initializeTeams() {
        for(Team t : teams) t.initialize();
    }

    /** Goes through all players without a team and assigns them to the team with the least players */
    public void autoAssignTeams() {
        PlayerManager.forAll(p -> {
            if(playerTeamMap.containsKey(p)) return;
            addToTeam(teams.stream().min(Comparator.comparingInt(Team::size)).get(), p);
            if(maxPerTeam > 1) Messenger.sendLines(p, ChatColor.AQUA + "Since you haven't chosen a team,\n" + ChatColor.AQUA + "you have been automatically assigned to one.");
        });
    }

}
