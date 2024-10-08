package de.lolsu.gmfw.commands;

import de.lolsu.gmfw.api.Messenger;
import de.lolsu.gmfw.config.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetMap implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player)) return false;
        Messenger.send((Player) sender, ChatColor.AQUA + "This is " + ChatColor.GOLD + ConfigUtil.fancy(((Player) sender).getWorld().getName()));
        return true;
    }

}
