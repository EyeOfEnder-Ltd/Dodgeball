package com.eyeofender.dodgeball.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.eyeofender.dodgeball.Dodgeball;

public class SetGlobalLobbyCommand extends DodgeballExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!isAllowed(sender, true, "dodgeball.setgloballobby")) return true;
        Dodgeball.instance.getGameManager().setGlobalLobby(((Player) sender).getLocation());
        return sendMsg(sender, ChatColor.GREEN + "Updated the global lobby!");
    }

}
