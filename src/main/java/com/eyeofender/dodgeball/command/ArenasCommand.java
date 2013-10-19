package com.eyeofender.dodgeball.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.game.Arena;

public class ArenasCommand extends DodgeballExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!isAllowed(sender, false, "dodgeball.arena.list")) return true;

        StringBuffer sb = new StringBuffer();
        for (Arena arena : Dodgeball.instance.getGameManager().getArenas()) {
            String name = arena.getName() + ChatColor.RESET + ", ";

            switch (arena.getStage()) {
                case 0: // Waiting
                    sb.append(ChatColor.GREEN + name);
                    break;
                case 1: // Starting
                    sb.append(ChatColor.YELLOW + name);
                    break;
                case 2: // In Game
                    sb.append(ChatColor.GRAY + name);
                    break;
                default:
                    sb.append(name);
                    break;
            }
        }

        sender.sendMessage(getPageHeader("Arenas"));
        sender.sendMessage(ChatColor.GREEN + "Waiting" + ChatColor.RED + " : " + ChatColor.YELLOW + "Starting" + ChatColor.RED + " : " + ChatColor.GRAY + "In Game");
        sender.sendMessage(sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "No Arenas found.  Use \"/create <arena>\" to add one.");
        return true;
    }

}
