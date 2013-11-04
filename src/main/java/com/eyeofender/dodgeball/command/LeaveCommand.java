package com.eyeofender.dodgeball.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.game.Arena;
import com.eyeofender.dodgeball.game.GameManager;

public class LeaveCommand extends DodgeballExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!isAllowed(sender, true)) return true;
        Player player = (Player) sender;
        GameManager manager = Dodgeball.instance.getGameManager();
        Arena arena = manager.getArena(player);

        if (arena == null) {
            arena = manager.getArenaFromSpectator(player);
            if (arena == null) return sendMsg(sender, ChatColor.RED + "You aren't in an arena!");
            arena.removeSpectator(player, true);
            return sendMsg(sender, ChatColor.GREEN + "No longer spectating in arena: " + arena.getName());
        }

        arena.removePlayer(player, false, true);
        return sendMsg(sender, ChatColor.GREEN + "Left arena: " + arena.getName());
    }

}
