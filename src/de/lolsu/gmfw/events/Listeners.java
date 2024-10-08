package de.lolsu.gmfw.events;

import de.lolsu.gmfw.main.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton
 * */
public class Listeners {

    private static final Listeners instance = new Listeners();
    public static Listeners get() { return instance; }
    private Listeners () { }

    public List<Listener> listenersCurrent = new ArrayList<>();
    public List<Listener> listenersIngame = new ArrayList<>();

    /**
     * Adds a listener to the ingame listeners which are registered only during the game and unregistered right after
     * @param listeners The listeners you want to have active during the game
     * */
    public static void addIngame(Listener... listeners) {
        get().listenersIngame.addAll(Arrays.asList(listeners));
    }

    /**
     * Registers all events by the given listener and allows future unregistering
     * @param listener The listener that should be registered
     * */
    public static void register(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, Gamemode.getPlugin());
        get().listenersCurrent.add(listener);
    }

    /**
     * Registers all events by all given listeners and allows future unregistering
     * @param listeners The listeners that should be registered
     * */
    public static void register(Listener... listeners) {
        for(Listener listener : listeners)
            register(listener);
    }

    /**
     * Unregisters all events by the given class that have been previously registered
     * @param listener The listener class with the events that should be unregistered
     * @return The listener that was removed, if none was found, null
     */
    public static Listener remove(Class<? extends Listener> listener) {
        for(Listener l : get().listenersCurrent) {
            if(!l.getClass().equals(listener)) continue;

            HandlerList.unregisterAll(l);
            get().listenersCurrent.remove(l);
            return l;
        }

        return null;
    }

    /**
     * Unregisters all events by the given classes that have been previously registered
     * */
    public static void removeAll(Collection<Listener> listeners) {
        for(Listener l : listeners) {
            if(!get().listenersCurrent.contains(l)) continue;

            HandlerList.unregisterAll(l);
            get().listenersCurrent.remove(l);
        }
    }

    /**
     * Registers all listeners that should only be handled during the game
     * */
    public static void registerAllIngame() {
        for(Listener listener : get().listenersIngame)
            register(listener);
    }

    /**
     * Unregisters all listeners that should only be handled during the game
     * */
    public static void unregisterAllIngame() {
        removeAll(get().listenersIngame);
    }

    /** Registers a listener without handling class logic, requiring you to keep an instance for later removal */
    public static void registerLight(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, Gamemode.getPlugin());
    }

    /** Unregisters a listener based on an instance instead of a class and list */
    public static void removeLight(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

}
