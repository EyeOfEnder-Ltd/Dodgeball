package com.eyeofender.dodgeball;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.eyeofender.dodgeball.game.Arena;
import com.eyeofender.dodgeball.game.Team;

public class Commands {

    private final String[] commands = { "create <arena>", "delete <arena>", "save <arena/all>", "rename <arena> <name>", "join <arena>", "spectate <arena>", "leave", "team <team>", "setgloballobby",
            "setlobby <arena>", "addspawn <arena> <team>", "clearspawns <arena> <team/all>", "setline <arena> [cancel]", "clearlines <arena>", "arenalist", "arenainfo <arena>", "playerlist <arena>",
            "search <username>", "eject <player>/(all <arena>)", "fstart <arena> [delay]", "fstop <arena> [delay]", "addball <arena> [team]", "clearballs <arena>", "friendlyfire <arena> <on/off>",
            "setballlimit <arena> <limit>", "setlimit <arena> <limit>", "setstartcount <arena> <count>" };

    public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
        label = label.toLowerCase();
        if (label.equals("dodge") || label.equals("dodgeball") || label.equals("db")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("version"))
                return sendMsg(sender, "Running version " + ChatColor.LIGHT_PURPLE + Dodgeball.instance.getDescription().getVersion() + ChatColor.GRAY + " by " + ChatColor.BLUE + "LimeByte.");
            if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                StringBuffer sb = new StringBuffer(ChatColor.GRAY + "[]" + ChatColor.RED + "===" + ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Arena Commands" + ChatColor.GRAY + "]" + ChatColor.RED
                        + "===" + ChatColor.GRAY + "[]");
                for (int i = 0; i < commands.length; i++)
                    sb.append("\n/" + (i % 2 == 0 ? ChatColor.AQUA : ChatColor.DARK_AQUA) + commands[i]);
                sender.sendMessage(sb.toString());
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                if (!authorizeSender(sender, false, "dodgeball.reload")) return true;
                Dodgeball.instance.reloadConfig();
                return sendMsg(sender, ChatColor.GREEN + "Reloaded the config.");
            }
        }

        if (label.equals("spawn") || label.equals("lobby") || label.equals("globby") || label.equals("globallobby")) {
            if (!authorizeSender(sender, true, null)) return true;
            Player player = (Player) sender;
            if (Dodgeball.instance.getGameManager().getArena(player) != null || Dodgeball.instance.getGameManager().getArenaFromSpectator(player) != null)
                return sendMsg(sender, ChatColor.RED + "You can't teleport while in an arena. You must /leave first.");
            ((Player) sender).teleport(Dodgeball.instance.getGameManager().getGlobalLobby());
            return true;
        }

        if (label.equals("arenas") || label.equals("arenalist")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.list")) return true;
            sender.sendMessage(ChatColor.GRAY + "[]" + ChatColor.RED + "===" + ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Arenas" + ChatColor.GRAY + "]" + ChatColor.RED + "===" + ChatColor.GRAY
                    + "[]\n" + ChatColor.GREEN + "Waiting: " + ChatColor.GRAY + Arrays.toString(Dodgeball.instance.getGameManager().getArenasInStage(0)) + ChatColor.YELLOW + "\nStarting: "
                    + ChatColor.GRAY + Arrays.toString(Dodgeball.instance.getGameManager().getArenasInStage(1)) + ChatColor.RED + "\nIn game: " + ChatColor.GRAY
                    + Arrays.toString(Dodgeball.instance.getGameManager().getArenasInStage(2)));
            return true;
        }

        if (label.equals("leave")) {
            if (!authorizeSender(sender, true, null)) return true;
            Player player = (Player) sender;
            Arena arena = Dodgeball.instance.getGameManager().getArena(player);
            boolean isSpectator = false;
            if (arena == null) {
                arena = Dodgeball.instance.getGameManager().getArenaFromSpectator(player);
                if (arena == null) return sendMsg(sender, ChatColor.RED + "You aren't in an arena!");
                isSpectator = true;
            }
            if (isSpectator) {
                arena.removeSpectator(player, true);
                return sendMsg(sender, ChatColor.GREEN + "No longer spectating in arena: " + arena.getName());
            }
            arena.removePlayer(player, false, true);
            return sendMsg(sender, ChatColor.GREEN + "Left arena: " + arena.getName());
        }

        if (label.equals("setgloballobby")) {
            if (!authorizeSender(sender, true, "dodgeball.setgloballobby")) return true;
            Dodgeball.instance.getGameManager().setGlobalLobby(((Player) sender).getLocation());
            return sendMsg(sender, ChatColor.GREEN + "Updated the global lobby!");
        }

        if (args.length < 1) return sendMsg(sender, ChatColor.RED + "Too few arguments.");
        String name = args[0];

        if (label.equals("save")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.save")) return true;
            if (args[0].equalsIgnoreCase("all")) {
                Dodgeball.instance.getGameManager().saveArenas();
                return sendMsg(sender, ChatColor.GREEN + "Saved changes to all arenas.");
            }
            Arena arena = Dodgeball.instance.getGameManager().getArena(args[0]);
            if (arena == null) return sendMsg(sender, ChatColor.RED + "There is no arena by that name!");
            arena.save();
            return sendMsg(sender, ChatColor.GREEN + "Saved changes to arena: " + arena.getName());
        }

        if (label.equals("create") || label.equals("add")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.create")) return true;
            return sendMsg(sender, Dodgeball.instance.getGameManager().addArena(new Arena(name)) ? ChatColor.GREEN + "Created a new arena: " + name : ChatColor.RED
                    + "There is already an arena by that name!");
        }

        if (label.equals("search")) {
            if (!authorizeSender(sender, false, "dodgeball.search")) return true;
            Player lookup = Dodgeball.instance.getServer().getPlayer(name);
            if (lookup == null) return sendMsg(sender, ChatColor.RED + "Player " + name + " is not online.");
            Arena arena = Dodgeball.instance.getGameManager().getArena(lookup);
            return sendMsg(sender, "Player " + lookup.getName() + " is " + (arena == null ? "not in an arena." : "in arena: " + arena.getName() + "."));
        }

        if (label.equals("eject")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.eject")) return true;
            if (args[0].equalsIgnoreCase("all")) {
                if (args.length < 2) return sendMsg(sender, ChatColor.RED + "You must specify an arena.");
                Arena arena = Dodgeball.instance.getGameManager().getArena(args[1]);
                if (arena == null) return sendMsg(sender, ChatColor.RED + "There is no arena by that name!");
                arena.broadcast(ChatColor.DARK_RED + "An administrator has closed the arena.");
                arena.clearSpectators();
                arena.clearPlayers();
                return sendMsg(sender, ChatColor.GREEN + "Emptied arena: " + arena.getName());
            }
            Player player = Dodgeball.instance.getServer().getPlayer(args[0]);
            if (player == null) return sendMsg(sender, ChatColor.RED + "That player isn't online!");
            Arena arena = Dodgeball.instance.getGameManager().getArena(player);
            if (arena != null) {
                arena.broadcast(player.getName() + " was kicked from the arena!");
                arena.removePlayer(player, false, true);
            } else {
                arena = Dodgeball.instance.getGameManager().getArenaFromSpectator(player);
                if (arena == null) return sendMsg(sender, player.getName() + " isn't in an arena.");
                arena.removeSpectator(player, true);
            }
            player.sendMessage(Dodgeball.prefix + ChatColor.DARK_RED + "You were kicked from the arena.");
            return sendMsg(sender, ChatColor.GREEN + "Kicked " + player.getName() + " from arena: " + arena.getName());
        }

        if (label.equals("team")) {
            if (!authorizeSender(sender, true, "dodgeball.arena.team")) return true;
            Player player = (Player) sender;
            Arena arena = Dodgeball.instance.getGameManager().getArena(player);
            if (arena == null) return sendMsg(sender, ChatColor.RED + "You must be in an arena to choose a team!");
            if (arena.getStage() != 1) return sendMsg(sender, ChatColor.RED + "Team selection is only available while the round is starting.");
            Team team = Team.fromString(name);
            if (!arena.containsTeam(team)) {
                String teamList = Arrays.toString(arena.getTeams());
                return sendMsg(sender, ChatColor.RED + "No such team exists in your arena! " + ChatColor.GRAY + "Available teams: " + teamList.substring(1, teamList.length() - 1));
            }
            if (team.equals(arena.getTeam(player))) return sendMsg(sender, ChatColor.RED + "You are already on the " + team + " team!");
            if (arena.getTeamPlayerCount(team) >= (int) Math.ceil((double) arena.getPlayerCount() / (double) arena.getTeamCount()))
                return sendMsg(sender, ChatColor.RED + "Could not join team " + team + " due to player imbalance.");
            arena.addPlayer(player, team, false, false);
            return sendMsg(sender, "Joined the " + team + " team!");
        }

        Arena arena = Dodgeball.instance.getGameManager().getArena(name);
        if (arena == null) return sendMsg(sender, ChatColor.RED + "There is no arena by that name!");

        if (label.equals("delete") || label.equals("remove")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.delete")) return true;
            if (arena.getPlayerCount() > 0) return sendMsg(sender, ChatColor.RED + "An arena can only be deleted when empty.");
            Dodgeball.instance.getGameManager().deleteArena(arena);
            return sendMsg(sender, ChatColor.GREEN + "Successfully removed arena: " + arena.getName());
        }

        if (label.equals("join")) {
            if (!authorizeSender(sender, true, "dodgeball.join")) return true;
            Player player = (Player) sender;
            if (!arena.addPlayer(player)) return true;
            player.teleport(arena.getLobby());
            player.sendMessage(Dodgeball.prefix + "Now playing in arena " + arena.getName() + "!");
            if (arena.getStage() == 0 && arena.getPlayerCount() >= arena.getStartCount()) {
                arena.broadcast("Start count reached! The round will begin shortly.");
                arena.setStage(1);
            }
            return true;
        }

        if (label.equals("spectate")) {
            if (!authorizeSender(sender, true, "dodgeball.spectate")) return true;
            Player player = (Player) sender;
            if (Dodgeball.instance.getGameManager().getArena(player) != null || Dodgeball.instance.getGameManager().getArenaFromSpectator(player) != null)
                return sendMsg(sender, ChatColor.RED + "You are already in an arena!");
            if (arena.getStage() != 2) return sendMsg(sender, ChatColor.RED + "There is no round in progress in that arena.");
            arena.addSpectator(player);
            player.teleport(arena.getLobby());
            player.sendMessage(Dodgeball.prefix + "Now spectating in arena " + arena.getName() + "!");
            return true;
        }

        if (label.equals("setlobby")) {
            if (!authorizeSender(sender, true, "dodgeball.arena.setlobby")) return true;
            if (arena.getPlayerCount() > 0) return sendMsg(sender, ChatColor.RED + "An arena can only be edited when empty.");
            arena.setLobby(((Player) sender).getLocation());
            return sendMsg(sender, ChatColor.GREEN + "Updated the lobby for arena: " + arena.getName());
        }

        if (label.equals("setline")) {
            if (!authorizeSender(sender, true, "dodgeball.arena.setline")) return true;
            if (arena.getPlayerCount() > 0) return sendMsg(sender, ChatColor.RED + "An arena can only be edited when empty.");
            Player player = (Player) sender;
            if (args.length > 1) {
                if (!arena.clearPendingLocation(player)) return sendMsg(sender, ChatColor.RED + "You you have no pending line postitions in that arena!");
                return sendMsg(sender, ChatColor.GREEN + "Cleared your pending line position for arena: " + arena.getName());
            }
            if (arena.addPendingLocation(player)) return sendMsg(sender, "You have added a new boundary line to the arena: " + arena.getName());
            return sendMsg(sender, ChatColor.GREEN + "Set the first position for a boundary in arena " + arena.getName()
                    + ". Repeat for a second position to register a boundary, or clear your pending position with " + ChatColor.AQUA + "/setline " + arena.getName() + " cancel");
        }

        if (label.equals("clearlines")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.clearlines")) return true;
            if (arena.getPlayerCount() > 0) return sendMsg(sender, ChatColor.RED + "An arena can only be edited when empty.");
            arena.clearMidlines();
            return sendMsg(sender, ChatColor.GREEN + "Cleared all midlines for arena: " + arena.getName());
        }

        if (label.equals("addball")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.balls.add")) return true;
            if (arena.getStage() != 2) return sendMsg(sender, ChatColor.RED + "The round has not yet started in that arena!");
            if (arena.getBallCount() + 1 >= arena.getBallLimit()) return sendMsg(sender, ChatColor.RED + "The number of dodgeballs in arena " + arena.getName() + " has reached the limit.");
            if (args.length == 1) { // No team has been specified to receive the
                                    // dodgeball
                arena.spawnDodgeball(Team.NONE, false);
                return sendMsg(sender, ChatColor.GREEN + "Spawned a dodgeball into arena: " + arena.getName());
            }
            Team recipient = Team.fromString(args[1]);
            if (recipient.equals(Team.NONE) || arena.getTeamPlayerCount(recipient) == 0) return sendMsg(sender, ChatColor.RED + "Invalid team!");
            arena.spawnDodgeball(recipient, false);
            return sendMsg(sender, ChatColor.GREEN + "Spawned a dodgeball into arena: " + arena.getName());
        }

        if (label.equals("clearballs")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.balls.clear")) return true;
            return sendMsg(sender, arena.clearDodgeballs() ? ChatColor.GREEN + "Cleared all dodgeballs in arena: " + arena.getName() : ChatColor.RED
                    + "Dodgeballs can only be cleared from arenas that have at least one team spawnpoint.");
        }

        if (label.equals("arenainfo")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.info")) return true;
            String teamList = Arrays.toString(arena.getTeams());
            sender.sendMessage(ChatColor.GRAY + "[]" + ChatColor.RED + "===" + ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Info: " + arena.getName() + ChatColor.GRAY + "]" + ChatColor.RED + "==="
                    + ChatColor.GRAY + "[]\n" + "Stage: " + arena.getStageName() + ChatColor.GRAY + "\nPlayers: " + ChatColor.LIGHT_PURPLE + arena.getPlayerCount() + ChatColor.GRAY + "/"
                    + ChatColor.LIGHT_PURPLE + arena.getPlayerLimit() + ChatColor.GRAY + "\nPlayers needed for auto-start: " + ChatColor.LIGHT_PURPLE + arena.getStartCount() + ChatColor.GRAY
                    + "\nBall limit: " + ChatColor.LIGHT_PURPLE + arena.getBallLimit() + ChatColor.GRAY + "\nFriendly fire: "
                    + (arena.isFriendlyFireEnabled() ? ChatColor.GREEN + "on" : ChatColor.RED + "off") + ChatColor.GRAY + "\nTeams: " + ChatColor.DARK_AQUA
                    + teamList.substring(1, teamList.length() - 1));
            return true;
        }

        if (label.equals("players") || label.equals("playerlist")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.playerlist")) return true;
            String players = Arrays.toString(arena.getPlayers().toArray(new String[arena.getPlayerCount()]));
            sender.sendMessage(ChatColor.GRAY + "[]" + ChatColor.RED + "===" + ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Players in " + arena.getName() + ChatColor.GRAY + "]" + ChatColor.RED
                    + "===" + ChatColor.GRAY + "[]\n" + (players.length() > 2 ? players.substring(1, players.length() - 1) : ""));
            return true;
        }

        if (label.equals("fstart") || label.equals("forcestart")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.forcestart")) return true;
            if (arena.getStage() == 2) return sendMsg(sender, ChatColor.RED + "A round is already in progress in that arena.");
            if (arena.getPlayerCount() < 2) return sendMsg(sender, ChatColor.RED + "A round cannot be started in an arena with less than two players.");
            int delay = 1;
            if (args.length == 2) {
                try {
                    delay = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    delay = 0;
                }
                if (delay < 1) return sendMsg(sender, ChatColor.RED + "The delay must be at least one second.");
            }
            arena.setStage(1);
            arena.setTimerValue(delay);
            return sendMsg(sender, ChatColor.GREEN + "Forced arena " + arena.getName() + " to start " + (delay == 1 ? "immediately." : "in " + delay + " seconds."));
        }

        if (label.equals("fstop") || label.equals("forcestop")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.forcestart")) return true;
            if (arena.getStage() != 2) return sendMsg(sender, ChatColor.RED + "No round is in progress in that arena.");
            int delay = 1;
            if (args.length == 2) {
                try {
                    delay = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    delay = 0;
                }
                if (delay < 1) return sendMsg(sender, ChatColor.RED + "The delay must be at least one second.");
            }
            arena.setTimerValue(delay);
            return sendMsg(sender, ChatColor.GREEN + "Forced arena " + arena.getName() + " to stop " + (delay == 1 ? "immediately." : "in " + delay + " seconds."));
        }

        if (args.length < 2) return sendMsg(sender, ChatColor.RED + "Too few arguments.");

        if (label.equals("rename")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.rename")) return true;
            if (arena.getPlayerCount() != 0) return sendMsg(sender, ChatColor.RED + "The arena must be empty.");
            if (Dodgeball.instance.getGameManager().getArena(args[1]) != null) return sendMsg(sender, ChatColor.RED + "There is already an arena by that name!");
            String oldName = arena.getName();
            arena.setName(args[1]);
            return sendMsg(sender, ChatColor.GREEN + "Renamed arena " + oldName + " to " + arena.getName());
        }

        if (label.equals("addspawn")) {
            if (!authorizeSender(sender, true, "dodgeball.arena.setspawn")) return true;
            if (arena.getPlayerCount() > 0) return sendMsg(sender, ChatColor.RED + "An arena can only be edited when empty.");
            Team t = Team.fromString(args[1]);
            if (t.equals(Team.NONE)) return sendMsg(sender, ChatColor.RED + "Invalid team!");
            arena.addSpawnPoint(t, ((Player) sender).getLocation());
            return sendMsg(sender, ChatColor.GREEN + "Added a " + t.toString() + " spawn in arena: " + arena.getName());
        }

        if (label.equals("clearspawns")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.clearspawns")) return true;
            if (arena.getPlayerCount() > 0) return sendMsg(sender, ChatColor.RED + "An arena can only be edited when empty.");
            if (args[1].equalsIgnoreCase("all")) {
                arena.clearSpawnPoints();
                return sendMsg(sender, ChatColor.GREEN + "Cleared all team spawnpoints in arena: " + arena.getName());
            }
            Team t = Team.fromString(args[1]);
            if (t.equals(Team.NONE)) return sendMsg(sender, ChatColor.RED + "Invalid team!");
            if (!arena.clearSpawnPoints(t)) return sendMsg(sender, "That team doesn't have any spawn points.");
            return sendMsg(sender, ChatColor.GREEN + "Cleared the " + t.toString() + " spawnpoints in arena: " + arena.getName());
        }

        if (label.equals("friendlyfire")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.togglefriendlyfire")) return true;
            if (arena.getPlayerCount() > 0) return sendMsg(sender, ChatColor.RED + "An arena can only be edited when empty.");
            if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true")) {
                return sendMsg(sender, arena.setFriendlyFireEnabled(true) ? ChatColor.GREEN + "Enabled friendly fire in arena: " + arena.getName() : "Friendly fire is already enabled.");
            }
            if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false")) {
                return sendMsg(sender, arena.setFriendlyFireEnabled(false) ? ChatColor.GREEN + "Disabled friendly fire in arena: " + arena.getName() : "Friendly fire is already disabled.");
            }
            return sendMsg(sender, ChatColor.RED + "Invalid flag. " + ChatColor.GRAY + "Use " + ChatColor.GREEN + "on" + ChatColor.GRAY + " or " + ChatColor.DARK_RED + "off" + ChatColor.GRAY + ".");
        }

        if (label.equals("setballlimit")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.balls.setlimit")) return true;
            if (arena.getStage() == 2) return sendMsg(sender, ChatColor.RED + "The ball limit cannot be set while a round is in progress.");
            int limit = 0;
            try {
                limit = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
            }
            if (limit < 1) return sendMsg(sender, ChatColor.RED + "The ball limit must be a number no less than 1.");
            arena.setBallLimit(limit);
            return sendMsg(sender, ChatColor.GREEN + "Updated the ball limit for arena: " + arena.getName());
        }

        if (label.equals("setlimit")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.setlimit")) return true;
            if (arena.getPlayerCount() > 0) return sendMsg(sender, ChatColor.RED + "An arena can only be edited when empty.");
            int limit = 0;
            try {
                limit = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
            }
            if (limit < 2) return sendMsg(sender, ChatColor.RED + "The player limit must be a number no less than 2.");
            if (limit < arena.getStartCount()) return sendMsg(sender, ChatColor.RED + "The player limit cannot be less than the start count.");
            arena.setPlayerLimit(limit);
            return sendMsg(sender, ChatColor.GREEN + "Updated the player limit for arena: " + arena.getName());
        }

        if (label.equals("setstartcount")) {
            if (!authorizeSender(sender, false, "dodgeball.arena.setstartcount")) return true;
            if (arena.getPlayerCount() > 0) return sendMsg(sender, ChatColor.RED + "An arena can only be edited when empty.");
            int count = 0;
            try {
                count = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
            }
            if (count < 2) return sendMsg(sender, ChatColor.RED + "The start count must be a number no less than 2.");
            if (count > arena.getPlayerLimit()) return sendMsg(sender, ChatColor.RED + "The start count cannot be greater than the player limit.");
            arena.setStartCount(count);
            return sendMsg(sender, ChatColor.GREEN + "Updated the start count for arena: " + arena.getName());
        }

        return sendMsg(sender, "Command not recognized. Type " + ChatColor.AQUA + "/dodge help" + ChatColor.GRAY + " for a list of commands.");
    }

    private boolean sendMsg(CommandSender sender, String msg) {
        sender.sendMessage(Dodgeball.prefix + msg);
        return true;
    }

    private boolean authorizeSender(CommandSender sender, boolean mustBePlayer, String permission) {
        if (mustBePlayer && !(sender instanceof Player)) {
            sender.sendMessage(Dodgeball.prefix + ChatColor.RED + "Only players can run that command.");
            return false;
        }
        if (permission != null && sender instanceof Player && !sender.hasPermission(permission)) {
            sender.sendMessage(Dodgeball.prefix + ChatColor.RED + "Access denied!");
            return false;
        }
        return true;
    }
}
