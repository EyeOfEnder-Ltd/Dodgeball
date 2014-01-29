package com.eyeofender.dodgeball;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import me.avery246813579.universalcredits.UniversalCredits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
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
    private static Random rand = new Random();
    private State state;
    private Arena arena;
    private Map<String, DodgeTeam> players;
    private List<String> spectators;

    private Scoreboard scoreboard;
    private Objective lives;

    private GameCountdown countdown;
    private GameTimer timer;

    private int ballCount;

    public Game(Dodgeball plugin) {
        this.plugin = plugin;
        this.state = State.DISABLED;
        this.players = Maps.newHashMap();
        this.spectators = Lists.newArrayList();

        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        this.countdown = new GameCountdown(plugin, 120);
        this.timer = new GameTimer(plugin, 60 * 5);

        this.ballCount = 0;

        setArena(Arena.getRandom());
    }

    public void addPlayer(Player player) {
        if (players.containsKey(player.getName())) return;
        if (arena == null) setArena(Arena.getRandom());

        if (state == State.DISABLED) {
            Dodgeball.sendMessage(player, ChatColor.RED + "No arenas exist.");
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
        ActivePerks.get(player).updateHealth(player);
        player.setFoodLevel(20);
        updateLives(player);

        if (state == State.WAITING || state == State.STARTING) {
            assignTeam(player);
            player.teleport(arena.getLobby());
            Dodgeball.broadcastMessage(ChatColor.BLUE + player.getName() + " has joined the game. (" + Bukkit.getOnlinePlayers().length + "/" + Bukkit.getMaxPlayers() + ")");

            startCountdown();
        } else {
            addSpectator(player, true);
            Dodgeball.sendMessage(player, ChatColor.AQUA + "You are a spectator for the remainder of the game.");
        }
    }

    public void removePlayer(Player player, boolean kick) {
    	if(!(state == State.IN_GAME))
    		Dodgeball.broadcastMessage(ChatColor.RED + player.getName() + " has left the game. (" + Bukkit.getOnlinePlayers().length + "/" + Bukkit.getMaxPlayers() + ")");
    	
        if (!players.containsKey(player.getName())) return;
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        setTeam(player, null);
        players.remove(player.getName());
        removeSpectator(player);
        if (kick) Util.sendPM(player, "Connect", "hub");
    }

    public void addSpectator(Player player, boolean teleport) {
        spectators.add(player.getName());

        player.setMaxHealth(2.0);
        player.setHealth(2.0);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.setAllowFlight(true);

        Location spawn = teleport ? arena.getRandomSpawnPoint(arena.getRandomTeam()) : player.getLocation();
        player.teleport(spawn.add(0, 4, 0));
        player.setFlying(true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(player);
            if (isSpectator(p)) player.hidePlayer(p);
        }

        if (getState() == State.IN_GAME && getRemainingTeams().size() < 2) stop();
    }

    public void removeSpectator(Player player) {
        if (!spectators.contains(player.getName())) return;

        player.setAllowFlight(false);

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(player);
        }

        spectators.remove(player.getName());
    }

    public boolean isPlayer(Player player) {
        return contains(player) && !spectators.contains(player.getName());
    }

    public boolean isSpectator(Player player) {
        return contains(player) && spectators.contains(player.getName());
    }

    public int getPlayerCount() {
        return players.size() - spectators.size();
    }

    public DodgeTeam getTeam(Player player) {
        return players.get(player.getName());
    }

    public boolean contains(Player player) {
        return players.containsKey(player.getName());
    }

    private void startCountdown() {
        if (getState() != State.WAITING || countdown.isRunning()) return;

        if (players.size() < 4 || getRemainingTeams().size() < 2) {
            return;
        }

        state = State.STARTING;
        countdown.start();
    }

    public void start() {
		for(Player player : Bukkit.getOnlinePlayers()){
			player.sendMessage(ChatColor.GOLD + "-=- -=-=- -=-=-=- -=-==- -=-=-=- -=-=- -=-");
			player.sendMessage("");
			player.sendMessage(ChatColor.BLUE + "Gamemode: " + ChatColor.GRAY + "Dodgeball");
			player.sendMessage("");
			player.sendMessage(ChatColor.LIGHT_PURPLE + "Map: " + ChatColor.GRAY + arena.getName());
			player.sendMessage(ChatColor.GREEN + "Creator: " + ChatColor.GRAY + "Eye Of Ender Staff");
			player.sendMessage("");
			player.sendMessage(ChatColor.RED + "Plugin by: " + ChatColor.GRAY + "Avery246813579, Limebyte, LinearLogic ");
			player.sendMessage(ChatColor.GOLD + "-=- -=-=- -=-=-=- -=-=- -=-=-=- -=-=- -=-");
		}

    	
        if (state != State.WAITING && state != State.STARTING) return;

        if (countdown.isRunning()) countdown.stop();

        for (Entry<String, DodgeTeam> entry : players.entrySet()) {
            Player player = Bukkit.getPlayerExact(entry.getKey());
            if (player == null) continue;

            player.teleport(arena.getRandomSpawnPoint(entry.getValue()));
            player.getInventory().clear();
            ActivePerks.get(player).apply(arena);
            entry.getValue().addPlayer(player);
        }

        timer.start();
        state = State.IN_GAME;
    }

    public void stop() {
        if (state != State.IN_GAME) return;
        state = State.RESTARTING;

        timer.stop();

        List<DodgeTeam> teams = getRemainingTeams();

        if (teams.size() == 1) {
        	for(Player player : Bukkit.getOnlinePlayers()){
        		Dodgeball.sendMessage(player, teams.get(0).getChatColour() + teams.get(0).getDisplayName() + ChatColor.GOLD + " team won the game!");
        		for(Player players : teams.get(0).getGamePlayers()){
        			UniversalCredits.getApi().pay(players.getName(), 10, false);
        			
        			players.sendMessage("");
        			players.sendMessage("");
        			players.sendMessage(ChatColor.GOLD + "-=- -=-=- -=-=-=- -=-=- -=-");
        			players.sendMessage("");
					Dodgeball.sendMessage(player , "You have received 100 Credits for winning the game!");
        			players.sendMessage("");
					if( player.isOp() || plugin.getApi().getRankManager().hasRank(player)){
						player.sendMessage("");
						Dodgeball.sendMessage(player, "You have received a bonus of " + (10) + " Credits for being a donator." );
	        			UniversalCredits.getApi().pay(players.getName(), 10, false);
					}
					player.sendMessage("");
					player.sendMessage(ChatColor.GOLD + "-=- -=-=- -=-=-=- -=-=- -=-");
        		}
        	}
        } else {
        	for(Player player : Bukkit.getOnlinePlayers()){
        		Dodgeball.sendMessage(player, "The game is a tie!");
        	}        
        }

        final Arena next = Arena.getRandom();
        Bukkit.broadcastMessage(ChatColor.GOLD + "Next arena: " + next.getName() + "!");

        new BukkitRunnable() {
            private int time = 10;

            @Override
            public void run() {
                time--;

                launchFirework(arena.getRandomSpawnPoint(arena.getRandomTeam()));

                if (time <= 0) {
                    for (Entry<String, DodgeTeam> entry : players.entrySet()) {
                        Player player = Bukkit.getPlayerExact(entry.getKey());
                        if (player == null) continue;

                        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                        setTeam(player, null);

                        removeSpectator(player);
                    }

                    for (DodgeTeam team : arena.getTeams()) {
                        team.reset();
                    }

                    players.clear();
                    spectators.clear();

                    clearDodgeballs();

                    setArena(next);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) continue;
                        if (state == State.DISABLED) {
                            Util.sendPM(player, "Connect", "hub");
                        } else {
                            addPlayer(player);
                        }
                    }

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
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
        List<DodgeTeam> smallest = Lists.newArrayList();
        int size = 500;

        for (DodgeTeam team : arena.getTeams()) {
            if (team.getPlayers() > size) continue;
            if (team.getPlayers() < size) smallest.clear();
            size = team.getPlayers();
            smallest.add(team);
        }

        setTeam(player, smallest.get(rand.nextInt(smallest.size())));
    }

    public void switchTeam(Player player, DodgeTeam team) {
        int smallest = 500;
        int largest = 0;

        if (state != State.WAITING && state != State.STARTING) {
            Dodgeball.sendMessage(player, ChatColor.RED + "You cannot switch teams mid-game!");
            return;
        }

        if (!arena.getTeams().contains(team)) {
            Dodgeball.sendMessage(player, ChatColor.RED + "Invalid team!");
            return;
        }

        DodgeTeam old = getTeam(player);
        if (old != null && old.equals(team)) {
            Dodgeball.sendMessage(player, ChatColor.RED + "You are already on that team!");
            return;
        }

        for (DodgeTeam t : arena.getTeams()) {
            int size = t.getPlayers();

            if (t.toString().equals(team.toString())) size++;
            if (t.toString().equals(getTeam(player).toString())) size--;

            if (size < smallest) smallest = size;
            if (size > largest) largest = size;
        }

        if ((largest - smallest) > 2) {
            Dodgeball.sendMessage(player, ChatColor.RED + "You cannot join this team or they will become unbalanced!");
            return;
        }

        setTeam(player, team);
    }

    public void setTeam(Player player, DodgeTeam team) {
        DodgeTeam old = getTeam(player);
        if (old != null) {
            old.removePlayer(player);
        }

        players.put(player.getName(), team);

        Team oldTeam = scoreboard.getPlayerTeam(player);
        if (oldTeam != null) {
            oldTeam.removePlayer(player);
        }

        if (team == null) return;

        team.addPlayer(player);

        Team t = scoreboard.getTeam(team.toString());
        if (t == null) {
            t = scoreboard.registerNewTeam(team.toString());
            t.setAllowFriendlyFire(false);
            t.setCanSeeFriendlyInvisibles(true);
            t.setDisplayName(team.getDisplayName());
            t.setPrefix(team.getChatColour() + "");
        }
        t.addPlayer(player);
        Dodgeball.sendMessage(player, ChatColor.AQUA + "You are on team " + team.getChatColour() + team.getDisplayName());
    }

    public List<DodgeTeam> getRemainingTeams() {
        List<DodgeTeam> teams = Lists.newArrayList();
        for (Entry<String, DodgeTeam> entry : players.entrySet()) {
            if (spectators.contains(entry.getKey())) continue;
            if (!teams.contains(entry.getValue())) teams.add(entry.getValue());
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
        dropLoc.getWorld().playEffect(dropLoc, Effect.ENDER_SIGNAL, 4);
        dropLoc.getWorld().playEffect(dropLoc.add(0, 0.5, 0), Effect.ENDER_SIGNAL, 4);
        dropLoc.getWorld().playEffect(dropLoc.add(0, 0.5, 0), Effect.ENDER_SIGNAL, 4);
        if (!silently){
        	for(Player player : Bukkit.getOnlinePlayers()){
        		Dodgeball.sendMessage(player, "A new dodgeball has entered the arena.");
        	}
        }
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

    private void launchFirework(Location location) {
        Firework fw = (Firework) location.getWorld().spawn(location, Firework.class);
        FireworkMeta fm = fw.getFireworkMeta();
        Type type = null;
        switch (rand.nextInt(5)) {
            default:
            case 0:
                type = Type.BALL;
                break;
            case 1:
                type = Type.BALL_LARGE;
                break;
            case 2:
                type = Type.BURST;
                break;
            case 3:
                type = Type.CREEPER;
                break;
            case 4:
                type = Type.STAR;
                break;
        }
        Color c1 = Color.fromRGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        Color c2 = Color.fromRGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        FireworkEffect effect = FireworkEffect.builder().flicker(rand.nextBoolean()).withColor(c1).withFade(c2).with(type).trail(rand.nextBoolean()).build();
        fm.addEffect(effect);
        int Power = rand.nextInt(2) + 1;
        fm.setPower(Power);
        fw.setFireworkMeta(fm);

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

	public Scoreboard getScoreboard() {
		return scoreboard;
	}

	public void setScoreboard(Scoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

}
