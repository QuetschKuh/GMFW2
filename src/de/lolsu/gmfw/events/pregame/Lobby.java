package de.lolsu.gmfw.events.pregame;

import de.lolsu.gmfw.api.ItemStacks;
import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.phases.pregame.VoteManager;
import de.lolsu.gmfw.phases.pregame.TeamSelector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

/**
 * One listener class to handle teaming, voting and credits events
 * */
public class Lobby implements Listener {

    // Items
    public static ItemStack inventory;
    public static ItemStack teaming;
    public static ItemStack voting;
    public static ItemStack rules;
    public static ItemStack credits;

    // Credits
    public List<String> creditsList = new ArrayList<>();
    Map<Player, Integer> creditsMap = new HashMap<>();
    Map<Player, Long> creditsTimeMap = new HashMap<>();

    public Lobby() {
        // First 3 (ez)
        inventory = ItemStacks.create(Material.CHEST, ChatColor.YELLOW + "" + ChatColor.BOLD + "Inventory");
        teaming = ItemStacks.create(Material.BOAT, ChatColor.BLUE + "" + ChatColor.BOLD + "Team up!");
        voting = ItemStacks.create(Material.DIAMOND, ChatColor.AQUA + "" + ChatColor.BOLD + "Vote!");

        // Rule book
        rules = ItemStacks.create(Material.WRITTEN_BOOK, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Rules");
        BookMeta ruleMeta = (BookMeta) rules.getItemMeta();
        ruleMeta.setTitle(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Rules");
        ruleMeta.setAuthor(Gamemode.getInstance().nameServer);
        // For some reason reading the config will interpret \n differently than when hardcoded, so we need to do it like this to actually write any line breaks (???)
        List<String> ruleList = Gamemode.getLang().getStringList("Rules");
        ruleList.replaceAll(s -> s.replace("<br>", "\n"));
        ruleMeta.setPages(ruleList);
        rules.setItemMeta(ruleMeta);

        // Credits
        creditsList.add(ChatColor.GOLD + "" + ChatColor.BOLD + "The Credits");
        creditsList.add(ChatColor.GRAY + "Click again to continue the credits");
        creditsList.add(ChatColor.RED + "Programming by...");
        creditsList.add(ChatColor.GOLD + "QuetschKuh (lolsu.de)");
        creditsList.addAll(Gamemode.getLang().getStringList("Credits"));
        creditsList.add(ChatColor.GOLD + "" + ChatColor.BOLD + "Credits over!");
        creditsList.add(ChatColor.GRAY + "Click again to restart the credits");

        // Credits Item Credits
        List<String> cic = new ArrayList<>(creditsList);
        cic.remove(0);
        cic.remove(0);
        cic.remove(cic.size() - 1);
        cic.remove(cic.size() - 1);
        String lastCC = "";
        for(int i = 0; i < cic.size(); i++) {
            if(i % 2 == 0) lastCC = cic.get(i).replace(ChatColor.stripColor(cic.get(i)), "");
            else cic.set(i, lastCC + ChatColor.stripColor(cic.get(i)));
        }
        for(int i = cic.size() - 2; i >= 2; i--) {
            if(i % 2 == 0)
                cic.add(i, "");
        }
        credits = ItemStacks.create(Material.NAME_TAG, ChatColor.GOLD + "" + ChatColor.BOLD + "Credits", cic);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        switch (p.getItemInHand().getType()) {
            // Voting
            case DIAMOND: {
                p.openInventory(Gamemode.getStateMan().voteManager.getSelector(p));
                break;
            }

            // Teaming
            case BOAT: {
                new TeamSelector(p).open();
                break;
            }

            // Credits
            case NAME_TAG: {
                if ((creditsTimeMap.containsKey(p) && creditsTimeMap.get(p) + 10000 < System.currentTimeMillis()) ||
                        (creditsMap.containsKey(p) && creditsMap.get(p) * 2 + 1 >= creditsList.size())
                        || !creditsMap.containsKey(p))
                    creditsMap.put(p, 0);
                creditsTimeMap.put(p, System.currentTimeMillis());

                int index = creditsMap.get(p);

                Messenger.sendTitleBar(p, creditsList.get(index * 2), creditsList.get(index * 2 + 1), 5, 15, 80);
                Messenger.sendActionBar(p, ChatColor.GRAY + "Click again to continue the credits");

                creditsMap.put(p, index + 1);
                break;
            }

            default:
                break;
        }
    }

    @EventHandler
    public void onInvInteract(InventoryClickEvent e) {
        if(e.getClickedInventory() == null) return;

        Player p = (Player) e.getWhoClicked();
        Inventory inv = e.getClickedInventory();

        // Voting
        if(Objects.equals(inv.getTitle(), VoteManager.INVENTORY_NAME)) {
            if(e.getSlot() < 0 || e.getSlot() > 53) return;

            int index = Math.floorDiv(e.getSlot(), 9);
            Gamemode.getStateMan().voteManager.setPlayerVote(p, index);
            Gamemode.getStateMan().voteManager.updateInventories();
        } else

        // Teaming
        if(Objects.equals(inv.getTitle(), TeamSelector.INVENTORY_NAME)) {
            if(e.getSlot() < 0 || e.getSlot() > 8 || e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
            String name = e.getCurrentItem().getItemMeta().getDisplayName();
            int team = Integer.parseInt(name.substring(name.length() - 1)) - 1;
            if(Gamemode.getPlayerMan().addToTeam(team, p))
                TeamSelector.updateInventories();
        }

    }

}
