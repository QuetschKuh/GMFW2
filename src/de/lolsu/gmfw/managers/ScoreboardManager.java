package de.lolsu.gmfw.managers;

import de.lolsu.gmfw.api.SimpleScoreBoard;
import de.lolsu.gmfw.interfaces.ISimpleScoreBoardManager;
import de.lolsu.gmfw.main.Gamemode;
import de.lolsu.gmfw.main.Logger;
import de.lolsu.gmfw.statistics.StatManager;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

public class ScoreboardManager extends ISimpleScoreBoardManager {

    public List<String> statsDisplayed;

    public ScoreboardManager() {
        statsDisplayed = Gamemode.getConfig().getStringList("Stats.Displayed");
        createScoreboard();
    }

    @Override
    public void addPlayer(Player player) {
        scoreboard.addPlayer(player);
        setPlayerListHeaderFooter(player, Gamemode.getInstance().nameGamemode + "\n" + ChatColor.GRAY, ChatColor.GRAY + "\n" + Gamemode.getInstance().nameServer);
    }

    @Override
    public void createScoreboard() {
        if(scoreboard != null) scoreboard.stopUpdater();

        scoreboard = new SimpleScoreBoard(Gamemode.getInstance().name)
            .addSpace()
            .addLine(ChatColor.DARK_RED + "» Time left")
            .addVariable("TIME")
            .addSpace()
            .addVariable("PLAYERTITLE")
            .addVariable("PLAYERS")
            .addSpace()
            .addLine(ChatColor.AQUA + "» Your Stats");
        for(int i = 0; i < statsDisplayed.size(); i++)
            scoreboard.addVariable("STAT" + i, ChatColor.WHITE + Gamemode.getLang().getString("Stats.Short." + statsDisplayed.get(i)) + " – ");

        scoreboard
            .addSpace()
            .addLine(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------")
            .addLine(ChatColor.WHITE + "lolsu.de");

        scoreboard.addPlayers(Gamemode.getPlayerMan().getPlayers());
        scoreboard.startUpdater(this::getVariables);
    }

    @Override
    public HashMap<String, String> getVariables() {
        Player player = scoreboard.currentPlayer;
        int state = GamestateManager.state.ordinal();
        return new HashMap<String, String>() {{
            String time = "-:-";
            if(state == 0 && Gamemode.getStateMan().lobbyRoutine.isCountdown()) time = Gamemode.getStateMan().lobbyRoutine.countdown + "s";
            put("TIME", ChatColor.DARK_RED + time);
            put("PLAYERTITLE", ChatColor.DARK_GREEN + (state == 1 ? "» Players left" : "» Players"));
            put("PLAYERS", ChatColor.DARK_GREEN + "" + Bukkit.getOnlinePlayers().size());
            for(int i = 0; i < statsDisplayed.size(); i++)
                put("STAT" + i, ChatColor.WHITE + "" + (state == 0 ? StatManager.getGlobal(player, statsDisplayed.get(i)) : StatManager.getStat(player, statsDisplayed.get(i))));
        }};

    }

    /**
     * Sets the header and footer in the TAB-list (Player-list)
     * */
    public static void setPlayerListHeaderFooter(Player p, String header, String footer) {
        IChatBaseComponent icbcHeader = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + header + "\"}");
        IChatBaseComponent icbcFooter = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + footer + "\"}");
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        try {
            Field fieldHeader = packet.getClass().getDeclaredField("a");
            Field fieldFooter = packet.getClass().getDeclaredField("b");
            fieldHeader.setAccessible(true);
            fieldFooter.setAccessible(true);
            fieldHeader.set(packet, icbcHeader);
            fieldFooter.set(packet, icbcFooter);
        } catch (Exception exception) {
            Logger.error("Failed to set fields of header/footer.", exception);
        }

        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

}
