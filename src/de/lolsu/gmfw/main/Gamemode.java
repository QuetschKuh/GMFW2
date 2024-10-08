package de.lolsu.gmfw.main;

import de.lolsu.gmfw.admin.AdminCommands;
import de.lolsu.gmfw.admin.AdminEvents;
import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.commands.GetMap;
import de.lolsu.gmfw.config.Config;
import de.lolsu.gmfw.config.ConfigReader;
import de.lolsu.gmfw.interfaces.ISimpleScoreBoardManager;
import de.lolsu.gmfw.managers.GamestateManager;
import de.lolsu.gmfw.managers.PlayerManager;
import de.lolsu.gmfw.managers.ScoreboardManager;
import de.lolsu.gmfw.phases.MapHandler;
import de.lolsu.gmfw.statistics.StatManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Random;

public class Gamemode {

    private static Gamemode instance;
    private final JavaPlugin plugin;

    private Config general;
    private ConfigReader lang;

    public static final Random random = new Random();

    public final boolean setupMode;

    /** The name of the game (e.g. lolsuSW for SkyWars on lolsu.de, but can be the same as {@link #nameGamemode}) */
    public final String name;
    /** The name of the server (e.g. lolsu.de) */
    public final String nameServer;
    /** The name of just the gamemode (e.g. SkyWars) */
    public final String nameGamemode;

    public final World lobbyWorld;
    public final Location spawnLocation;

    public Gamemode(JavaPlugin plugin) {
        if(instance != null) Logger.error("Multiple instances of Gamemode. Please refrain from initializing Gamemode twice or more.", new InstantiationException());

        this.plugin = plugin;
        instance = this;

        setupMode = plugin.getConfig().getBoolean("setupmode", true);

        // Create lobby world
        if(Bukkit.getWorld("Lobby") == null) {
            if(!new File("Lobby").exists()) lobbyWorld = MapHandler.createEmpty("Lobby");
            else lobbyWorld = MapHandler.loadWorld("Lobby");
        } else lobbyWorld = Bukkit.getWorld("Lobby");

        // Validate plugin config
        if(!plugin.getConfig().contains("path_config") || !plugin.getConfig().contains("path_lang") || !plugin.getConfig().contains("path_maps")) {
            plugin.getConfig().set("path_config", "../gm_config.yml");
            plugin.getConfig().set("path_lang", "../gm_lang.yml");
            plugin.getConfig().set("path_maps", "../maps/");
            plugin.saveConfig();
        }

        // Get other config stuff
        String pathGeneral = plugin.getConfig().getString("path_config", "../gm_config.yml");
        String pathLang = plugin.getConfig().getString("path_lang", "../gm_lang.yml");
        String pathMaps = plugin.getConfig().getString("path_maps", "../maps/");
        StatManager.setStatFolderLocation(plugin.getConfig().getString("path_stats", "../stats/"));
        general = new Config(pathGeneral, plugin.getResource("gm_config.yml"));
        lang = new ConfigReader(pathLang, plugin.getResource("gm_lang.yml"));

        name = lang.getString("Names.Name", "Name");
        nameServer = lang.getString("Names.Server", "Server");
        nameGamemode = lang.getString("Names.Gamemode", "Gamemode");

        spawnLocation = general.getLocation("Lobby.Spawn", lobbyWorld);

        // Initialize static/utility classes
        MapHandler.setMapDirectory(pathMaps);
        MapHandler.cleanup();
        Messenger.setPrefix(name);
        Messenger.setPrefixSetup(ChatColor.RED + "GMSETUP");
    }

    /**
     * Finalizes the game and initializes the lobby phase, only execute at the end of your {@code onEnable()} function, after you have initialized all else.
     * */
    public void start() {
        // Commands
        plugin.getCommand("gmsetup").setExecutor(new AdminCommands());
        plugin.getCommand("getmap").setExecutor(new GetMap());

        if(setupMode) {
            Bukkit.getPluginManager().registerEvents(new AdminEvents(), plugin);
            Logger.info("Started server in setup mode.");
            return;
        }

        // Managers
        if(playerManager == null) playerManager = new PlayerManager();
        if(gamestateManager == null) gamestateManager = new GamestateManager();
        if(scoreboard == null) scoreboard = new ScoreboardManager();

        gamestateManager.onStart();
    }

    /**
     * Saves configurations and cleans up any temporary states of the game (although a cleanup is run on startup too)
     * */
    public void disable() {
        if(MapHandler.getMap() != null)
            MapHandler.unloadMap();
        general.save();
    }

    public static Gamemode getInstance() {
        if(instance == null) Logger.error("Tried to get Gamemode instance but is null.\nPlease initialize Gamemode first by using 'new Gamemode(plugin);'!", new NullPointerException());
        return instance;
    }

    public static JavaPlugin getPlugin() { return getInstance().plugin; }
    /** Gets the general plugin configuration, containing basic data about the setup of the gamemode */
    public static Config getConfig() { return getInstance().general; }
    /** Gets the language configuration, containing all strings and string lists that might want to be changed based on the servers language or specifics */
    public static ConfigReader getLang() { return getInstance().lang; }

    /*
    * Managers
    * */
    private PlayerManager playerManager;
    private GamestateManager gamestateManager;
    public void setPlayerMan(PlayerManager manager) { this.playerManager = manager; }
    public void setStateMan(GamestateManager manager) { this.gamestateManager = manager; }
    public static PlayerManager getPlayerMan() { return getInstance().playerManager; }
    public static GamestateManager getStateMan() { return getInstance().gamestateManager; }

    private ISimpleScoreBoardManager scoreboard;
    public void setScoreBoard(ISimpleScoreBoardManager scoreboard) { this.scoreboard = scoreboard; }
    public static ISimpleScoreBoardManager getScoreBoard() { return getInstance().scoreboard; }

}
