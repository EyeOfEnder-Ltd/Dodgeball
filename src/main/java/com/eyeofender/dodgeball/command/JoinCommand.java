package com.eyeofender.dodgeball.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.game.Arena;
import com.google.common.collect.ImmutableList;

public class JoinCommand extends DodgeballExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, true, "dodgeball.join")) return true;

        if (args.length > 0) {
            Arena arena = Dodgeball.instance.getGameManager().getArena(args[0]);
            if (arena == null) return sendMsg(sender, ChatColor.RED + "There is no arena by that name!");

            Player player = (Player) sender;
            if (!arena.addPlayer(player)) return true;
            player.teleport(arena.getLobby());
            if (arena.getStage() == 0 && arena.getPlayerCount() >= arena.getStartCount()) {
                arena.broadcast("Start count reached! The round will begin shortly.");
                arena.setStage(1);
            }
            return sendMsg(sender, Dodgeball.prefix + "Now playing in arena " + arena.getName() + "!");
        }

        sendMsg(sender, ChatColor.RED + "Please specify an arena.");
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
