package de.lolsu.gmfw.api;

import de.lolsu.gmfw.main.Logger;

import javax.persistence.EntityNotFoundException;

/**
 * An enum for all gamerules available from 1.8.1 to 1.8.9
 * */
public enum GameruleValue {
    DO_FIRE_TICK("doFireTick", "true"),
    MOB_GRIEFING("mobGriefing", "true"),
    KEEP_INVENTORY("keepInventory", "false"),
    DO_MOB_SPAWNING("doMobSpawning", "true"),
    DO_MOB_LOOT("doMobLoot", "true"),
    DO_TILE_DROPS("doTileDrops", "true"),
    COMMAND_BLOCK_OUTPUT("commandBlockOutput", "true"),
    NATURAL_REGENERATION("naturalRegeneration", "true"),
    DO_DAYLIGHT_CYCLE("doDaylightCycle", "true"),
    LOG_ADMIN_COMMANDS("logAdminCommands", "true"),
    SHOW_DEATH_MESSAGES("showDeathMessages", "true"),
    RANDOM_TICK_SPEED("randomTickSpeed", "3"),
    SEND_COMMAND_FEEDBACK("sendCommandFeedback", "true"),
    REDUCED_DEBUG_INFO("reducedDebugInfo", "false"),
    DO_ENTITY_DROPS("doEntityDrops", "true"),
    ;

    public final String value;
    public final String def;

    GameruleValue(String value, String def) {
        this.value = value;
        this.def = def;
    }

    @Override
    public String toString() {
        return value;
    }

    public GameruleValue fromString(String value) {
        for(GameruleValue rule : GameruleValue.values())
            if(rule.value.equals(value)) return rule;
        Logger.error("Failed to get GameruleValue from String", new EntityNotFoundException());
        return null;
    }

}
