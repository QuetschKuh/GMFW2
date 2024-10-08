package de.lolsu.gmfw.admin;

import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.api.Packets;
import de.lolsu.gmfw.config.Config;
import de.lolsu.gmfw.config.ConfigUtil;
import de.lolsu.gmfw.events.Listeners;
import de.lolsu.gmfw.events.custom.PlayerInteractNPCEvent;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.phases.MapHandler;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Instantiatable class for handling user-friendly setting of locations.
 * */
public class AdminConfigurator implements Listener {

    private final Player player;
    private final String path;
    private final boolean series;

    /** The placement mode of the location. 0 = block looking at, 1 = player looking at, 2 = block at foot level, 3 = player location */
    private int mode = 0;
    private int taskId;

    private static final String actionbar = "[RMB] Confirm; [LMB] Change mode; [SNEAK] Exit";
    private static final String[] modes = {
            "Mode Change: Targeted Block",
            "Mode Change: Facing Player",
            "Mode Change: Block at Feet",
            "Mode Change: Your Location"
    };
    private static boolean active = false;

    /** Prohibits other functions you shouldn't be able to execute while a location is being modified */
    public static boolean isActive() {
        return active;
    }

    /**
     * Puts a player into location config mode with the given path to the location
     * @param player The player
     * @param path The path to the location
     * @param series Whether the location path is a list and the configurator should continue after confirmation of one location
     * */
    public AdminConfigurator(Player player, String path, boolean series) {
        active = true;

        this.player = player;
        this.path = path;
        this.series = series;

        // Set up block and armourstand
        current = player.getLocation();
        current.setY(0);

        nmsStand = new EntityArmorStand(((CraftWorld) player.getWorld()).getHandle(), current.getX(), current.getY(), current.getZ());
        nmsStand.setGravity(false);
        nmsStand.setBasePlate(false);
        nmsStand.setArms(true);
        PacketPlayOutSpawnEntityLiving packetSpawn = new PacketPlayOutSpawnEntityLiving(nmsStand);
        PacketPlayOutEntityEquipment packetHand = new PacketPlayOutEntityEquipment(nmsStand.getId(), 0, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_SWORD)));
        PacketPlayOutEntityEquipment packetHead = new PacketPlayOutEntityEquipment(nmsStand.getId(), 1, CraftItemStack.asNMSCopy(new ItemStack(Material.DIAMOND_HELMET)));
        Packets.send(player, packetSpawn, packetHand, packetHead);

        Listeners.registerLight(this);

        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                Messenger.sendActionBar(player, actionbar);
            }
        }.runTaskTimer(Gamemode.getPlugin(), 0L, 20L).getTaskId();
    }

    public void destroy() {
        Bukkit.getScheduler().cancelTask(taskId);

        // Destroy indicators
        resetBlock();
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(nmsStand.getId());
        Packets.send(player, packet);

        // Remove events
        Listeners.removeLight(this);

        active = false;
    }

    public Location current;
    public void setBlock(Location location) {
        // Reset previous block (no need for checks since we set up in constructor)
        resetBlock();

        // Send new block
        PacketPlayOutBlockChange packetP = new PacketPlayOutBlockChange(((CraftWorld) location.getWorld()).getHandle(), new BlockPosition(location.getX(), location.getY(), location.getZ()));
        packetP.block = CraftMagicNumbers.getBlock(Material.GLASS).getBlockData();
        Packets.send(player, packetP);

        current = location;
    }

    private void resetBlock() {
        PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(((CraftWorld) current.getWorld()).getHandle(), new BlockPosition(current.getX(), current.getY(), current.getZ()));
        packet.block = CraftMagicNumbers.getBlock(current.getBlock().getType()).fromLegacyData(current.getBlock().getData());
        Packets.send(player, packet);
    }

    public EntityArmorStand nmsStand;
    public void teleportStand(Location location) {
        nmsStand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(nmsStand);
        Packets.send(player, packet);
    }

    public void cycleMode() {
        mode = ++mode % 4;
        Messenger.sendTitleBar(player, "", modes[mode], 5, 25, 10);

        // Move away the other indicator
        Location location = player.getLocation();
        location.setY(0);
        if(mode % 2 == 0) teleportStand(location);
        else setBlock(location);
    }

    public void saveLocation() {
        // Get the correct location
        Location location;
        if(mode % 2 == 0) location = current;
        else location = new Location(null, nmsStand.locX, nmsStand.locY, nmsStand.locZ, nmsStand.yaw, nmsStand.pitch);

        // Get the correct config
        Config config;
        if(Objects.equals(MapHandler.getMap(), player.getWorld().getName()))
            config = MapHandler.getConfig();
        else config = Gamemode.getConfig();

        if(series) {
            List<Location> locations = config.getLocationList(path, null);
            locations.add(location);
            config.setLocationList(path, locations);
        } else {
            config.setLocation(path, location);
        }

        config.save();

        Messenger.send(player, "Location saved: '" + path + "' - " + location.getX() + " " + location.getY() + " " + location.getZ() + (series ? " (list)" : ""));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(event.getPlayer() != player) return;

        // Sneak = Exit
        if(event.getPlayer().isSneaking()) {
            destroy();
            return;
        }

        // Adjust armour stand/block location
        Location location = player.getLocation();

        if(mode == 0) {
            location = player.getTargetBlock((Set<Material>) null, 8).getLocation();
        }

        if(mode == 1) {
            List<Block> blocks = player.getLastTwoTargetBlocks((Set<Material>) null, 8);
            if (blocks.size() != 2 || !blocks.get(1).getType().isOccluding()) return;
            Block target = blocks.get(1);
            BlockFace face = target.getFace(blocks.get(0));
            location = target.getLocation();
            location.add(face.getModX(), face.getModY(), face.getModZ());
            location.setYaw((player.getLocation().getYaw() + 180) % 360);
        }

        location = ConfigUtil.smoothLocation(location);

        if(mode % 2 == 0) setBlock(location);
        else teleportStand(location);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(event.getPlayer() != player) return;

        event.setCancelled(true);

        if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            cycleMode();
        } else if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            saveLocation();
            if(!series) destroy();
        }
    }

    @EventHandler
    public void onInteractNPC(PlayerInteractNPCEvent event) {
        if(event.getPlayer() != player) return;

        event.setCancelled(true);

        if(event.getAction() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
            cycleMode();
        } else if(event.getAction() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT || event.getAction() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
            saveLocation();
            if(!series) destroy();
        }
    }

}
