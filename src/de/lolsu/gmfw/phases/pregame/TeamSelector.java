package de.lolsu.gmfw.phases.pregame;

import de.lolsu.gmfw.api.ItemStacks;
import de.lolsu.gmfw.api.Skulls;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.managers.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TeamSelector {

    public static final String INVENTORY_NAME = ChatColor.AQUA + "Team with somebody!";
    private static final String[] slots = {"", "5", "2:6", "2:4:6", "1:3:5:7", "0:2:4:6:8", "1:2:3:5:6:7", "1:2:3:4:5:6:7", "0:1:2:3:5:6:7:8", "0:1:2:3:4:5:6:7:8"};

    private Player player;
    private Inventory inventory;

    public TeamSelector(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, INVENTORY_NAME);

        PlayerManager pm = Gamemode.getPlayerMan();

        // Teams and the players in them
        String[] slots = TeamSelector.slots[pm.teamAmount].split(":");
        for(int i = 0; i < pm.teamAmount; i++) {
            boolean teamFull = pm.getTeams().get(i).isFull();
            boolean teamOfPlayer = pm.getTeam(player) != null && pm.getTeam(player).id == i;

            List<String> lore = new ArrayList<>(pm.getTeams().get(i).names(ChatColor.GRAY + ""));
            lore.add(0, "");
            lore.add(0,
                    teamOfPlayer ? ChatColor.RED + "Left click to leave." :
                            teamFull ? ChatColor.RED + "Team is already full!" :
                                    ChatColor.GREEN + "Left click to join!"
            );

            ItemStack item = ItemStacks.create(
                    pm.getTeams().get(i).isFull() ? Material.MINECART : Material.BOAT,
                    ChatColor.WHITE + "Team " + (i + 1), lore
            );
            if(teamOfPlayer) ItemStacks.addGlint(item);

            inventory.setItem(Integer.parseInt(slots[i]), item);
        }

        // Divider
        ItemStack divider = ItemStacks.create(Material.STAINED_GLASS_PANE, ChatColor.GRAY + "Players not assigned");
        for(int i = 9; i < 18; i++)
            inventory.setItem(i, divider);

        // Player who haven't selected a team
        List<Player> playersRemaining = new ArrayList<>(Bukkit.getOnlinePlayers());
        playersRemaining.removeAll(pm.playerTeamMap.keySet());
        for(int i = 18; i < 27; i++) {
            if((i - 18) >= playersRemaining.size()) break;
            Player rp = playersRemaining.get(i - 18);
            inventory.setItem(i, Skulls.getPlayerSkull(ChatColor.WHITE + rp.getDisplayName(), Collections.singletonList(ChatColor.GRAY + "This player has not yet chosen a team"), rp.getName()));
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    /** Updates the teaming inventory of all players */
    public static void updateInventories() {
        // Replace inventory contents of all players with the teaming inventory open
        PlayerManager.forAll(p -> {
            if (p.getOpenInventory() != null && Objects.equals(p.getOpenInventory().getTitle(), INVENTORY_NAME))
                p.getOpenInventory().getTopInventory().setContents(new TeamSelector(p).getInventory().getContents());
        });
    }

}
