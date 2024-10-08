package de.lolsu.gmfw.netty;

import de.lolsu.gmfw.events.custom.PlayerInteractNPCEvent;
import de.lolsu.gmfw.interfaces.IPacketReader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Reads incoming packets from the player to which it is attached and calls custom events based on them
 */
public class PacketReader implements IPacketReader {

    private final Player player;
    private Channel channel;

    /**
     * Creates a new packet reader and injects the given player
     * @param player The player to be injected
     * */
    public PacketReader(Player player) {
        this.player = player;
        inject();
    }

    /**
     * Inserts a packet injector into the network pipeline of the player, executing {@link #readPacket(Packet)} when packets are received.<br>
     * You shouldn't call this method unless you uninjected the player earlier.<br>
     * This is automatically executed on construction.
     * */
    @Override
    public void inject() {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<Packet<?>>() {
            @Override
            protected void decode(ChannelHandlerContext context, Packet<?> packet, List<Object> args) {
                args.add(packet);
                readPacket(packet);
            }
        });
    }

    /**
     * Removes the PacketInjector from the player network pipeline and therefore stops reading of packets
     * */
    @Override
    public void uninject() {
        if(channel.pipeline().get("PacketInjector") != null)
            channel.pipeline().remove("PacketInjector");
    }

    /**
     * Reads a packet and executes any custom events needed
     * @param packet The packet to read
     * */
    @Override
    public void readPacket(Packet<?> packet) {
        if(packet instanceof PacketPlayInUseEntity) {
            int id = (Integer) getValue(packet, "a");
            PacketPlayInUseEntity.EnumEntityUseAction action = ((PacketPlayInUseEntity) packet).a();

            PlayerInteractNPCEvent event = new PlayerInteractNPCEvent(player, id, action);
            Bukkit.getPluginManager().callEvent(event);
        }
    }

    /**
     * Reflection function to get a declared field from a class
     * */
    @Override
    public Object getValue(Object obj, String name) {
        try {
            Field f = obj.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception exception) {
            return null;
        }
    }

}
