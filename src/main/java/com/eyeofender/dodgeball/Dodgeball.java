package com.eyeofender.dodgeball;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.eyeofender.dodgeball.command.CommandManager;
import com.eyeofender.dodgeball.connect.DatabaseConnection;
import com.eyeofender.dodgeball.event.GameListener;
import com.eyeofender.dodgeball.event.GeneralListener;
import com.eyeofender.dodgeball.event.StatsSignManager;
import com.eyeofender.dodgeball.game.GameManager;
import com.eyeofender.massapi.MassAPI;

public class Dodgeball extends JavaPlugin {

    public static final String prefix = ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE + "Dodgeball" + ChatColor.GRAY + "] ";

    public static Dodgeball instance;
    public static boolean broadcastOnHit;
    public static boolean saveOnEdit;

    private MassAPI api;
    private Commands cmdexe;
    private GameManager gameManager;
    private GameListener gameListener;
    private GeneralListener generalListener;
    private StatsSignManager statsSignListener;

    // private DonationListener donationListener;

    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        Dodgeball.instance = this;

        try {
            this.api = (MassAPI) pm.getPlugin("MassAPI");
        } catch (NoClassDefFoundError e) {
            logInfo("Unsupported or no version of MassAPI found.");
            pm.disablePlugin(this);
            return;
        }

        gameManager = new GameManager();
        logInfo("Loading the config...");
        saveDefaultConfig();
        reloadConfig();
        statsSignListener = new StatsSignManager(this);

        logInfo("Registering listeners...");
        gameListener = new GameListener();
        generalListener = new GeneralListener();
        pm.registerEvents(generalListener, this);
        pm.registerEvents(statsSignListener, this);

        logInfo("Registering command handlers...");
        cmdexe = new Commands();
        CommandManager.registerCommands(this);

        logInfo("Loading saved arenas...");
        gameManager.loadArenas();
        gameManager.updateArenaSigns();
        gameManager.loadArenaMenus();

        DatabaseConnection.init(this);

        logInfo("Starting round timer...");
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                gameManager.decrementTimers();
            }
        }, 10L, 20L);
        logInfo("Successfully enabled. Game on!");
    }

    public void onDisable() {
        if (api != null) {
            logInfo("Saving the config...");
            Location gl = gameManager.getGlobalLobby();
            getConfig().set("global-lobby.world", gl.getWorld().getName());
            getConfig().set("global-lobby.x", gl.getX());
            getConfig().set("global-lobby.y", gl.getY());
            getConfig().set("global-lobby.z", gl.getZ());
            getConfig().set("global-lobby.yaw", (double) gl.getYaw());
            getConfig().set("global-lobby.pitch", (double) gl.getPitch());
            saveConfig();
            logInfo("Saving current arenas...");

            statsSignListener.saveLocations();

            getServer().getScheduler().cancelTasks(this);
            gameManager.shutdown();
            gameManager = null;
        }

        logInfo("Successfully disabled. Game over!");
        instance = null;
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        return DatabaseConnection.getDatabaseClasses();
    }

    @Override
    public void installDDL() {
        super.installDDL();
    }

    public void reloadConfig() {
        super.reloadConfig();
        broadcastOnHit = getConfig().getBoolean("broadcast-on-hit");
        saveOnEdit = getConfig().getBoolean("save-arenas-on-edit");
        gameManager.setGlobalLobby(new Location(getServer().getWorld(getConfig().getString("global-lobby.world")), getConfig().getDouble("global-lobby.x"), getConfig().getDouble("global-lobby.y"),
                getConfig().getDouble("global-lobby.z"), (float) getConfig().getDouble("global-lobby.yaw"), (float) getConfig().getDouble("global-lobby.pitch")));
        gameManager.updateArenaMenus();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return cmdexe.run(sender, cmd, label, args);
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public GameListener getGameListener() {
        return gameListener;
    }

    public void logInfo(String msg) {
        getLogger().info(msg);
    }

    public void logWarning(String msg) {
        getLogger().warning(msg);
    }

    public void logSevere(String msg) {
        getLogger().severe(msg);
    }
}
