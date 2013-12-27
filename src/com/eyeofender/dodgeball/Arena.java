package com.eyeofender.dodgeball;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.eyeofender.dodgeball.util.Region;
import com.eyeofender.dodgeball.util.SerializableLocation;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Arena implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Random rand = new Random();
    private static Map<String, Arena> arenas = Maps.newHashMap();

    private String name;
    private SerializableLocation lobby;
    private ArrayList<Region> midlines;
    private HashMap<String, ArrayList<SerializableLocation>> spawnPoints;

    private transient HashMap<String, Location> pendingLocations;

    public Arena(String name) {
        this.name = name;
        init();
    }

    public void init() {
        if (midlines == null) midlines = Lists.newArrayList();
        if (spawnPoints == null) spawnPoints = Maps.newHashMap();
        pendingLocations = new HashMap<String, Location>();
    }

    public boolean isSetup() {
        return lobby != null && spawnPoints.keySet().size() > 1;
    }

    public String getName() {
        return name;
    }

    public boolean setName(String newName) {
        if (name.equalsIgnoreCase(newName)) return false;
        File arenaFile = new File(Dodgeball.getInstance().getDataFolder() + File.separator + "arenas", name + ".arena");
        arenaFile.delete();
        name = newName;
        save();
        return true;
    }

    public Location getLobby() {
        if (lobby == null) return null;
        return lobby.asBukkitLocation();
    }

    public void setLobby(Location location) {
        lobby = location == null ? null : new SerializableLocation(location, true);
        save();
    }

    public void addMidline(Region midline) {
        midlines.add(midline);
        save();
    }

    public void clearMidlines() {
        midlines.clear();
    }

    public boolean isLocationInMidline(Location loc) {
        for (Region line : midlines)
            if (line.contains(SerializableLocation.asBlockLocation(loc))) return true;
        return false;
    }

    public List<DodgeTeam> getTeams() {
        List<DodgeTeam> teams = Lists.newArrayList();
        for (String name : spawnPoints.keySet()) {
            teams.add(DodgeTeam.fromString(name));
        }
        return teams;
    }

    public DodgeTeam getRandomTeam() {
        return getTeams().get(rand.nextInt(getTeams().size()));
    }

    public Location[] getSpawnPoints(DodgeTeam team) {
        ArrayList<SerializableLocation> spawns = spawnPoints.get(team.toString());
        if (spawns == null) return null;
        Location[] spawnLocs = new Location[spawns.size()];
        for (int i = 0; i < spawns.size(); i++)
            spawnLocs[i] = spawns.get(i).asBukkitLocation();
        return spawnLocs;
    }

    public Location getRandomSpawnPoint(DodgeTeam team) {
        ArrayList<SerializableLocation> spawns = spawnPoints.get(team.toString());
        if (spawns == null) return null;
        return spawns.get(rand.nextInt(spawns.size())).asBukkitLocation();
    }

    public void addSpawnPoint(DodgeTeam team, Location loc) {
        if (loc == null || team == null) return;
        if (!spawnPoints.containsKey(team.toString())) spawnPoints.put(team.toString(), new ArrayList<SerializableLocation>());
        spawnPoints.get(team.toString()).add(new SerializableLocation(loc, true));
        save();
    }

    public void clearSpawnPoints() {
        spawnPoints.clear();
        save();
    }

    public boolean clearSpawnPoints(DodgeTeam team) {
        if (spawnPoints.remove(team.toString()) != null) {
            save();
            return true;
        }
        return false;
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
            pendingLocations.put(player.getName(), SerializableLocation.asBlockLocation((player.getLocation())));
            return false;
        }
        midlines.add(new Region(loc, SerializableLocation.asBlockLocation(player.getLocation())));
        pendingLocations.remove(player.getName());
        return true;
    }

    public boolean clearPendingLocation(Player player) {
        return pendingLocations.remove(player.getName()) != null;
    }

    @Override
    public String toString() {
        return name;
    }

    public void save() {
        try {
            File arenaDir = new File(Dodgeball.getInstance().getDataFolder() + File.separator + "arenas");
            arenaDir.mkdirs();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(arenaDir, name + ".arena")));
            oos.writeObject(this);
            oos.close();
        } catch (IOException e) {
            Dodgeball.getInstance().log().severe("Failed to save arena \"" + name + "\"");
            e.printStackTrace();
        }
    }

    public static void loadArenas() {
        arenas.clear();
        File arenaDir = new File(Dodgeball.getInstance().getDataFolder() + File.separator + "arenas");
        arenaDir.mkdirs();
        for (File arenaFile : arenaDir.listFiles()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arenaFile));
                Arena arena = (Arena) ois.readObject();
                arena.init();
                arenas.put(arena.getName().toLowerCase(), arena);
                Dodgeball.getInstance().log().info("Loaded arena " + arena.getName());
                ois.close();
            } catch (Exception e) {
                Dodgeball.getInstance().log().log(Level.SEVERE, "Failed to load an arena from the $1 file.", arenaFile.getName());
                e.printStackTrace();
                continue;
            }
        }
    }

    public static void saveArenas() {
        for (Arena arena : arenas.values()) {
            arena.save();
        }
    }

    public static Arena getRandom() {
        if (arenas == null || arenas.isEmpty()) return null;
        return arenas.values().toArray(new Arena[arenas.size()])[rand.nextInt(arenas.size())];
    }

    public static Arena get(String name) {
        return arenas.get(name.toLowerCase());
    }

    public static boolean create(String name) {
        if (arenas.containsKey(name)) return false;
        arenas.put(name.toLowerCase(), new Arena(name));
        return true;
    }

    public static boolean delete(String name) {
        Arena arena = get(name);
        if (arena == null) return false;
        arenas.remove(name.toLowerCase());
        File arenaFile = new File(Dodgeball.getInstance().getDataFolder() + File.separator + "arenas", arena.getName() + ".arena");
        arenaFile.delete();
        return true;
    }

    public static Map<String, Arena> getArenas() {
        return arenas;
    }

}
