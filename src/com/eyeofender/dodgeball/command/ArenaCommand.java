package com.eyeofender.dodgeball.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.eyeofender.dodgeball.Arena;
import com.eyeofender.dodgeball.DodgeTeam;
import com.eyeofender.dodgeball.Dodgeball;

public class ArenaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length < 1) return false;

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length < 2) return false;
            if (Arena.create(args[1])) {
                sender.sendMessage(ChatColor.GREEN + "Created arena: " + args[1] + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "An arena by that name does not exist!");
            }
        } else if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) return false;
            if (Arena.delete(args[1])) {
                sender.sendMessage(ChatColor.GREEN + "Deleted arena: " + args[1] + ".");
            } else {
                sender.sendMessage(ChatColor.RED + "An arena by that name does not exist!");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(Arena.getArenas().keySet().toString());
            return true;
        } else if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) return false;

            Arena arena = Arena.get(args[1]);

            if (arena == null) return true;
            if (!(sender instanceof Player)) return true;

            Player player = (Player) sender;

            if (args[2].equalsIgnoreCase("lobby")) {
                arena.setLobby(player.getLocation());
                return true;
            } else if (args[2].equalsIgnoreCase("name")) {
                arena.setName(args[3]);
                return true;
            } else if (args[2].equalsIgnoreCase("spawn")) {
                arena.addSpawnPoint(DodgeTeam.fromString(args[3]), player.getLocation());
                return true;
            } else if (args[2].equalsIgnoreCase("line")) {
                if (args.length > 3) {
                    if (args[3].equalsIgnoreCase("cancel")) {
                        if (arena.clearPendingLocation(player)) {
                            player.sendMessage(ChatColor.GREEN + "You you have no pending line postitions in that arena!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Cleared your pending line position for arena: " + arena.getName());
                        }
                        return true;
                    }
                    return false;
                }

                if (arena.addPendingLocation(player)) {
                    player.sendMessage(ChatColor.GREEN + "You have added a new boundary line to the arena: " + arena.getName());
                } else {
                    player.sendMessage(ChatColor.GREEN + "Set the first position for a boundary in arena " + arena.getName() + ".");
                    player.sendMessage(ChatColor.GREEN + "Repeat for a second position, or clear your pending position with '/arena set " + arena.getName() + " line cancel'.");
                }
                return true;
            } else if (args[2].equalsIgnoreCase("active")) {
                Dodgeball.getInstance().getGame().setArena(Arena.get(args[3]));
                return true;
            }
        } else if (args[0].equalsIgnoreCase("clear")) {
            if (args.length < 3) return false;

            Arena arena = Arena.get(args[1]);

            if (args[2].equalsIgnoreCase("spawn")) {
                arena.clearSpawnPoints(DodgeTeam.fromString(args[3]));
                sender.sendMessage(ChatColor.GREEN + "Spawns cleared!");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("save")) {
            if (args.length < 2) return false;
            Arena arena = Arena.get(args[1]);
            if (arena != null) {
                arena.save();
            }
        } else if (args[0].equalsIgnoreCase("check")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Please specify an arena.");
                return true;
            }

            Arena arena = Arena.get(args[1]);

            if (arena == null) {
                sender.sendMessage(ChatColor.RED + "An arena by that name does not exist.");
                return true;
            }

            sender.sendMessage(arena.getName());
            sender.sendMessage(arena.isSetup() ? ChatColor.GREEN + "Ready" : ChatColor.RED + "Not Ready");
            sender.sendMessage(arena.getLobby() != null ? "Lobby Set" : "Lobby Unset");
            sender.sendMessage(arena.getTeams().isEmpty() ? "No teams" : arena.getTeams().size() + " Teams");

            for (DodgeTeam team : arena.getTeams()) {
                if (arena.getSpawnPoints(team) == null) continue;
                sender.sendMessage(team.getChatColour() + team.getDisplayName() + ": " + arena.getSpawnPoints(team).length + " spawns");
            }
        }

        return false;
    }
}
