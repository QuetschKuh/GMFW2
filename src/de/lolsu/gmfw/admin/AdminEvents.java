package de.lolsu.gmfw.admin;

import de.lolsu.gmfw.api.ItemStacks;
import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.events.PlayerJoinLeave;
import de.lolsu.gmfw.netty.PacketReader;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;

public class AdminEvents implements Listener {

    private final ItemStack worldeditWand;
    private final ItemStack commandItem;

    private HashMap<Player, PacketReader> readers = new HashMap<>();

    public AdminEvents() {
        worldeditWand = ItemStacks.create(Material.WOOD_AXE, ChatColor.GOLD + "Worldedit Wand");
        commandItem = ItemStacks.addGlint(ItemStacks.create(Material.BLAZE_ROD, ChatColor.RED + "Access Setup Menu"));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!player.hasPermission("gmsetup")) {
            player.kickPlayer(ChatColor.RED + "The server is currently in setup mode.\nYou don't have the permission to join right now");
            return;
        }

        player.setGameMode(GameMode.CREATIVE);
        player.teleport(player.getLocation().add(0, 0.1, 0));
        player.setFlying(true);

        event.setJoinMessage(ChatColor.GOLD + player.getDisplayName() + ChatColor.RED + " joined in Setup Mode");

        player.getInventory().setItem(7, worldeditWand);
        player.getInventory().setItem(8, commandItem);

        readers.put(player, new PacketReader(player));

        Messenger.sendTitleBar(player, ChatColor.RED + "Entered in Setup Mode", "Type '/gmsetup setmode normal' to exit server setup", 10, 60, 10);
        Messenger.sendDev(player, "Entered in Setup Mode. Type '/gmsetup setmode normal' to exit server setup");
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        readers.remove(event.getPlayer()).uninject();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if(!Objects.equals(event.getPlayer().getItemInHand(), commandItem)) return;
        event.setCancelled(true);
        event.getPlayer().performCommand("gmsetup");
        event.getPlayer().updateInventory();
    }

}
