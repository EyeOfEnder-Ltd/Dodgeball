package com.eyeofender.dodgeball.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.game.Arena;
import com.google.common.collect.ImmutableList;

public class SaveCommand extends DodgeballExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, false, "dodgeball.arena.save")) return true;

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("all")) {
                Dodgeball.instance.getGameManager().saveArenas();
                return sendMsg(sender, ChatColor.GREEN + "Saved changes to all arenas.");
            }

            Arena arena = Dodgeball.instance.getGameManager().getArena(args[0]);
            if (arena == null) return sendMsg(sender, ChatColor.RED + "There is no arena by that name!");

            arena.save();
            return sendMsg(sender, ChatColor.GREEN + "Saved changes to arena: " + arena.getName());
        }

        sendMsg(sender, ChatColor.RED + "Please specify an arena or all.");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            List<String> list = getCurrentArenas();
            list.add("all");
            return StringUtil.copyPartialMatches(args[0], list, new ArrayList<String>());
        }

        return ImmutableList.of();
    }

}
