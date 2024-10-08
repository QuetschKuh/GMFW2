package de.lolsu.gmfw.events;

import de.lolsu.gmfw.events.pregame.Lobby;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.managers.GamestateManager;
import de.lolsu.gmfw.phases.MapHandler;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeave implements Listener {

    public int toStart;
    public int toQuick;

    public PlayerJoinLeave() {
        toStart = Gamemode.getConfig().getInt("Countdown.ToStart");
        toQuick = Gamemode.getConfig().getInt("Countdown.ToQuick");
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (Gamemode.getPlayerMan().getPlayers().contains(e.getPlayer())) return;

        Gamemode.getPlayerMan().reset(e.getPlayer());

        // Gamestate specific handling
        switch (GamestateManager.state) {
            case PREGAME: {
                // Kick if lobby is full
                if (Gamemode.getPlayerMan().isLobbyFull()) {
                    e.setJoinMessage("");
                    player.kickPlayer(ChatColor.RED + "Lobby is already full!");
                    return;
                } else
                    e.setJoinMessage(ChatColor.AQUA + player.getDisplayName() + ChatColor.GREEN + " is ready to rumble!");

                // Register the player
                Gamemode.getPlayerMan().register(e.getPlayer());

                player.setGameMode(GameMode.ADVENTURE);
                player.teleport(Gamemode.getInstance().spawnLocation);

                // Fill inventory
                player.getInventory().setItem(0, Lobby.inventory);
                if (Gamemode.getPlayerMan().maxPerTeam > 1)
                    player.getInventory().setItem(2, Lobby.teaming);
                player.getInventory().setItem(4, Lobby.voting);
                player.getInventory().setItem(6, Lobby.rules);
                player.getInventory().setItem(8, Lobby.credits);

                // Start/accelerate countdown
                int players = Gamemode.getPlayerMan().getPlayersAmount();
                if (players == toStart) Gamemode.getStateMan().onCountdownStart();
                if (players == toQuick) Gamemode.getStateMan().onCountdownQuick();
                break;
            }

            case INGAME: {
                e.setJoinMessage(ChatColor.DARK_GRAY + player.getDisplayName() + " is now spectating the game.");

                // Register the player
                Gamemode.getPlayerMan().register(e.getPlayer());

                Gamemode.getPlayerMan().spec(player);
                player.teleport(MapHandler.getConfig().getLocation("Spawns.Spec", MapHandler.getWorld()));
                break;
            }

            case POSTGAME: {
                e.setJoinMessage("");
                player.kickPlayer("The game is over. Please wait for the server to restart.");
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if(!Gamemode.getPlayerMan().getPlayers().contains(e.getPlayer())) return;
        
        switch (GamestateManager.state) {
            case PREGAME:
                e.setQuitMessage(ChatColor.AQUA + player.getDisplayName() + ChatColor.DARK_AQUA + " didn't want to play anymore.");

                // Stop countdown
                int players = Gamemode.getPlayerMan().getPlayersAmount() - 1;
                if(players < toStart)
                    Gamemode.getStateMan().onCountdownCancel();
                break;

            case INGAME:
                if(Gamemode.getPlayerMan().isAlive(player)) {
                    Gamemode.getPlayerMan().kill(player);
                    e.setQuitMessage(ChatColor.AQUA + player.getDisplayName() + " disconnected.");
                } else {
                    e.setQuitMessage(ChatColor.DARK_GRAY + player.getDisplayName() + " is no longer spectating the game.");
                }
                break;

            case POSTGAME:
                e.setQuitMessage(ChatColor.DARK_GRAY + player.getDisplayName() + " left.");
                break;
        }

        // Unregister the player
        Gamemode.getPlayerMan().unregister(player);
    }

}
