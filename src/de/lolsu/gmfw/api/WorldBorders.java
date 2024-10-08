package de.lolsu.gmfw.api;

import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public class WorldBorders {

    public static void setWorldBorder(double centerX, double centerY, double diameter, long time, int warningDistance, int warningTime) {
        WorldBorder wb = Bukkit.getWorld("Game").getWorldBorder();
        wb.setCenter(centerX, centerY);
        wb.setSize(diameter, time);
        wb.setDamageAmount(5);
        wb.setDamageBuffer(0);
        wb.setWarningDistance(warningDistance);
        wb.setWarningTime(warningTime);
    }

    public static void sendWorldBorder(Player p, double centerX, double centerY, double diameter, long time) {
        net.minecraft.server.v1_8_R3.WorldBorder wb = new net.minecraft.server.v1_8_R3.WorldBorder();
        wb.world = ((CraftWorld) p.getWorld()).getHandle();
        wb.setCenter(centerX, centerY);
        wb.setSize(diameter);
        PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(wb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public static void removeWorldBorder(Player p) {
        net.minecraft.server.v1_8_R3.WorldBorder wb = new net.minecraft.server.v1_8_R3.WorldBorder();
        PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(wb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public static void removeWorldBorders(Collection<? extends Player> players) {
        net.minecraft.server.v1_8_R3.WorldBorder wb = new net.minecraft.server.v1_8_R3.WorldBorder();
        PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(wb, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
        for(Player p : players) ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public  static void broadcastRemoveWorldBorders() {
        removeWorldBorders(Bukkit.getOnlinePlayers());
    }

}

