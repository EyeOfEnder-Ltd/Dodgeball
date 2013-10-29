package com.eyeofender.dodgeball.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.game.Arena;

public class CreateCommand extends DodgeballExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAllowed(sender, false, "dodgeball.arena.create")) return true;

        if (args.length > 0) {
            String name = args[0];
            boolean success = Dodgeball.instance.getGameManager().addArena(new Arena(args[0]));
            return sendMsg(sender, success ? ChatColor.GREEN + "Created a new arena: " + name : ChatColor.RED + "There is already an arena by that name!");
        }

        sendMsg(sender, ChatColor.RED + "Please specify an arena.");
        return false;
    }

}
