package com.eyeofender.dodgeball.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.game.GameManager;

public class SpawnCommand extends DodgeballExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!isAllowed(sender, true)) return true;
        Player player = (Player) sender;
        GameManager manager = Dodgeball.instance.getGameManager();

        if (manager.getArena(player) != null || manager.getArenaFromSpectator(player) != null) {
            return sendMsg(sender, ChatColor.RED + "You can't teleport while in an arena. You must /leave first.");
        }

        player.teleport(manager.getGlobalLobby());
        return true;
    }

}
