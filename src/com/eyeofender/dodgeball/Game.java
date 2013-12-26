package com.eyeofender.dodgeball;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.eyeofender.dodgeball.perk.ActivePerks;
import com.eyeofender.dodgeball.perk.FiringMode;
import com.eyeofender.dodgeball.util.GameCountdown;
import com.eyeofender.dodgeball.util.GameTimer;
import com.eyeofender.dodgeball.util.Menu;
import com.eyeofender.enderpearl.Util;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Game {

    private Dodgeball plugin;
    private State state;
    private Arena arena;
    private Map<String, DodgeTeam> players;

    private Scoreboard scoreboard;
    private Objective lives;

    private GameCountdown countdown;
    private GameTimer timer;

    private int ballCount;

    public Game(Dodgeball plugin) {
        this.plugin = plugin;
        this.state = State.DISABLED;
        this.players = Maps.newHashMap();

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.lives = scoreboard.registerNewObjective("lives", "dummy");
        lives.setDisplaySlot(DisplaySlot.BELOW_NAME);
        lives.setDisplayName(ChatColor.RED + "\u2764");

        this.countdown = new GameCountdown(plugin, 60);
        this.timer = new GameTimer(plugin, 60 * 5);

        this.ballCount = 0;

        setArena(Arena.getRandom());
    }

    public void addPlayer(Player player) {
        if (arena == null) setArena(Arena.getRandom());

        if (state == State.DISABLED) {
            player.sendMessage(ChatColor.RED + "No arenas exist.");
            return;
        }

        players.put(player.getName(), null);
        player.setScoreboard(scoreboard);

        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);

        Menu.apply(player);

        player.setLevel(0);
        player.setExp(0);
        player.setMaxHealth(10.0);
        player.setHealth(plugin.getApi().getRankManager().hasRank(player) ? 8.0 : 6.0);
        player.setFoodLevel(20);
        updateLives(player);

        assignTeam(player);
        player.teleport(arena.getLobby());

        startCountdown();
    }

    public void removePlayer(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        players.remove(player.getName());
        Util.sendPM(player, "Connect", "hub");

        if (getState() == State.IN_GAME && getRemainingTeams().size() < 2) stop();
    }

    public boolean contains(Player player) {
        return players.containsKey(player.getName());
    }

    private void startCountdown() {
        if (players.size() < 4 || getRemainingTeams().size() < 2) {
            Bukkit.broadcastMessage(ChatColor.AQUA + "The countdown will begin once " + (arena.getTeams().size() - players.size()) + " more players join!");
            return;
        }

        if (getState() == State.WAITING) {
            state = State.STARTING;
            countdown.start();
        }
    }

    public void start() {
        if (state != State.WAITING && state != State.STARTING) return;

        for (Entry<String, DodgeTeam> entry : players.entrySet()) {
            Player player = Bukkit.getPlayerExact(entry.getKey());
            if (player == null) continue;

            player.teleport(arena.getRandomSpawnPoint(entry.getValue()));
            player.getInventory().clear();
            ActivePerks.get(player).apply(arena);
            entry.getValue().equip(player);
        }

        spawnDodgeball(null, false);
        timer.start();
        state = State.IN_GAME;
    }

    public void stop() {
        if (state != State.IN_GAME) return;

        timer.stop();

        List<DodgeTeam> teams = Lists.newArrayList();
        for (DodgeTeam team : players.values()) {
            if (!teams.contains(team)) teams.add(team);
        }

        if (teams.size() == 1) {
            Bukkit.broadcastMessage(teams.get(0).getChatColour() + teams.get(0).getDisplayName() + ChatColor.GOLD + " team won the game!");
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + "Draw!");
        }

        for (Entry<String, DodgeTeam> entry : players.entrySet()) {
            Player player = Bukkit.getPlayerExact(entry.getKey());
            if (player == null) continue;

            if (teams.size() == 1 && teams.get(0).equals(entry.getValue())) player.sendMessage(ChatColor.GREEN + "Your team won the game!");

            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            Util.sendPM(player, "Connect", "hub");
        }

        players.clear();

        clearDodgeballs();
        state = State.RESTARTING;
        setArena(Arena.getRandom());
    }

    public State getState() {
        return state;
    }

    public Arena getArena() {
        return arena;
    }

    public int getBallCount() {
        return ballCount;
    }

    public void setArena(Arena arena) {
        this.arena = arena;

        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }

        if (arena == null || !arena.isSetup()) {
            plugin.log().info("Arena is null or not setup.");
            this.state = State.DISABLED;
            return;
        }

        this.state = State.WAITING;

        for (DodgeTeam team : arena.getTeams()) {
            Team t = scoreboard.registerNewTeam(team.toString());
            t.setAllowFriendlyFire(false);
            t.setCanSeeFriendlyInvisibles(true);
            t.setDisplayName(team.getDisplayName());
            t.setPrefix(team.getChatColour() + "");
        }

        plugin.log().info("Selected arena '" + arena.getName() + "'.");
    }

    public void assignTeam(Player player) {
        DodgeTeam smallest = arena.getRandomTeam();
        int size = Integer.MAX_VALUE;

        Map<DodgeTeam, Integer> teams = Maps.newHashMap();
        for (DodgeTeam team : players.values()) {
            if (teams.containsKey(team)) {
                teams.put(team, teams.get(team) + 1);
            } else {
                teams.put(team, 1);
            }
        }

        if (teams.size() == arena.getTeams().size()) {

            for (Entry<DodgeTeam, Integer> entry : teams.entrySet()) {
                if (entry.getValue() < size) {
                    size = entry.getValue();
                    smallest = entry.getKey();
                }
            }
        }

        setTeam(player, smallest);
    }

    public void setTeam(Player player, DodgeTeam team) {
        players.put(player.getName(), team);

        Team oldTeam = scoreboard.getPlayerTeam(player);
        if (oldTeam != null) {
            oldTeam.removePlayer(player);
        }

        Team t = scoreboard.getTeam(team.toString());
        if (t == null) {
            t = scoreboard.registerNewTeam(team.toString());
            t.setAllowFriendlyFire(false);
            t.setCanSeeFriendlyInvisibles(true);
            t.setDisplayName(team.getDisplayName());
            t.setPrefix(team.getChatColour() + "");
        }
        t.addPlayer(player);
    }

    public List<DodgeTeam> getRemainingTeams() {
        List<DodgeTeam> teams = Lists.newArrayList();
        for (DodgeTeam team : players.values()) {
            if (!teams.contains(team)) teams.add(team);
        }
        return teams;
    }

    public void dropDodgeball(Location location) {
        location.getWorld().dropItem(location, FiringMode.STANDARD.getDodgeball());
    }

    public void spawnDodgeball(DodgeTeam team, boolean silently) {
        Random rand = new Random();
        if (team == null) { // Choose a team at random
            List<DodgeTeam> validTeams = getRemainingTeams();
            if (validTeams.size() == 0) return;
            team = validTeams.get(rand.nextInt(validTeams.size()));
        }
        Location[] spawns = arena.getSpawnPoints(team);
        Location dropLoc = spawns[rand.nextInt(spawns.length)];
        dropDodgeball(dropLoc);
        if (!silently) Bukkit.broadcastMessage(ChatColor.GRAY + "A new dodgeball has entered gameplay!");
        ballCount++;
    }

    public void clearDodgeballs() {
        for (Item item : arena.getLobby().getWorld().getEntitiesByClass(Item.class)) {
            ItemStack stack = item.getItemStack();
            if (stack.getType() == Material.SNOW_BALL) item.remove();
        }
        this.ballCount = 0;
    }

    public void respawn(Player player) {
        player.teleport(arena.getRandomSpawnPoint(players.get(player.getName())));
        updateLives(player);
    }

    private void updateLives(Player player) {
        lives.getScore(player).setScore((int) (player.getHealth() / 2));
    }

    public enum State {
        DISABLED(ChatColor.DARK_RED + "Disabled"),
        WAITING(ChatColor.GREEN + "Waiting"),
        STARTING(ChatColor.YELLOW + "Starting"),
        IN_GAME(ChatColor.DARK_RED + "In Progress"),
        RESTARTING(ChatColor.DARK_RED + "Restarting");

        private String motd;

        private State(String motd) {
            this.motd = motd;
        }

        public String getMotd() {
            return motd;
        }
    }

}
