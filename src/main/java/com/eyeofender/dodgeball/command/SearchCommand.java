package com.eyeofender.dodgeball.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.game.Arena;
import com.google.common.collect.ImmutableList;

public class SearchCommand extends DodgeballExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, false, "dodgeball.search")) return true;

        if (args.length > 0) {
            Player lookup = Bukkit.getPlayer(args[0]);
            if (lookup == null) return sendMsg(sender, ChatColor.RED + "Player " + args[0] + " is not online.");
            Arena arena = Dodgeball.instance.getGameManager().getArena(lookup);
            return sendMsg(sender, "Player " + lookup.getName() + " is " + (arena == null ? "not in an arena." : "in arena: " + arena.getName() + "."));
        }

        sendMsg(sender, ChatColor.RED + "Please specify a player name.");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            ArrayList<String> online = new ArrayList<String>();
            for (Player player : sender.getServer().getOnlinePlayers()) {
                online.add(player.getName());
            }
            return StringUtil.copyPartialMatches(args[0], online, new ArrayList<String>());
        }

        return ImmutableList.of();
    }

}
