package de.lolsu.gmfw.statistics;

import java.util.UUID;

public class Stats {

    public static final String KILLS = "kills";
    public static final String DEATHS = "deaths";

    /** This statistic is included but not counted by GMFW2, you will have to count it yourself using {@link StatManager#increase(UUID, String, int)} or similar */
    public static final String KILLS_FINAL = "finalsout";
    /** This statistic is included but not counted by GMFW2, you will have to count it yourself using {@link StatManager#increase(UUID, String, int)} or similar */
    public static final String DEATHS_FINAL = "finalsin";

    public static final String DMG_DEALT = "damageout";
    public static final String DMG_TAKEN = "damagein";

    public static final String BLOCKS_PLACED = "blocksplaced";
    public static final String BLOCKS_BROKEN = "blocksbroken";

    /** This statistic is included but not counted by GMFW2, you will have to count it yourself using {@link StatManager#increase(UUID, String, int)} or similar */
    public static final String CHESTS = "chests";

}
