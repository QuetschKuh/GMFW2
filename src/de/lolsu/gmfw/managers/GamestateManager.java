package de.lolsu.gmfw.managers;

import de.lolsu.gmfw.api.*;
import de.lolsu.gmfw.config.ConfVar;
import de.lolsu.gmfw.config.ConfigUtil;
import de.lolsu.gmfw.events.*;
import de.lolsu.gmfw.events.ingame.Damage;
import de.lolsu.gmfw.events.ingame.Map;
import de.lolsu.gmfw.events.ingame.Spectator;
import de.lolsu.gmfw.events.pregame.Lobby;
import de.lolsu.gmfw.events.pregame.Warmup;
import de.lolsu.gmfw.interfaces.IGamestateManager;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.main.Logger;
import de.lolsu.gmfw.modules.AutoLapis;
import de.lolsu.gmfw.modules.InvisArmour;
import de.lolsu.gmfw.modules.Soup;
import de.lolsu.gmfw.modules.TNT;
import de.lolsu.gmfw.phases.MapHandler;
import de.lolsu.gmfw.phases.pregame.VoteManager;
import de.lolsu.gmfw.phases.routines.LobbyRoutine;
import de.lolsu.gmfw.phases.routines.ShrinkWorldBorder;
import de.lolsu.gmfw.phases.routines.TitleRoutine;
import de.lolsu.gmfw.phases.routines.WarmupRoutine;
import de.lolsu.gmfw.statistics.StatManager;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The gamestate manager is one of 5 managers and arguably the most relevant one.<br>
 *
 * */
public class GamestateManager extends IGamestateManager {

    /* Runnables */
    public LobbyRoutine lobbyRoutine;

    /** Pregame vote manager, active from {@link #onStart()} to {@link #onVotingEnd()} */
    public VoteManager voteManager;

    public GamestateManager() {

    }

    @Override
    public void onStart() {
        state = State.PREGAME;

        voteManager = new VoteManager();

        // Register listeners
        Listeners.register(
                // General
                new PlayerJoinLeave(),
                new Chat(),
                // Lobby
                new NoInteracts(),
                new Lobby()
        );

        lobbyRoutine = new LobbyRoutine();

        Logger.info("Game initialized");
    }

    @Override
    public void onCountdownStart() {
        if(lobbyRoutine.isCountdown()) return;

        lobbyRoutine.countdown = lobbyRoutine.countdownMax;

        Logger.info("Countdown started");
    }

    @Override
    public void onCountdownQuick() {
        if(lobbyRoutine.isStarting()) return;

        lobbyRoutine.countdown = 20;
        Messenger.broadcast("Quick-starting game!");

        Logger.info("Countdown accelerated");
    }

    @Override
    public void onCountdownCancel() {
        if(!lobbyRoutine.isCountdown()) return;

        lobbyRoutine.countdown = -1;
        Messenger.broadcastTitleBar(ChatColor.RED + "Canceled!", ChatColor.DARK_RED + "Waiting for players...", 5, 15, 20);

        Logger.info("Countdown canceled");
    }

    @Override
    public void onVotingEnd() {
        // Unregister events, remove items and close the inventories
        Listeners.remove(Lobby.class);
        PlayerManager.forAll(p -> {
            p.getInventory().remove(Material.CHEST);
            p.getInventory().remove(Material.DIAMOND);
            p.getInventory().remove(Material.BOAT);
            p.getInventory().remove(Material.NAME_TAG);
            p.closeInventory();
        });

        Gamemode.getPlayerMan().autoAssignTeams();

        // Load the most voted map and announce it
        String mostVoted = voteManager.getMostVoted();
        MapHandler.loadMap(mostVoted);
        Messenger.broadcastLines(Chars.divider + "\n" +
                ChatColor.RED + "Voting ended!\n" +
                ChatColor.LIGHT_PURPLE + "The map " + ChatColor.GOLD + ChatColor.BOLD + ConfigUtil.fancy(mostVoted) + ChatColor.LIGHT_PURPLE + " won!\n" +
                Chars.divider
        );

        Logger.info("Voting ended");
    }

    @Override
    public void onGameStart() {
        state = State.INGAME;

        // Allow interactions
        Listeners.remove(NoInteracts.class);

        // Register modules
        if(ConfVar.MODULE_INVIS_ARMOUR.getState()) Listeners.register(new InvisArmour());
        if(ConfVar.MODULE_LAPIS_AUTO.getState()) Listeners.register(new AutoLapis());
        if(ConfVar.MODULE_TNT_INSTANT.getState()) Listeners.register(new TNT());
        if(ConfVar.MODULE_SOUP_HEAL.getState()) Listeners.register(new Soup());

        Listeners.register(new Damage(), new Spectator(Gamemode.getPlayerMan()), new Map());
        Listeners.registerAllIngame();

        Gamemode.getPlayerMan().initializeTeams();

        // Reset and teleport
        PlayerManager.forAll(player -> {
            player.setGameMode(ConfVar.MAP_GAMEMODE.getObject(GameMode.class));
            Gamemode.getPlayerMan().reset(player);
        });
        teleportPlayers();

        // Start warmup (if warmup in enabled)
        if(ConfVar.TIME_WATCH.getInt() != 0 || ConfVar.TIME_PVP_ENABLE.getInt() != 0) {
            if(ConfVar.TIME_PVP_ENABLE.getInt() < ConfVar.TIME_WATCH.getInt()) {
                ConfVar.TIME_PVP_ENABLE.setInt(ConfVar.TIME_WATCH.getInt());
                Logger.warn("ConfVar TIME_PVP_ENABLE can't be smaller than TIME_WATCH!");
            }
            new WarmupRoutine(this, ConfVar.TIME_WATCH.getInt(), ConfVar.TIME_PVP_ENABLE.getInt()).runTaskTimer(Gamemode.getPlugin(), 0L, 20L);
        }

        // Worldborder
        if(ConfVar.WB_SHRINK.getState())
            new ShrinkWorldBorder(
                    ConfVar.WB_DIAMETER_END.getInt(),
                    ConfVar.WB_SHRINK_DURATION.getInt(),
                    20, 20
            ).runTaskLater(Gamemode.getPlugin(), ConfVar.WB_SHRINK_DELAY.getInt() * 20L);

        // PVP edge case
        if(ConfVar.PVP_ENABLED.getState() && ConfVar.TIME_PVP_ENABLE.getInt() == 0)
            MapHandler.getWorld().setPVP(true);
        else MapHandler.getWorld().setPVP(false);

        // Thingy
        Messenger.sendTitleBars(Bukkit.getOnlinePlayers(), Gamemode.getInstance().nameGamemode, ChatColor.GRAY + "A " + ChatColor.WHITE + "lolsu.de" + ChatColor.GRAY + " Creation", 10, 30, 40);

        Logger.info("Game started");
    }

    /**
     * Separate function to allow for easier modification without needing to rewrite the entire {@link #onGameStart()} method.
     * */
    private void teleportPlayers() {
        PlayerManager pm = Gamemode.getPlayerMan();
        List<Location> spawns = MapHandler.getConfig().getLocationList("Spawns.Game", MapHandler.getWorld());
        boolean noTeamSpawns = ConfVar.NO_TEAM_SPAWNS.getState();
        int spawnsNeeded = noTeamSpawns ? pm.getPlayersAmount() : pm.getTeamsAmount();
        for (int i = 0; i < spawnsNeeded; i++) {
            // In germany we say "Ehrenlose Zeile Code"
            Location l = spawns.get((int) Math.floor(i * (float) ((noTeamSpawns ? pm.maxPlayers : pm.maxPlayers / pm.maxPerTeam) / spawnsNeeded)));
            if (noTeamSpawns) pm.getPlayers().get(i).teleport(l);
            else pm.getTeams().get(i).teleportAll(l);
        }
    }

    @Override
    public void onWatchEnd() {
        if(ConfVar.WB_WATCH_ENABLE.getState()) WorldBorders.broadcastRemoveWorldBorders();
        else Listeners.remove(Warmup.class);

        Messenger.broadcast(ChatColor.GREEN + "Game started!");
        Messenger.broadcastSound(Sound.GLASS);
        new TitleRoutine(ConfVar.WATCH_DONE_TITLE.getString()).runTaskTimerAsynchronously(Gamemode.getPlugin(), 0L, 2L);

        Logger.info("Watch period ended");
    }

    @Override
    public void onWarmupEnd() {
        MapHandler.getWorld().setPVP(true);

        Messenger.broadcast(ChatColor.RED + "PVP has been enabled!");
        Messenger.broadcastSound(Sound.GLASS);

        Logger.info("Warmup period ended");
    }

    @Override
    public void onGameEnd() {
        state = State.POSTGAME;

        // Winner title (format: first name {, next name } [ and last name]
        List<String> names = Gamemode.getPlayerMan().getTeamsAlive().get(0).names();
        String players = names.size() > 1 ?
                String.join(", ", names.subList(0, names.size() - 1))
                        .concat(String.format("%s and ", names.size() > 2 ? "," : ""))
                        .concat(names.get(names.size() - 1))
                : names.get(0);
        Messenger.broadcastTitleBar(ChatColor.RED + "Game Over!", ChatColor.GOLD + players + " won!", 10, 20, 70);

        // 5 second delay until podium is loaded
        Bukkit.getScheduler().runTaskLater(Gamemode.getPlugin(), this::onPodiumStart, 100L);

        Logger.info("Game ended");
    }

    @Override
    public void onPodiumStart() {
        Listeners.unregisterAllIngame();
        Listeners.remove(Damage.class);
        Listeners.remove(Spectator.class);
        Listeners.register(new NoInteracts());

        World worldPodium = new WorldCreator("Podium").createWorld();
        worldPodium.setDifficulty(Difficulty.PEACEFUL);
        worldPodium.setPVP(false);
        worldPodium.setThundering(false);

        // Podium locations: Player spawn, NPC Stat 1, NPC Stat 2, NPC Stat 3, NPC Winner, FW1, FW2
        Location[] podiumLocs = new Location[] {
                Gamemode.getConfig().getLocation("Podium.Spawn", worldPodium),
                Gamemode.getConfig().getLocation("Podium.NPCs.Stat1", worldPodium),
                Gamemode.getConfig().getLocation("Podium.NPCs.Stat2", worldPodium),
                Gamemode.getConfig().getLocation("Podium.NPCs.Stat3", worldPodium),
                Gamemode.getConfig().getLocation("Podium.NPCs.Winners", worldPodium),
                Gamemode.getConfig().getLocation("Podium.Fireworks.1", worldPodium),
                Gamemode.getConfig().getLocation("Podium.Fireworks.2", worldPodium)
        };

        List<String> stats = Gamemode.getConfig().getStringList("Stats.Displayed");

        // Get players of statistics
        OfflinePlayer[] players = {
                Bukkit.getOfflinePlayer(StatManager.getHighest(stats.get(0))),
                Bukkit.getOfflinePlayer(StatManager.getHighest(stats.get(1))),
                Bukkit.getOfflinePlayer(StatManager.getHighest(stats.get(2)))
        };

        // Get winners and calculate winner location
        List<Player> winners = getWinners();
        Location winnerLoc = podiumLocs[4].subtract( winners.size() - 1, 0, 0);

        // Construct NPCs: Create list, populate with stats, populate with winners
        NPC[] npcs = new NPC[3 + winners.size()];
        for(int i = 0; i < 3; i++) npcs[i] = new NPC(podiumLocs[i + 1], players[i].getName());
        for(int i = 0; i < winners.size(); i++) npcs[i + 3] = new NPC(winnerLoc.add(2, 0, 0), winners.get(i).getDisplayName());

        // Construct armour stands
        PacketPlayOutSpawnEntityLiving[] packetSpawnStands = new PacketPlayOutSpawnEntityLiving[3];
        for(int i = 0; i < 3; i++) {
            EntityArmorStand nmsStand = new EntityArmorStand(((CraftWorld) worldPodium).getHandle());
            Location l = podiumLocs[i + 1];
            nmsStand.setLocation(l.getX(), l.getY() -2, l.getZ() -2, 0, 0);
            nmsStand.setGravity(false);
            nmsStand.setInvisible(true);
            ChatColor color = i == 0 ? ChatColor.GOLD : i == 1 ? ChatColor.WHITE : ChatColor.RED;
            nmsStand.setCustomName(color + Gamemode.getLang().getString("Stats.Most." + stats.get(i), "Statistic") + ChatColor.GRAY + " (" + color + StatManager.getStat(players[i], stats.get(i)) + ChatColor.GRAY + ")");
            nmsStand.setCustomNameVisible(true);
            packetSpawnStands[i] = new PacketPlayOutSpawnEntityLiving(nmsStand);
        }

        // Teleport, change gamemode, send npcs and armour stands
        for(Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(podiumLocs[0]);
            player.setGameMode(GameMode.ADVENTURE);

            for(Player spectator : Gamemode.getPlayerMan().getSpectators())
                player.showPlayer(spectator);

            for (NPC npc : npcs) npc.send(player);
            Packets.send(player, packetSpawnStands);

            // Accumulate statistics
            ArrayList<String> statList = new ArrayList<>();
            for(java.util.Map.Entry<String, Integer> playerStat : StatManager.getAll(player).entrySet())
                statList.add(Gamemode.getLang().getString("Stats.Long." + playerStat.getKey()) + ": " + playerStat.getValue());
            player.getInventory().setItem(8, ItemStacks.create(Material.PAPER, ChatColor.AQUA + "> Your stats this game <", statList));
        }

        // Fireworks and countdown to restart server
        new BukkitRunnable() {
            FireworkEffect fwEffect = FireworkEffect.builder()
                    .trail(true)
                    .withColor(Color.YELLOW)
                    .with(FireworkEffect.Type.STAR)
                    .build();

            int counter = 0;
            @Override
            public void run() {
                Messenger.broadcastActionBars(ChatColor.RED + "Server restarting in " + ChatColor.DARK_RED + (15 - counter) + ChatColor.RED + "...");

                // Spawn 2 fireworks every 3 seconds
                if(counter % 3 == 0) {
                    for(int i = 0; i < 2; i++) {
                        Firework firework = (Firework) worldPodium.spawnEntity(podiumLocs[5 + i], EntityType.FIREWORK);
                        FireworkMeta fireworkMeta = firework.getFireworkMeta();
                        fireworkMeta.addEffect(fwEffect);
                        fireworkMeta.setPower(1);
                        firework.setFireworkMeta(fireworkMeta);
                    }
                }

                // Actually restart
                if(counter >= 15) {
                    cancel();
                    onShutdown();
                    return;
                }

                counter++;
            }
        }.runTaskTimer(Gamemode.getPlugin(), 100L, 20L);

        Logger.info("Podium started");
    }

    /**
     * The players that should be displayed on the winner pedestal at the podium as well as have their wins stat increased.<br>
     * By default, returns all players from the last team alive.<br>
     * Separate function to allow for easier overriding.
     * */
    private List<Player> getWinners() {
        return Gamemode.getPlayerMan().getTeamsAlive().get(0).getPlayers();
    }

    @Override
    public void onShutdown() {
        for(Player p : Bukkit.getOnlinePlayers()) p.kickPlayer(ChatColor.RED + "Server is restarting...");

        if(MapHandler.getMap() != null)
            MapHandler.unloadMap();

        // Create and or run startup file
        String path = ConfVar.PATH_STARTFILE.getString();
        try {
            File f = new File(path);
            if(!f.exists()) {
                f.createNewFile();
                Writer output = new BufferedWriter(new FileWriter(path));
                /* Combines the name of the two parent directories to make the tmux session, so for a structure like this:
                 * -> Servers
                 * 		-> survival-games
                 * 			-> single
                 * 			-> multi
                 * 		-> sw
                 * 			-> solos-1
                 * 			-> duos-1
                 * The resulting names would be: survival-games-single, sw-solos-1, ... */
                String[] sNameSplit = f.getAbsolutePath().split("/");
                String sessionName = sNameSplit[sNameSplit.length - 3] + "-" + sNameSplit[sNameSplit.length - 2];
                output.write("#!/bin/bash\nsleep 2;\ntmux new-session -d -s \"" + sessionName + "\";\ntmux send-keys -t \"" + sessionName + "\" \"java -Xmx512M -jar spigot.jar nogui\" ENTER;");
                output.close();
                Runtime.getRuntime().exec("chmod u+x " + path);
            }
            Runtime.getRuntime().exec("sh " + path);
        } catch (IOException | SecurityException exception) {
            Logger.error("Failed to create and/or run start file", exception);
        }

        Logger.info("Shutting down");
        Bukkit.shutdown();
    }

}