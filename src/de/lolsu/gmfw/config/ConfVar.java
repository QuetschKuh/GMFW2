package de.lolsu.gmfw.config;

import de.lolsu.gmfw.main.Logger;
import de.lolsu.gmfw.managers.PlayerManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import sun.security.krb5.internal.crypto.EType;

import java.util.ArrayList;
import java.util.Objects;

/** A simple configuration enum. These are not able to be changed in-game and need to be hardcoded. All booleans are on {@code false} by default unless specified otherwise. Default values of all non-boolean types are listed in the javadocs */
public enum ConfVar {

    PATH_STARTFILE("start.sh"),

    // Global settings
    /** Whether PVP should be enabled during the game. PVP will be enabled a certain time after game start, defined by {@link #TIME_PVP_ENABLE} */
    PVP_ENABLED,
    /** Whether members of a team should all have the same spawn point (e.g. Bed Wars) or if they should be split up (e.g. Survival Games) */
    NO_TEAM_SPAWNS,

    // Timings
    /** Time in seconds after the start of the game in which the player can't move freely. The type of movement players are allowed is defined by {@link #WB_WATCH_ENABLE}. A delay of 0 will skip the watch phase (DEFAULT: 10) */
    TIME_WATCH(10),
    /** Time in seconds after the start of the game until pvp is enabled and players can fight. Only active when {@link #PVP_ENABLED} is enabled. A delay of 0 will enable PVP immediately. Can't be smaller than {@link #TIME_WATCH} (DEFAULT: 30) */
    TIME_PVP_ENABLE(30),

    /** The title to be displayed when the watch-phase is done (DEFAULT: "FIGHT!") */
    WATCH_DONE_TITLE("FIGHT!"),

    // Worldborder
    /** Whether during {@link #TIME_WATCH} a per-player world-border should be employed rather than just disabling movement entirely */
    WB_WATCH_ENABLE,
    /** Diameter in blocks of the world-border during {@link #TIME_WATCH}, only relevant if {@link #WB_WATCH_ENABLE} is enabled. (DEFAULT: 20) */
    WB_WATCH_DIAMETER(20),
    /** Diameter in blocks of the world-border during the game. (DEFAULT: 1000) */
    WB_DIAMETER_START(1000),
    /** Whether the world-border should shrink during the game */
    WB_SHRINK,
    /** Time in seconds until the worldborder should shrink (DEFAULT: 600) */
    WB_SHRINK_DELAY(600),
    /** Duration in seconds in which the worldborder should go from WB_DIAMETER_START to WB_DIAMETER_END (DEFAULT: 180) */
    WB_SHRINK_DURATION(180),
    /** Diameter in blocks of the worldborder at the end of shrinkage (DEFAULT: 50) */
    WB_DIAMETER_END(50),

    // Map
    /** Whether players should be able to break the map */
    MAP_BREAKABLE,
    /** Whether players should be able to place blocks on the map */
    MAP_PLACEABLE,
    /** A whitelist of blocks able to be broken if {@link #MAP_BREAKABLE} is disabled. */
    MAP_BREAK_WHITELIST(new ArrayList<Material>()),
    /** A whitelist of blocks able to be placed if {@link #MAP_PLACEABLE} is disabled. */
    MAP_PLACE_WHITELIST(new ArrayList<Material>()),
    /** The gamemode players should be in */
    MAP_GAMEMODE(GameMode.SURVIVAL),

    // Death
    /** Whether {@link PlayerManager#kill(Player)} drops the entire inventory of the player */
    DEATH_DROPS_ALL,
    /** The items that should be dropped if {@link ConfVar#DEATH_DROPS_ALL} is false.<br>
     * Takes an {@code ArrayList<Material>}, which is set with {@link ConfVar#setObject(Object)} and retrieved with {@link ConfVar#getObject()} */
    DEATH_DROPS_WHITELIST(new ArrayList<Material>()),

    // Modules
    /** Makes the armour invisible on players who have drunk an invisibility potion */
    MODULE_INVIS_ARMOUR,
    /** Immediately activates any tnt placed, making TNTs named "Napalm" explode bigger and fiery */
    MODULE_TNT_INSTANT,
    /** The amount of health you gain by drinking mushroom soup */
    MODULE_SOUP_HEAL(0.0),
    /** Automatically puts lapis in any enchanting table */
    MODULE_LAPIS_AUTO,
    ;

    private double value = 0.0;
    private Object obj = null;

    ConfVar() {

    }
    ConfVar(boolean state) {
        this.value = state ? 1.0 : 0.0;
    }
    ConfVar(int value) {
        this.value = value;
    }
    ConfVar(float value) {
        this.value = value;
    }
    ConfVar(double value) {
        this.value = value;
    }
    ConfVar(Object obj) {
        this.obj = obj;
    }

    public boolean getState() { return value == 1.0; }
    public int getInt() { return (int) value; }
    public float getFloat() { return (float) value; }
    public double getDouble() { return value; }
    public String getString() { return (String) obj; }
    public Object getObject() { return obj; }
    public <T> T getObject(Class<T> type) {
        T result = null;
        try {
            result = type.cast(obj);
        } catch (ClassCastException exception) {
            Logger.error("Failed to cast Object gotten from ConfVar", exception);
        }
        return result;
    }
    public Class<?> getObjectType() { return obj.getClass(); }

    public void setState(boolean state) { value = (state ? 1.0 : 0.0); }
    public void setInt(int value) { this.value = value; }
    public void setFloat(float value) { this.value = value; }
    public void setDouble(double value) { this.value = value; }
    public void setString(String string) { this.obj = string; }
    public void setObject(Object obj) { this.obj = obj; }

}
