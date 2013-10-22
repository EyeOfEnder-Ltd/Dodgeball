package com.eyeofender.dodgeball.event;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;

import com.eyeofender.dodgeball.connect.DatabaseConnection;
import com.eyeofender.dodgeball.connect.table.Stats;
import com.google.common.collect.Maps;

public class StatsSignManager implements Listener {

    private static final String UNKNOWN_PLAYER = "MHF_Question";
    private static final long MINUTE = 20 * 60;

    private Plugin plugin;

    private FileConfiguration signConfig;
    private File signConfigFile;

    private Map<Location, Statistic> signs;
    private Map<Statistic, String> stats = Maps.newHashMap();

    public StatsSignManager(Plugin plugin) {
        this.plugin = plugin;
        loadLocations();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                update();
            }
        }, 0, MINUTE * 30); // Every half hour
    }

    private void loadLocations() {
        if (signConfigFile == null) {
            signConfigFile = new File(plugin.getDataFolder(), "signs.dat");
        }
        signConfig = YamlConfiguration.loadConfiguration(signConfigFile);

        signs = Maps.newHashMap();
        List<String> list = signConfig.getStringList("signs");

        for (String entry : list) {
            String[] vars = entry.split(", ");
            Location loc = new Location(Bukkit.getWorld(vars[1]), Double.parseDouble(vars[2]), Double.parseDouble(vars[3]), Double.parseDouble(vars[4]));
            signs.put(loc, Statistic.getByName(vars[0]));
        }
    }

    public void saveLocations() {
        if (signConfig == null || signConfigFile == null) return;

        signConfig.set("signs", null);
        List<String> list = new ArrayList<String>();

        for (Entry<Location, Statistic> entry : signs.entrySet()) {
            Location loc = entry.getKey();
            String coords = loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();

            list.add(entry.getValue().getName() + ", " + loc.getWorld().getName() + ", " + coords);
        }

        signConfig.set("signs", list);

        try {
            signConfig.save(signConfigFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save signs to " + signConfigFile, ex);
        }
    }

    private void update() {
        String topAccuracy = UNKNOWN_PLAYER;
        String topHitHarm = UNKNOWN_PLAYER;

        double numAccuracy = 0;
        double numHitHarm = 0;

        plugin.getLogger().info("Updating statistic signs...");

        for (Stats stats : DatabaseConnection.getStats()) {
            String name = stats.getName();

            double hits = stats.getTotalHits();
            double harm = stats.getTotalHarm();
            double shots = hits + stats.getTotalMisses();

            double accuracy = (hits / (shots > 0 ? shots : 1)) * 100;
            double hitHarm = hits / (harm > 0 ? harm : 1);

            if (accuracy >= numAccuracy) {
                numAccuracy = accuracy;
                topAccuracy = name;
            }

            if (hitHarm >= numHitHarm) {
                numHitHarm = hitHarm;
                topHitHarm = name;
            }

        }

        stats.put(Statistic.TOP_ACCURACY, topAccuracy + ", " + round(numAccuracy, 2) + "%");
        stats.put(Statistic.TOP_HIT_HURT, topHitHarm + ", " + round(numHitHarm, 2));

        for (Entry<Location, Statistic> entry : signs.entrySet()) {
            update(entry.getKey(), entry.getValue());
        }

        plugin.getLogger().info("Update complete.");
    }

    private void update(Location location, Statistic stat) {
        Block block = location.getBlock();
        if (block == null) return;

        String[] pair = stats.get(stat).split(", ");
        String name = pair[0];
        String value = pair[1];

        Sign sign = (Sign) block.getState();
        sign.setLine(0, ChatColor.DARK_AQUA + stat.getTitle());
        sign.setLine(1, "");
        sign.setLine(2, ChatColor.GREEN + name != UNKNOWN_PLAYER ? name : "???");
        sign.setLine(3, value);
        sign.update();

        setHead(block.getRelative(BlockFace.UP), name);
    }

    private void setHead(Block block, String name) {
        if (block.isEmpty() || block.getType() == Material.SKULL) {
            block.setType(Material.SKULL);

            Skull skull = (Skull) block.getState();
            BlockFace face = ((org.bukkit.material.Sign) block.getRelative(BlockFace.DOWN).getState().getData()).getAttachedFace();

            skull.setSkullType(SkullType.PLAYER);
            skull.setOwner(name);
            skull.setRotation(face.getOppositeFace());
            skull.update();
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getLine(0).equalsIgnoreCase("[DodgeStat]")) return;

        Statistic stat = Statistic.getByName(event.getLine(1).toLowerCase());
        if (stat == null) {
            event.getPlayer().sendMessage(ChatColor.RED + "Invalid Statistic!");
            return;
        }

        Block block = event.getBlock();
        event.setLine(0, ChatColor.DARK_AQUA + stat.getTitle());
        event.setLine(1, "");
        event.setLine(2, "- Awaiting - ");
        event.setLine(3, "- Update -");

        setHead(block.getRelative(BlockFace.UP), UNKNOWN_PLAYER);

        signs.put(block.getLocation(), stat);
        saveLocations();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();

        if (signs.containsKey(loc)) {
            signs.remove(loc);
        }
    }

    private double round(double unrounded, int dp) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(dp, BigDecimal.ROUND_HALF_UP);
        return rounded.doubleValue();
    }

    private enum Statistic {
        TOP_HIT_HURT("top-hit-hurt", "Top Hit/Hurt"),
        TOP_ACCURACY("top-accuracy", "Top Accuracy");

        private static final Map<String, Statistic> BY_NAME = Maps.newHashMap();
        private String name;
        private String title;

        private Statistic(String name, String title) {
            this.name = name;
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public String getTitle() {
            return title;
        }

        public static Statistic getByName(String name) {
            return BY_NAME.get(name);
        }

        static {
            for (Statistic stat : values()) {
                BY_NAME.put(stat.getName(), stat);
            }
        }
    }
}
