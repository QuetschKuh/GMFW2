package de.lolsu.gmfw.phases.pregame;

import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.config.ConfigUtil;
import de.lolsu.gmfw.managers.PlayerManager;
import de.lolsu.gmfw.phases.MapHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class VoteManager {

    public static final String INVENTORY_NAME = ChatColor.AQUA + "Vote for a map!";

    List<String> maps;
    public Map<String, Integer> mapVotes = new HashMap<>();
    public Map<Player, Integer> playerVotes = new HashMap<>();

    public VoteManager() {
        // Get and shuffle maps
        maps = Arrays.asList(MapHandler.getMaps());
        Collections.shuffle(maps);
        maps = new ArrayList<>(maps.subList(0, 5));
        maps.add("RANDOM");

        for (String map : maps) mapVotes.put(map, 0);
    }

    /**
     * Sets a player vote
     * */
    public void setPlayerVote(Player p, int index) {
        String map = maps.get(index);
        if (!playerVotes.containsKey(p) || playerVotes.get(p) != index) {
            if(playerVotes.containsKey(p)) {
                String lastMap = maps.get(playerVotes.get(p));
                mapVotes.replace(lastMap, mapVotes.get(lastMap) - 1);
            }
            mapVotes.replace(map, mapVotes.get(map) + 1);
            playerVotes.put(p, index);
        } else {
            Messenger.send(p, ChatColor.RED + "You already voted for this Map!");
        }
    }

    /**
     * Removes a player vote
     * */
    public void removePlayerVote(Player p) {
        if(!playerVotes.containsKey(p)) return;
        String map = maps.get(playerVotes.get(p));
        mapVotes.replace(map, mapVotes.get(map) - 1);
        playerVotes.remove(p);
    }

    /**
     * Gets the map that currently has the most votes
     * */
    public String getMostVoted() {
        // What Map is currently the highest voted and how many votes that map has
        List<String> mostVotedList = new ArrayList<>();
        int voteCount = 0;

        // Iterate through every map change the voted var depending on if it was the highest voted
        for(Map.Entry<String, Integer> voted : mapVotes.entrySet()) {
            if(voted.getValue() < voteCount) continue;
            if(voted.getValue() > voteCount) mostVotedList.clear();
            mostVotedList.add(voted.getKey());
            voteCount = voted.getValue();
        }

        Random r = new Random();
        String mostVoted = mostVotedList.get(r.nextInt(mostVotedList.size()));
        if(mostVoted.equals("RANDOM")) return maps.get(r.nextInt(maps.size() - 1));
        else return mostVoted;
    }

    /**
     * Gets the voting inventory for a certain player, including the votes already cast by others and themselves
     * */
    public Inventory getSelector(Player p) {

        // The inventory
        Inventory inv = Bukkit.createInventory(null, 54, INVENTORY_NAME);
        int totalVotes = mapVotes.values().stream().mapToInt(Integer::intValue).sum();

        // Iterate through all maps for the main panes
        for(int i = 0; i < 6; i++) {
            // Calculate variables
            String map = maps.get(i);
            int votes = mapVotes.get(map);
            boolean playerVoted = playerVotes.containsKey(p) && playerVotes.get(p) == i;

            // Set the main item
            ItemStack is = new ItemStack(i == 5 ? Material.REDSTONE_BLOCK : playerVoted ? Material.MAP : Material.EMPTY_MAP, votes);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName(ChatColor.GOLD + ConfigUtil.fancy(map));
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            List<String> lore = new ArrayList<>();
            lore.add(playerVoted ? ChatColor.GOLD + "You already voted for this map!" : ChatColor.AQUA + "Vote for this Map!");
            lore.add(ChatColor.GRAY + "Votes: " + ChatColor.GREEN + votes);
            im.setLore(lore);
            is.setItemMeta(im);
            if(playerVoted) is.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
            inv.setItem(i * 9, is);

            // The panes for votes or not votes
            ItemStack voteItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
            ItemMeta voteMeta = voteItem.getItemMeta();
            voteMeta.setDisplayName(playerVoted ? ChatColor.GOLD + "You already voted for this map!" : ChatColor.AQUA + "Vote for this Map!");
            voteMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            voteItem.setItemMeta(voteMeta);
            if(playerVoted) voteItem.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
            ItemStack bgItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
            ItemMeta bgMeta = bgItem.getItemMeta();
            bgMeta.setDisplayName(playerVoted ? ChatColor.GOLD + "You already voted for this map!" : ChatColor.DARK_AQUA + "Vote for this Map!");
            bgItem.setItemMeta(bgMeta);

            // Calculate how many panes should be coloured (and avoid division by zero)
            int a = (int) (((float) votes / (totalVotes <= 0 ? 1f : (float) totalVotes)) * 8f);

            // Iterate through every slot where a pane should be
            for(int c = 1; c < 9; c++) {
                if(c <= a) inv.setItem(i * 9 + c, voteItem);
                else inv.setItem(i * 9 + c, bgItem);
            }

        }

        return inv;
    }

    /** Updates the voting inventory of all players */
    public void updateInventories() {
        // Replace inventory contents of all players with the voting inventory open
        PlayerManager.forAll(p -> {
            if(p.getOpenInventory() != null && Objects.equals(p.getOpenInventory().getTitle(), INVENTORY_NAME))
                p.getOpenInventory().getTopInventory().setContents(getSelector(p).getContents());
        });
    }

}
