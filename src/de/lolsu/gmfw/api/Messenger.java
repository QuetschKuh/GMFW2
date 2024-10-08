package de.lolsu.gmfw.api;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Messenger {

    private static String prefix = "GM | ";
    private static String prefixSetup = "SETUP | ";

    public static void setPrefix(String prefix) {
        Messenger.prefix = ChatColor.RESET + prefix + ChatColor.GRAY + ChatColor.BOLD + " | " + ChatColor.RESET;
    }

    public static void setPrefixSetup(String prefix) {
        Messenger.prefixSetup = ChatColor.RESET + prefix + ChatColor.GRAY + ChatColor.BOLD + " | " + ChatColor.RESET;
    }

    /**
     * Broadcasts the given message with the prefix prepended
     * */
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(prefix + message);
    }

    /**
     * Broadcasts the given message and adds the prefix after each newline
     * */
    public static void broadcastLines(String message) {
        broadcast(message.replace("\n", "\n" + prefix));
    }

    /**
     * Broadcasts the given message as is.<br>
     * Equivalent to {@link org.bukkit.Bukkit#broadcastMessage(String)}
     * */
    public static void broadcastNoPre(String message) {
        Bukkit.broadcastMessage(message);
    }

    /**
     * Sends the given messsage to the given player with the setup prefix prepended
     * */
    public static void sendDev(Player player, String message) {
        player.sendMessage(prefixSetup + message);
    }

    /**
     * Sends the given message to the given player with the prefix prepended
     * */
    public static void send(Player player, String message) {
        player.sendMessage(prefix + message);
    }

    /**
     * Sends the given message to the given player and adds the prefix after each newline
     * */
    public static void sendLines(Player player, String message) {
        send(player, message.replace("\n", "\n" + prefix));
    }

    public static void sendGroup(Collection<? extends Player> players, String message) {
        for(Player player : players)
            player.sendMessage(prefix + message);
    }

    public static void sendTitleBar(Player player, String textTitle, String textMessage, int fadeIn, int length, int fadeOut) {
        IChatBaseComponent componentTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textTitle + "\"}");
        IChatBaseComponent componentMessage = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textMessage + "\"}");

        PacketPlayOutTitle packetTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, componentTitle);
        PacketPlayOutTitle packetMessage = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, componentMessage);
        PacketPlayOutTitle packetLength = new PacketPlayOutTitle(fadeIn, length, fadeOut);

        Packets.send(player, packetTitle, packetMessage, packetLength);
    }

    public static void sendTitleBars(Collection<? extends Player> players, String textTitle, String textMessage, int fadeIn, int length, int fadeOut) {
        IChatBaseComponent componentTitle = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textTitle + "\"}");
        IChatBaseComponent componentMessage = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + textMessage + "\"}");

        PacketPlayOutTitle packetTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, componentTitle);
        PacketPlayOutTitle packetMessage = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, componentMessage);
        PacketPlayOutTitle packetLength = new PacketPlayOutTitle(fadeIn, length, fadeOut);

        for(Player player : players)
            Packets.send(player, packetTitle, packetMessage, packetLength);
    }

    public static void broadcastTitleBar(String textTitle, String textMessage, int fadeIn, int length, int fadeOut) {
        sendTitleBars(Bukkit.getOnlinePlayers(), textTitle, textMessage, fadeIn, fadeOut, length);
    }

    public static void sendActionBar(Player player, String message) {
        IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(component, (byte) 2);

        Packets.send(player, packet);
    }

    public static void sendActionBars(Collection<? extends Player> players, String message) {
        IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(component, (byte) 2);

        for(Player player : players) Packets.send(player, packet);
    }

    public static void broadcastActionBars(String message) {
        sendActionBars(Bukkit.getOnlinePlayers(), message);
    }

    public static void broadcastSound(Sound sound) {
        for(Player player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), sound, 1, 1);
    }

}
