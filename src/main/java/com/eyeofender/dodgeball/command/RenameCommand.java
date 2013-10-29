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

public class RenameCommand extends DodgeballExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, false, "dodgeball.arena.rename")) return true;

        if (args.length > 1) {
            Arena arena = Dodgeball.instance.getGameManager().getArena(args[0]);
            if (arena == null) return sendMsg(sender, ChatColor.RED + "There is no arena by that name!");

            if (arena.getPlayerCount() != 0) return sendMsg(sender, ChatColor.RED + "The arena must be empty.");
            if (Dodgeball.instance.getGameManager().getArena(args[1]) != null) return sendMsg(sender, ChatColor.RED + "There is already an arena by that name!");

            arena.setName(args[1]);
            return sendMsg(sender, ChatColor.GREEN + "Renamed arena " + args[0] + " to " + arena.getName());
        }

        sendMsg(sender, ChatColor.RED + "Please specify an arena and new name.");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], getCurrentArenas(), new ArrayList<String>());
        }

        return ImmutableList.of();
    }

}
