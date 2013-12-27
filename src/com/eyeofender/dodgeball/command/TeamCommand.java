package com.eyeofender.dodgeball.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.eyeofender.dodgeball.DodgeTeam;
import com.eyeofender.dodgeball.Dodgeball;

public class TeamCommand implements CommandExecutor {

    private Dodgeball plugin;

    public TeamCommand(Dodgeball plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Please specify a team.");
            return false;
        }

        DodgeTeam team = DodgeTeam.fromString(args[0]);

        if (team == null) {
            sender.sendMessage(ChatColor.RED + "The specified team does not exist!");
            return true;
        }

        plugin.getGame().switchTeam((Player) sender, team);
        return true;
    }
}
