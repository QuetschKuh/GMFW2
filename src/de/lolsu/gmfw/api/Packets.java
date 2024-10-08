package de.lolsu.gmfw.api;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketListenerPlayOut;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * A simple class to contain and send multiple packets to a player in order.<br>
 * Only works with PacketListenerPlayOut.
 * */
public class Packets {

    /** The packets */
    private final Packet<PacketListenerPlayOut>[] packets;

    /**
     * @param packets The packets that may be sent later, in order
     * */
    @SafeVarargs
    public Packets(Packet<PacketListenerPlayOut>... packets) {
        this.packets = packets;
    }

    /**
     * Sends the packets with which this object was constructed, to a player, in order
     * @param player The player to whom the packets should be sent
     * */
    public void send(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for(Packet<PacketListenerPlayOut> packet : packets)
            connection.sendPacket(packet);
    }

    /**
     * Sends the given packet to the given player
     * */
    public static void send(Player player, Packet<PacketListenerPlayOut> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    /**
     * Sends the given packets to the given player
     * */
    @SafeVarargs
    public static void send(Player player, Packet<PacketListenerPlayOut>... packets) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for(Packet<PacketListenerPlayOut> packet : packets)
            connection.sendPacket(packet);
    }

}