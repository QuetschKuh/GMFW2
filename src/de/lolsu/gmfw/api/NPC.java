package de.lolsu.gmfw.api;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Simplified representation of a non-player-character (NPC).
 * Basically a holder as well as creator for the NMS (net.minecraft.server) EntityPlayer and according packets.
 * */
public class NPC {

    /** The actual player entity */
    public EntityPlayer nmsPlayer;
    /** The 4 packets that are sent to the player for the npc to appear */
    public Packets packets;

    /** Creates a non-player-character (NPC) that will just stand there
     * @param l The location of the npc
     * @param username The username of the npc displayed above its head as well as the player whose skin should be applied to the npc
     * */
    public NPC(Location l, String username) {
        this(l, username, username);
    }

    /** Creates a non-player-character (NPC) that will just stand there
     * @param l The location of the npc
     * @param npcUsername The username of the npc displayed above its head (must be < 16 chars)
     * @param skinUsername The username of the player whose skin should be applied to the npc
     */
    public NPC(Location l, String npcUsername, String skinUsername) {
        // Create the npc
        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer nmsWorld = ((CraftWorld) l.getWorld()).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), npcUsername);
        nmsPlayer = new EntityPlayer(nmsServer, nmsWorld, profile, new PlayerInteractManager(nmsWorld));
        nmsPlayer.setLocation(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());

        // Apply the skin
        try {
            HttpsURLConnection conn = (HttpsURLConnection) new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", skinUsername)).openConnection();
            if(conn.getResponseCode() == 200) {
                ArrayList<String> lines = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                reader.lines().forEach(lines::add);

                String reply = String.join(" ",lines);
                int indexOfValue = reply.indexOf("\"value\": \"");
                int indexOfSignature = reply.indexOf("\"signature\": \"");
                String skin = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
                String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

                profile.getProperties().put("textures", new Property("textures", skin, signature));
            } else {
                System.out.println("Connection couldn't be opened while fetching player skin\nCode: " + conn.getResponseCode() + "\n" + conn.getResponseMessage() + ")");
            }
        } catch (IOException ex) {
            System.out.println("Something went wrong while applying a skin to an npc\n" + ex.getMessage());
        }

        // Magic probably
        DataWatcher watcher = new DataWatcher(null);
        watcher.a(6, (float) 20);
        watcher.a(10, (byte) 127);

        // Create the packets
        packets = new Packets(
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, nmsPlayer),
                new PacketPlayOutNamedEntitySpawn(nmsPlayer),
                new PacketPlayOutEntityMetadata(nmsPlayer.getId(), watcher, true),
                new PacketPlayOutEntityHeadRotation(nmsPlayer, (byte) (nmsPlayer.yaw * 256 / 360))
        );
    }

    /**
     * Summons the non-player-character (NPC) on the given players client
     * @param player The player to whom the NPC should be sent to
     * */
    public void send(Player player) {
        packets.send(player);
    }

}