package de.lolsu.gmfw.phases.routines;

import de.lolsu.gmfw.api.Chars;
import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.managers.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class LobbyRoutine extends BukkitRunnable {

    /** Internal counter used for determining timings for info texts and action bars */
    private int internal = 30;

    // Info texts
    private final List<String> texts;
    private List<String> textsRemaining;

    // Waiting
    private final int start;
    private final int quick;
    private final int full;

    // Countdown
    public final int countdownMax;
    public int countdown = -1;

    /**
     * No {@link #runTaskTimer(Plugin, long, long)} needed! Automatically run synchronously at 0L delay and 20L period on creation!
     * */
    public LobbyRoutine() {
        texts = Gamemode.getLang().getStringList("InfoTexts");

        start = Gamemode.getConfig().getInt("Countdown.ToStart");
        quick = Gamemode.getConfig().getInt("Countdown.ToQuick");
        full = Gamemode.getPlayerMan().maxPlayers;

        countdownMax = Gamemode.getConfig().getInt("Countdown.Length", 150);

        runTaskTimer(Gamemode.getPlugin(), 0L, 20L);
    }

    @Override
    public void run() {
        internal--;

        // Info texts
        if(internal == 0) {
            if(textsRemaining == null || textsRemaining.isEmpty()) textsRemaining = new ArrayList<>(texts);
            String message = textsRemaining.get(Gamemode.random.nextInt(textsRemaining.size()));
            Messenger.broadcast(message);
            textsRemaining.remove(message);
        }

        // Waiting action bar
        if(internal % 5 == 0) {
            int players = Gamemode.getPlayerMan().getPlayersAmount();
            if(players < start)         Messenger.broadcastActionBars("Waiting for players... (" + players + "/" + start + ")");
            else if(players < quick)    Messenger.broadcastActionBars("Waiting for more players... (" + players + "/" + quick + ")");
            else                        Messenger.broadcastActionBars("Starting... (" + players + "/" + full + ")");
        }

        // Countdown
        if(isCountdown()) {
            countdown--;

            PlayerManager.forAll(p -> {
                p.setLevel(countdown);
                p.setExp((float) countdown / countdownMax);
            });

            if(countdown <= 20) {
                ChatColor cc = (countdown > 5 ? ChatColor.GREEN : countdown > 3 ? ChatColor.YELLOW : ChatColor.RED);
                String subtitle =
                        countdown > 10 ? "Voting ends in " + (countdown - 10) + " seconds!" :
                                countdown == 10 ? "Voting ended!" : "Game starting!";
                Messenger.broadcastTitleBar(
                        cc + Chars.blackCircledNumbers[countdown],
                        cc + subtitle,
                        0, 20, 20
                );
            }

            if(countdown == 10)
                Gamemode.getStateMan().onVotingEnd();

            if(countdown == 0) {
                Gamemode.getStateMan().onGameStart();
                cancel();
            }
        }

        if(internal == 0) internal = 30;
    }

    public boolean isCountdown() {
        return countdown >= 0;
    }

    public boolean isStarting() {
        return isCountdown() && countdown <= 20;
    }

}
