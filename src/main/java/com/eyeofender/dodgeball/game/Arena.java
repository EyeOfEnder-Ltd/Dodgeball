package com.eyeofender.dodgeball.game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.connect.DatabaseConnection;
import com.eyeofender.dodgeball.connect.table.Stats;
import com.eyeofender.dodgeball.game.perks.ActivePerks;
import com.eyeofender.dodgeball.game.perks.FiringMode;
import com.eyeofender.dodgeball.location.Region;
import com.eyeofender.dodgeball.location.SerializableLocation;
import com.eyeofender.massapi.database.MassDatabase;
import com.eyeofender.massapi.database.table.Membership;

public class Arena implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    private boolean friendlyFire;
    private int ballLimit;
    private int playerLimit;
    private int startCount;

    private SerializableLocation lobby;
    private ArrayList<Region> midlines;
    private HashMap<Team, ArrayList<SerializableLocation>> spawnPoints;
    private ArrayList<SerializableLocation> signLocations;

    private transient int ballCount;
    private transient int stage;
    private transient int timer;

    private transient Scoreboard board;
    private transient Objective objective;

    private transient HashMap<String, Location> pendingLocations;
    private transient HashSet<Player> spectators;
    private transient HashMap<String, Team> players;

    public Arena(String name) {
        this.name = name;
        initialize();
    }

    public void initialize() {
        if (ballLimit < 1) ballLimit = 5;
        if (playerLimit < 2) playerLimit = 10;
        if (startCount < 2) startCount = 4;
        if (midlines == null) midlines = new ArrayList<Region>();
        if (spawnPoints == null) spawnPoints = new HashMap<Team, ArrayList<SerializableLocation>>();
        if (signLocations == null) signLocations = new ArrayList<SerializableLocation>();
        board = Dodgeball.instance.getServer().getScoreboardManager().getNewScoreboard();
        objective = board.registerNewObjective("Player counts", "dummy");
        objective.setDisplayName(ChatColor.GOLD + "Player counts");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        pendingLocations = new HashMap<String, Location>();
        spectators = new HashSet<Player>();
        players = new HashMap<String, Team>();
        ballCount = 0;
        setStage(0);
    }

    public void broadcast(String message) {
        for (String name : players.keySet()) {
            Player player = Dodgeball.instance.getServer().getPlayerExact(name);
            if (player == null) return;
            player.sendMessage(Dodgeball.prefix + "<" + ChatColor.YELLOW + this.name + ChatColor.GRAY + "> " + message);
        }
        for (Player spectator : spectators)
            if (spectator != null) spectator.sendMessage(Dodgeball.prefix + "<" + ChatColor.YELLOW + this.name + ChatColor.GRAY + "> " + message);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() { // to facilitate displaying of the arena in
                               // listings
        return name;
    }

    public boolean setName(String newName) {
        if (name.equalsIgnoreCase(newName)) return false;
        File arenaFile = new File(Dodgeball.instance.getDataFolder() + File.separator + "arenas", name + ".arena");
        arenaFile.delete();
        name = newName;
        save();
        updateSigns();
        return true;
    }

    public boolean isFriendlyFireEnabled() {
        return friendlyFire;
    }

    public boolean setFriendlyFireEnabled(boolean enabled) {
        if (enabled == friendlyFire) return false;
        friendlyFire = enabled;
        if (Dodgeball.saveOnEdit) save();
        return true;
    }

    public int getBallLimit() {
        return ballLimit;
    }

    public void setBallLimit(int limit) {
        if (ballLimit == limit) return;
        ballLimit = limit;
        if (Dodgeball.saveOnEdit) save();
    }

    public int getBallCount() {
        return ballCount;
    }

    public int getPlayerLimit() {
        return playerLimit;
    }

    public void setPlayerLimit(int limit) {
        if (playerLimit == limit) return;
        playerLimit = limit;
        updateSigns();
        if (Dodgeball.saveOnEdit) save();
    }

    public int getStartCount() {
        return startCount;
    }

    public void setStartCount(int count) {
        if (startCount == count) return;
        startCount = count;
        if (Dodgeball.saveOnEdit) save();
    }

    public Location getLobby() {
        return lobby.asBukkitLocation();
    }

    public void setLobby(Location loc) {
        lobby = loc == null ? null : new SerializableLocation(asBlockLocation(loc));
        if (Dodgeball.saveOnEdit) save();
    }

    public void addMidline(Region midline) {
        midlines.add(midline);
        if (Dodgeball.saveOnEdit) save();
    }

    public void clearMidlines() {
        midlines.clear();
    }

    public boolean isLocationInMidline(Location loc) {
        for (Region line : midlines)
            if (line.contains(asBlockLocation(loc))) return true;
        return false;
    }

    public Location[] getSpawnPoints(Team team) {
        ArrayList<SerializableLocation> spawns = spawnPoints.get(team);
        if (spawns == null) return null;
        Location[] spawnLocs = new Location[spawns.size()];
        for (int i = 0; i < spawns.size(); i++)
            spawnLocs[i] = spawns.get(i).asBukkitLocation();
        return spawnLocs;
    }

    public Location getRandomSpawnPoint(Team team) {
        ArrayList<SerializableLocation> spawns = spawnPoints.get(team);
        if (spawns == null) return null;
        Random rand = new Random();
        return spawns.get(rand.nextInt(spawns.size())).asBukkitLocation();
    }

    public void addSpawnPoint(Team team, Location loc) {
        if (loc == null || team == null || team.equals(Team.NONE)) return;
        if (!spawnPoints.containsKey(team)) spawnPoints.put(team, new ArrayList<SerializableLocation>());
        spawnPoints.get(team).add(new SerializableLocation(asBlockLocation(loc)));
        if (Dodgeball.saveOnEdit) save();
    }

    public void clearSpawnPoints() {
        spawnPoints.clear();
        if (Dodgeball.saveOnEdit) save();
    }

    public boolean clearSpawnPoints(Team team) {
        if (spawnPoints.remove(team) != null) {
            if (Dodgeball.saveOnEdit) save();
            return true;
        }
        return false;
    }

    public int getSpectatorCount() {
        return spectators.size();
    }

    public boolean containsSpectator(Player player) {
        if (player == null) return false;
        return spectators.contains(player);
    }

    public void addSpectator(Player player) {
        if (player == null) return;
        spectators.add(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, timer * 20, 1, false));
        player.setAllowFlight(true);
    }

    public boolean removeSpectator(Player player, boolean teleport) {
        if (player == null || !spectators.remove(player)) return false;
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.setAllowFlight(false);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        if (teleport) player.teleport(Dodgeball.instance.getGameManager().getGlobalLobby());
        return true;
    }

    public void clearSpectators() {
        for (Player spectator : spectators) {
            spectator.removePotionEffect(PotionEffectType.INVISIBILITY);
            spectator.setAllowFlight(false);
            spectator.setMaxHealth(20.0);
            spectator.setHealth(20.0);
            spectator.setFoodLevel(20);
            spectator.teleport(Dodgeball.instance.getGameManager().getGlobalLobby());
        }
        spectators.clear();
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getTeamPlayerCount(Team team) {
        int count = 0;
        for (Entry<String, Team> entry : players.entrySet())
            if (entry.getValue().equals(team)) count++;
        return count;
    }

    public Set<String> getPlayers() {
        return players.keySet();
    }

    public Set<String> getPlayers(Team team) {
        Set<String> names = new HashSet<String>();
        for (Entry<String, Team> entry : players.entrySet())
            if (entry.getValue().equals(team)) names.add(entry.getKey());
        return names;
    }

    public boolean addPlayer(Player player) {
        if (player == null) return false;
        if (!player.hasPermission("dodgeball.join")) {
            player.sendMessage(Dodgeball.prefix + ChatColor.RED + "You don't have permission to play!");
            return false;
        }
        if (Dodgeball.instance.getGameManager().getArena(player) != null || Dodgeball.instance.getGameManager().getArenaFromSpectator(player) != null) {
            player.sendMessage(Dodgeball.prefix + ChatColor.RED + "You are already in an arena!");
            return false;
        }
        if (getTeamCount() < 2) {
            player.sendMessage(Dodgeball.prefix + ChatColor.RED + "That arena has not been set up: " + "it contains less than 2 teams. Please choose another arena.");
            // TODO Nag?
            return false;
        }
        if (stage == 2) {
            player.sendMessage(Dodgeball.prefix + ChatColor.RED + "A game is already in progress in that arena, choose another.");
            return false;
        }
        if (players.size() >= playerLimit) {
            Membership membership = MassDatabase.getMembership(player);
            int priority = membership != null ? membership.getPriority() : 0;

            if (priority > 0) {
                boolean joined = false;
                for (String name : players.keySet()) {
                    Player toKick = Dodgeball.instance.getServer().getPlayerExact(name);
                    Membership kickMembership = MassDatabase.getMembership(toKick);
                    int kickPriority = kickMembership != null ? kickMembership.getPriority() : 0;
                    if (toKick == null || kickPriority <= priority) continue;
                    removePlayer(toKick, false, true);
                    toKick.sendMessage(Dodgeball.prefix + "You were kicked from " + ChatColor.YELLOW + name + ChatColor.GRAY + " to make room for a " + ChatColor.GOLD + "premium player"
                            + ChatColor.GRAY + ". Please find another arena.");
                    joined = true;
                    break;
                }
                if (!joined) {
                    player.sendMessage(Dodgeball.prefix + ChatColor.RED + "That arena is full, and there are no non-premium members you can displace.");
                    return false;
                }
            } else {
                player.sendMessage(Dodgeball.prefix + ChatColor.RED + "That arena is full, choose another.");
                return false;
            }
        }
        addPlayer(player, Team.NONE, true, false);
        return true;
    }

    public void addPlayer(Player player, Team team, boolean updateSigns, boolean updateScoreboard) {
        if (team == null) return;
        Membership membership = MassDatabase.getMembership(player);
        double maxHealth = Dodgeball.instance.getConfig().getInt("lives-standard.max") * 2.0;
        double health = Dodgeball.instance.getConfig().getInt("lives-standard.starting") * 2.0;

        player.setMaxHealth(membership != null ? maxHealth + 2 : maxHealth);
        player.setHealth(membership != null ? health + 2 : health);
        player.setFoodLevel(20);

        players.put(player.getName(), team);
        if (!team.equals(Team.NONE)) player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, team.woolData));
        if (updateScoreboard) updateScoreboard();
        if (updateSigns) updateSigns();
        Dodgeball.instance.getGameListener().setRegistered(true);
    }

    public boolean removePlayer(final Player player, final boolean spectate, boolean teleport) {
        if (!players.containsKey(player.getName())) return false;
        Team team = players.remove(player.getName());
        player.setScoreboard(Dodgeball.instance.getServer().getScoreboardManager().getNewScoreboard());
        updateSigns();
        if (players.size() == 0 && Dodgeball.instance.getGameManager().areAllArenasEmpty()) Dodgeball.instance.getGameListener().setRegistered(false);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setWalkSpeed(0.2f);
        player.getInventory().setHelmet(null);
        Dodgeball.instance.getGameManager().loadGeneralInventory(player);
        DatabaseConnection database = Dodgeball.instance.getDatabaseConnection();
        database.saveStats(database.getStats(player, false));
        if (spectate) {
            addSpectator(player);
            player.sendMessage(Dodgeball.prefix + "You are now spectating in arena: " + name + ". Type " + ChatColor.AQUA + "/leave" + ChatColor.GRAY + " to return to the global lobby.");
        }
        if (teleport) {
            Dodgeball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Dodgeball.instance, new Runnable() {
                public void run() {
                    player.teleport(spectate ? getLobby() : Dodgeball.instance.getGameManager().getGlobalLobby());
                }
            }, 1L);
        }
        if (team == null || team.equals(Team.NONE)) return true;
        if (stage == 2) {
            if (getTeamPlayerCount(team) == 0) { // Team has been eliminated
                broadcast("The " + team.toString().toLowerCase() + " team has been eliminated!");
            }
            if (getActiveTeams().size() == 1) {
                Team winningTeam = getActiveTeams().iterator().next();
                Dodgeball.instance.getServer().broadcastMessage(Dodgeball.prefix + ChatColor.GOLD + "The " + winningTeam.toString() + " team emerges victorious from the arena: " + name + "!");

                Dodgeball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Dodgeball.instance, new Runnable() {
                    public void run() {
                        reset();
                    }
                }, 3L);
            }
            updateScoreboard();
        }
        return true;
    }

    public void clearPlayers() {
        hideScoreboard();
        DatabaseConnection database = Dodgeball.instance.getDatabaseConnection();
        for (String name : players.keySet()) {
            Player player = Dodgeball.instance.getServer().getPlayerExact(name);
            if (player == null) continue;
            player.setMaxHealth(20.0);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.getInventory().setHelmet(null);
            Dodgeball.instance.getGameManager().loadGeneralInventory(player);
            player.teleport(Dodgeball.instance.getGameManager().getGlobalLobby());
            database.saveStats(database.getStats(player, false));
        }
        players.clear();
        updateSigns();
        setStage(0);
        if (Dodgeball.instance.getGameManager().areAllArenasEmpty()) Dodgeball.instance.getGameListener().setRegistered(false);
    }

    public boolean containsTeam(Team team) {
        return spawnPoints.containsKey(team);
    }

    public int getTeamCount() {
        return spawnPoints.size();
    }

    public Team[] getTeams() {
        return spawnPoints.keySet().toArray(new Team[spawnPoints.size()]);
    }

    public Team getTeam(Player player) {
        return players.get(player.getName());
    }

    public Team getTeamWithMostPlayers() {
        HashMap<Team, Integer> teamCounts = new HashMap<Team, Integer>();
        for (Team team : getTeams())
            teamCounts.put(team, 0);
        for (Team team : players.values())
            if (!team.equals(Team.NONE)) teamCounts.put(team, teamCounts.get(team) + 1);
        Team most = Team.NONE;
        int count = 0;
        for (Entry<Team, Integer> entry : teamCounts.entrySet()) {
            if (entry.getValue() >= count) {
                count = entry.getValue();
                most = entry.getKey();
            }
        }
        return most;
    }

    public Team getTeamWithLeastPlayers() {
        HashMap<Team, Integer> teamCounts = new HashMap<Team, Integer>();
        for (Team team : getTeams())
            teamCounts.put(team, 0);
        for (Team team : players.values())
            if (!team.equals(Team.NONE)) teamCounts.put(team, teamCounts.get(team) + 1);
        Team least = Team.NONE;
        int count = 20000;
        for (Entry<Team, Integer> entry : teamCounts.entrySet()) {
            if (entry.getValue() <= count) {
                count = entry.getValue();
                least = entry.getKey();
            }
        }
        return least;
    }

    public HashSet<Team> getActiveTeams() {
        HashSet<Team> teams = new HashSet<Team>();
        for (Team team : players.values())
            teams.add(team);
        return teams;
    }

    public boolean containsPendingLocation(Player player) {
        return pendingLocations.containsKey(player.getName());
    }

    public Location getPendingLocation(Player player) {
        if (player == null) return null;
        return pendingLocations.get(player.getName());
    }

    public boolean addPendingLocation(Player player) {
        Location loc = pendingLocations.get(player.getName());
        if (loc == null) {
            pendingLocations.put(player.getName(), asBlockLocation(player.getLocation()));
            return false;
        }
        midlines.add(new Region(loc, asBlockLocation(player.getLocation())));
        pendingLocations.remove(player.getName());
        return true;
    }

    public boolean clearPendingLocation(Player player) {
        return pendingLocations.remove(player.getName()) != null;
    }

    public void clearPendingLocations() {
        pendingLocations.clear();
    }

    public Location[] getSignLocations() {
        Location[] locs = new Location[signLocations.size()];
        for (int i = 0; i < locs.length; i++)
            locs[i] = signLocations.get(i).asBukkitLocation();
        return locs;
    }

    public boolean addSignLocation(Location loc) {
        if (!signLocations.add(new SerializableLocation(loc))) return false;
        if (Dodgeball.saveOnEdit) save();
        return true;
    }

    public boolean removeSignLocation(Location loc) {
        if (!signLocations.remove(new SerializableLocation(loc))) return false;
        if (Dodgeball.saveOnEdit) save();
        return true;
    }

    public void clearSignLocations() {
        wipeSigns();
        signLocations.clear();
        if (Dodgeball.saveOnEdit) save();
    }

    public boolean containsSignLocation(Location loc) {
        return signLocations.contains(new SerializableLocation(loc));
    }

    public void updateSigns() {
        List<SerializableLocation> brokenLocs = new ArrayList<SerializableLocation>();
        for (SerializableLocation signLoc : signLocations) {
            Block block = signLoc.asBukkitLocation().getBlock();
            if (!block.getType().equals(Material.SIGN_POST) && !block.getType().equals(Material.WALL_SIGN)) { // Broken
                                                                                                              // sign
                                                                                                              // location
                brokenLocs.add(signLoc);
                continue;
            }
            Sign sign = (Sign) block.getState();
            sign.setLine(0, name);
            sign.setLine(1, getStageName());
            sign.setLine(2, players.size() + "/" + playerLimit);
            sign.setLine(3, stage == 0 ? "To start: " + Integer.toString(startCount - players.size()) : null);
            sign.update();
        }
        if (!brokenLocs.isEmpty()) signLocations.removeAll(brokenLocs);
    }

    public void wipeSigns() {
        for (SerializableLocation sloc : signLocations) {
            Block block = sloc.asBukkitLocation().getBlock();
            if (!block.getType().equals(Material.SIGN_POST) && !block.getType().equals(Material.WALL_SIGN)) continue;
            Sign sign = (Sign) block.getState();
            sign.setLine(0, null);
            sign.setLine(1, null);
            sign.setLine(2, null);
            sign.setLine(3, null);
        }
    }

    public void updateScoreboard() {
        for (Team team : getTeams()) {
            int playerCount = getTeamPlayerCount(team);
            if (playerCount < 1) {
                board.resetScores(Dodgeball.instance.getServer().getOfflinePlayer(team.toString()));
                continue;
            }
            Score score = objective.getScore(Dodgeball.instance.getServer().getOfflinePlayer(team.toString()));
            score.setScore(playerCount);
        }
        for (String name : getPlayers()) {
            Player player = Dodgeball.instance.getServer().getPlayerExact(name);
            if (player == null) continue;
            player.setScoreboard(board); // Reallocates the player's scoreboard
                                         // frequently to mitigate problems with
                                         // other plugins changing scoreboards
        }
    }

    public void hideScoreboard() {
        for (String name : getPlayers()) {
            Player player = Dodgeball.instance.getServer().getPlayerExact(name);
            if (player == null) continue;
            player.setScoreboard(Dodgeball.instance.getServer().getScoreboardManager().getNewScoreboard());
        }
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int newStage) {
        if (stage == newStage) return;
        stage = newStage;
        switch (stage) {
            default:
            case 0:
                timer = -1;
                updateSigns();
                clearDodgeballs();
                break;
            case 1:
                timer = Dodgeball.instance.getConfig().getInt("timers.starting");
                updateSigns();
                break;
            case 2:
                timer = Dodgeball.instance.getConfig().getInt("timers.game");
                updateSigns();
                break;
        }
        Dodgeball.instance.getGameManager().updateArenaMenus();
    }

    public String getStageName() {
        switch (stage) {
            case 0:
                return ChatColor.GREEN + "Waiting";
            case 1:
                return ChatColor.YELLOW + "Starting";
            case 2:
                return ChatColor.RED + "In progress";
            default:
                return ChatColor.DARK_RED + "N/A";
        }
    }

    public int getTimerValue() {
        return timer;
    }

    public void setTimerValue(int value) {
        timer = value;
        if (stage == 2) for (Player spectator : spectators)
            spectator.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, timer * 20, 1, false));
    }

    public void decrementTimer() {
        if (timer < 0) return;
        if (stage == 1) {
            switch (--timer) {
                case 0:
                    Team[] teams = getTeams();
                    int teamIndex = -1;
                    if (players.size() < 2) {
                        broadcast(ChatColor.RED + "There aren't enough players to start the round. Resetting the arena...");
                        clearPlayers();
                        return;
                    }
                    boolean preselected = false;
                    for (Team team : players.values()) {
                        if (!team.equals(Team.NONE)) {
                            preselected = true;
                            break;
                        }
                    }
                    Random rand = new Random();
                    if (!preselected) {
                        Iterator<String> iter = players.keySet().iterator();
                        while (iter.hasNext()) {
                            String name = iter.next();
                            Team team = teams[++teamIndex % teams.length];
                            players.put(name, team);
                            Player player = Dodgeball.instance.getServer().getPlayerExact(name);
                            armPlayer(player);
                            player.teleport(spawnPoints.get(team).get(rand.nextInt(spawnPoints.get(team).size())).asBukkitLocation());
                        }
                    } else {
                        Iterator<Entry<String, Team>> iter = players.entrySet().iterator();
                        while (iter.hasNext()) {
                            Entry<String, Team> entry = iter.next();
                            String name = entry.getKey();
                            Team team = entry.getValue();
                            if (team.equals(Team.NONE)) {
                                team = getTeamWithLeastPlayers();
                                players.put(name, team);
                            }
                            Player player = Dodgeball.instance.getServer().getPlayerExact(name);
                            armPlayer(player);
                            player.teleport(spawnPoints.get(team).get(rand.nextInt(spawnPoints.get(team).size())).asBukkitLocation());
                        }
                    }
                    updateScoreboard();
                    broadcast("" + ChatColor.RED + ChatColor.BOLD + "Game on!");
                    setStage(2);
                    spawnDodgeball(Team.NONE, true); // Spawn a ball at a random
                                                     // team spawnpoint
                    break;
                case 10:
                    broadcast("The round will start in 10 seconds!");
                    break;
                case 30:
                    broadcast("The round will start in 30 seconds!");
                    break;
                default:
                    if (timer % 60 == 0) // Every minute
                        broadcast("The round will start in " + (timer == 60 ? "1 minute!" : timer / 60 + " minutes!"));
                    break;
            }
            return;
        }
        if (stage == 2) {
            switch (--timer) {
                case 0:
                    Team leadingTeam = Team.NONE;
                    int leadingCount = 0;
                    for (Team team : spawnPoints.keySet()) {
                        int count = getTeamPlayerCount(team);
                        if (count > leadingCount) {
                            leadingCount = count;
                            leadingTeam = team;
                        }
                    }

                    DatabaseConnection database = Dodgeball.instance.getDatabaseConnection();
                    for (String name : players.keySet()) {
                        Player player = Bukkit.getServer().getPlayerExact(name);
                        if (player == null) continue;

                        Stats stats = database.getStats(player, false);
                        database.saveStats(stats);
                    }

                    Dodgeball.instance.getServer().broadcastMessage(Dodgeball.prefix + ChatColor.GOLD + "The " + leadingTeam.toString() + " team emerges victorious from the arena: " + name + "!");
                    reset();
                    break;
                case 10:
                    broadcast("The round will end in 10 seconds!");
                    if (ballCount < ballLimit) spawnDodgeball(Team.NONE, false);
                    break;
                case 30:
                    broadcast("The round will end in 30 seconds!");
                    if (ballCount < ballLimit) spawnDodgeball(Team.NONE, false);
                    break;
                default:
                    if (timer % 60 == 0) // Every minute
                        broadcast("The round will end in " + (timer == 60 ? "1 minute!" : timer / 60 + " minutes!"));
                    if (timer % 10 == 0) // Every ten seconds
                        if (ballCount < ballLimit) spawnDodgeball(Team.NONE, false);
                    break;
            }
        }
    }

    public void spawnDodgeball(Team team, boolean silently) {
        Random rand = new Random();
        if (team == null || team.equals(Team.NONE)) { // Choose a team at random
            ArrayList<Team> validTeams = new ArrayList<Team>();
            for (Team t : players.values())
                if (!validTeams.contains(t)) validTeams.add(t);
            if (validTeams.size() == 0) return;
            team = validTeams.get(rand.nextInt(validTeams.size()));
        }
        Location dropLoc = spawnPoints.get(team).get(rand.nextInt(spawnPoints.get(team).size())).asBukkitLocation();
        dropLoc.getWorld().dropItem(dropLoc, FiringMode.STANDARD.getDodgeball(this));
        if (!silently) broadcast("A new dodgeball has entered gameplay!");
        ballCount++;
    }

    public boolean clearDodgeballs() {
        if (spawnPoints.isEmpty()) // Spawnpoints are used to determine the
                                   // world containing dodgeballs
            return false;
        Iterator<Item> iter = spawnPoints.values().iterator().next().get(0).asBukkitLocation().getWorld().getEntitiesByClass(Item.class).iterator();
        while (iter.hasNext()) {
            Item item = iter.next();
            ItemStack stack = item.getItemStack();
            if (stack.hasItemMeta() && stack.getItemMeta().hasLore() && stack.getItemMeta().getLore().get(0).contains(name)) item.remove();
        }
        ballCount = 0;
        return true;
    }

    public void reset() {
        clearSpectators();
        clearPlayers();
        clearDodgeballs();
        setStage(0);
    }

    public void armPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.getInventory().setHelmet(new ItemStack(Material.WOOL, 1, players.get(player.getName()).woolData));
        ActivePerks.get(player).apply(this);
    }

    public void save() {
        try {
            File arenaDir = new File(Dodgeball.instance.getDataFolder() + File.separator + "arenas");
            arenaDir.mkdirs();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(arenaDir, name + ".arena")));
            oos.writeObject(this);
            oos.close();
        } catch (IOException e) {
            Dodgeball.instance.logSevere("Failed to save arena \"" + name + "\"");
            e.printStackTrace();
        }
    }

    public Location asBlockLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch());
    }
}
