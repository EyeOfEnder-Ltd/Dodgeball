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

        if (plugin.getGame().getArena() == null) return true;

        if (args.length < 1) {
            StringBuilder builder = new StringBuilder();
            for (DodgeTeam team : plugin.getGame().getArena().getTeams()) {
                builder.append(team.getChatColour() + team.toString());
                builder.append(ChatColor.GRAY + ", ");
            }
            builder.delete(0, builder.length() - 2);
            builder.append(".");

            sender.sendMessage(ChatColor.RED + "Please specify a team.");
            sender.sendMessage(ChatColor.AQUA + "Valid teams for this arena are:");
            sender.sendMessage(builder.toString());
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
