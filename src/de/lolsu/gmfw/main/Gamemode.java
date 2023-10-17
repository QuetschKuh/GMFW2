package de.lolsu.gmfw.main;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Gamemode {

    static Gamemode instance;
    public final JavaPlugin plugin;
    public final Config config;

    public Gamemode(JavaPlugin plugin, Config baseConfig) {
        this.plugin = plugin;
        this.config = baseConfig;
    }

    public static Gamemode getInstance() {
        if(instance == null) System.err.println("Tried to get Gamemode instance but is null. Please initialize Gamemode first by using 'new Gamemode(plugin, baseConfig);'");
        return instance;
    }

    public JavaPlugin getPlugin() {
        return instance.plugin;
    }

    public Config getConfig() {
        return instance.config;
    }

}
