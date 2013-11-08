package com.eyeofender.dodgeball.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.eyeofender.dodgeball.Dodgeball;

public class GameManager {

    private final ItemStack back;
    private final ItemStack arenaMenuLoader;
    private final ItemStack perkMenuLoader;

    private ArrayList<Arena> arenas = new ArrayList<Arena>();
    private Location globalLobby;

    private boolean updatingArenaMenus; // Whether the arena menus have already
                                        // been updated in this tick
    private HashMap<String, Inventory> arenaMenus = new HashMap<String, Inventory>();

    public GameManager() {
        back = new ItemStack(Material.WOOL, 1, (short) 15);
        ItemMeta meta = back.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Back to Main Menu");
        back.setItemMeta(meta);
        arenaMenuLoader = new ItemStack(Material.BOOK);
        meta = arenaMenuLoader.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Open Arena Menu");
        arenaMenuLoader.setItemMeta(meta);
        perkMenuLoader = new ItemStack(Material.NETHER_STAR);
        meta = perkMenuLoader.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Open Perk Menu");
        perkMenuLoader.setItemMeta(meta);
        updatingArenaMenus = true; // Prevents initial reloadConfig() from
                                   // throwing an NPE
    }

    public void loadArenas() {
        File arenaDir = new File(Dodgeball.instance.getDataFolder() + File.separator + "arenas");
        arenaDir.mkdirs();
        for (File arenaFile : arenaDir.listFiles()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arenaFile));
                Arena arena = (Arena) ois.readObject();
                arena.initialize();
                arenas.add(arena);
                ois.close();
            } catch (Exception e) {
                Dodgeball.instance.getMessenger().log(Level.SEVERE, "Failed to load an arena from the $1 file.", arenaFile.getName());
                e.printStackTrace();
                continue;
            }
        }
    }

    public void saveArenas() {
        for (Arena arena : arenas)
            arena.save();
    }

    public void shutdown() {
        clearArenaMenus();
        for (Arena arena : arenas) {
            arena.reset();
            arena.save();
        }
        arenas.clear();
    }

    public ArrayList<Arena> getArenas() {
        return arenas;
    }

    public Arena getArena(String name) {
        if (name == null) return null;
        for (Arena arena : arenas)
            if (arena.getName().equalsIgnoreCase(name)) return arena;
        return null;
    }

    public Arena getArena(Player player) {
        if (player == null) return null;
        for (Arena arena : arenas)
            if (arena.getTeam(player) != null) return arena;
        return null;
    }

    public Arena getArenaFromSpectator(Player player) {
        if (player == null) return null;
        for (Arena arena : arenas)
            if (arena.containsSpectator(player)) return arena;
        return null;
    }

    public Arena getArenaFromSignLocation(Location loc) {
        if (loc == null) return null;
        for (Arena arena : arenas)
            if (arena.containsSignLocation(loc)) return arena;
        return null;
    }

    public boolean addArena(Arena arena) {
        if (getArena(arena.getName()) != null) return false;
        arenas.add(arena);
        arena.save();
        return true;
    }

    public boolean removeArena(Arena arena) {
        return arenas.remove(arena);
    }

    public boolean deleteArena(Arena arena) {
        if (!removeArena(arena)) return false;
        arena.wipeSigns();
        File arenaFile = new File(Dodgeball.instance.getDataFolder() + File.separator + "arenas", arena.getName() + ".arena");
        arenaFile.delete();
        return true;
    }

    public void clearArenas() {
        arenas.clear();
    }

    public Arena[] getArenasInStage(int stage) {
        int counter = 0;
        Arena[] matches = new Arena[arenas.size()];
        for (Arena arena : arenas)
            if (arena.getStage() == stage) matches[counter++] = arena;
        return Arrays.copyOf(matches, counter);
    }

    public boolean areAllArenasEmpty() {
        for (Arena arena : arenas)
            if (arena.getPlayerCount() > 0) return false;
        return true;
    }

    public void updateArenaSigns() {
        for (Arena arena : arenas)
            arena.updateSigns();
    }

    public void decrementTimers() {
        for (Arena arena : arenas)
            arena.decrementTimer();
    }

    public void loadArenaMenus() {
        clearArenaMenus();
        Inventory mainMenu = Dodgeball.instance.getServer().createInventory(null, 9, "Arena Map Menu");
        ConfigurationSection currentSection = Dodgeball.instance.getConfig().getConfigurationSection("arena-menus");
        for (String map : currentSection.getKeys(false)) {
            Inventory mapMenu = Dodgeball.instance.getServer().createInventory(null, 9, map + " Arenas");
            int arenaCount = 0;
            for (String arenaName : currentSection.getStringList(map + ".arenas")) {
                Arena arena = getArena(arenaName);
                if (arena == null) continue;
                ItemStack icon = new ItemStack(Material.WOOL, 1, (short) (arena.getStage() == 0 ? 5 : arena.getStage() == 1 ? 4 : 14));
                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + arenaName);
                List<String> lore = new ArrayList<String>();
                lore.add(arena.getStageName());
                lore.add("Players: " + arena.getPlayerCount() + "/" + arena.getPlayerLimit());
                if (arena.getStage() == 0) lore.add(arena.getStartCount() - arena.getPlayerCount() + " more needed to start");
                meta.setLore(lore);
                icon.setItemMeta(meta);
                mapMenu.addItem(icon);
                arenaCount++;
            }
            mapMenu.setItem(8, back.clone());
            arenaMenus.put(map, mapMenu);
            ItemStack icon = new ItemStack(Material.getMaterial(currentSection.getString(map + ".item").toUpperCase().replace('-', '_')));
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + map);
            List<String> lore = new ArrayList<String>();
            lore.add(arenaCount + " arenas");
            meta.setLore(lore);
            icon.setItemMeta(meta);
            mainMenu.addItem(icon);
        }
        arenaMenus.put("main", mainMenu);
        Dodgeball.instance.getServer().getScheduler().scheduleSyncRepeatingTask(Dodgeball.instance, new Runnable() {
            public void run() {
                updateArenaMenus();
            }
        }, 300L, Dodgeball.instance.getConfig().getInt("timers.arena-menu-update-interval") * 20L);
        updatingArenaMenus = false;
    }

    public void updateArenaMenus() {
        if (updatingArenaMenus) return;
        updatingArenaMenus = true;
        Inventory mainMenu = arenaMenus.get("main");
        if (arenaMenus.isEmpty()) // Prevents NPE thrown by line 225
            return;
        mainMenu.clear();
        ConfigurationSection currentSection = Dodgeball.instance.getConfig().getConfigurationSection("arena-menus");
        for (String map : currentSection.getKeys(false)) {
            Inventory mapMenu = arenaMenus.get(map);
            mapMenu.clear();
            int arenaCount = 0;
            for (String arenaName : currentSection.getStringList(map + ".arenas")) {
                Arena arena = getArena(arenaName);
                if (arena == null) continue;
                ItemStack icon = new ItemStack(Material.WOOL, 1, (short) (arena.getStage() == 0 ? 5 : arena.getStage() == 1 ? 4 : 14));
                ItemMeta meta = icon.getItemMeta();
                meta.setDisplayName(ChatColor.AQUA + arenaName);
                List<String> lore = new ArrayList<String>();
                lore.add(arena.getStageName());
                lore.add("Players: " + arena.getPlayerCount() + "/" + arena.getPlayerLimit());
                if (arena.getStage() == 0) lore.add(arena.getStartCount() - arena.getPlayerCount() + " more needed to start");
                meta.setLore(lore);
                icon.setItemMeta(meta);
                mapMenu.addItem(icon);
                arenaCount++;
            }
            mapMenu.setItem(8, back.clone());
            ItemStack icon = new ItemStack(Material.getMaterial(currentSection.getString(map + ".item").toUpperCase().replace('-', '_')));
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + map);
            List<String> lore = new ArrayList<String>();
            lore.add(arenaCount + " arenas");
            meta.setLore(lore);
            icon.setItemMeta(meta);
            mainMenu.addItem(icon);
        }
        Dodgeball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Dodgeball.instance, new Runnable() {
            public void run() {
                updatingArenaMenus = false;
            }
        }, 1L);
    }

    public void clearArenaMenus() {
        for (Inventory menu : arenaMenus.values()) {
            List<HumanEntity> viewers = new ArrayList<HumanEntity>();
            viewers.addAll(menu.getViewers());
            for (HumanEntity viewer : viewers)
                viewer.closeInventory();
            menu.clear();
        }
        arenaMenus.clear();
    }

    public boolean containsArenaMenu(String name) {
        return arenaMenus.containsKey(name);
    }

    public Inventory getArenaMenu(String name) {
        return arenaMenus.get(name);
    }

    public void loadGeneralInventory(Player player) {
        player.closeInventory();
        player.getInventory().clear();
        player.getInventory().addItem(arenaMenuLoader.clone());
        player.getInventory().addItem(perkMenuLoader.clone());
    }

    public Location getGlobalLobby() {
        return globalLobby;
    }

    public void setGlobalLobby(Location loc) {
        globalLobby = loc;
    }
}
