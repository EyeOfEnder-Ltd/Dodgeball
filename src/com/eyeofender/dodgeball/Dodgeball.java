package com.eyeofender.dodgeball;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.eyeofender.dodgeball.command.ArenaCommand;
import com.eyeofender.dodgeball.command.FStartCommand;
import com.eyeofender.dodgeball.command.FStopCommand;
import com.eyeofender.dodgeball.command.TeamCommand;
import com.eyeofender.dodgeball.listener.EventListener;
import com.eyeofender.dodgeball.listener.GameListener;
import com.eyeofender.dodgeball.listener.LobbyListener;
import com.eyeofender.enderpearl.EnderPearl;

public class Dodgeball extends JavaPlugin {

    private static Dodgeball instance;

    private EnderPearl api;
    private Game game;

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();
        instance = this;

        try {
            this.api = (EnderPearl) pm.getPlugin("EnderPearl");
        } catch (NoClassDefFoundError e) {
            log().info("Unsupported or no version of EnderPearl found.");
            pm.disablePlugin(this);
            return;
        }

        Arena.loadArenas();
        this.game = new Game(this);

        pm.registerEvents(new EventListener(this), this);
        pm.registerEvents(new LobbyListener(this), this);
        pm.registerEvents(new GameListener(this), this);

        getCommand("arena").setExecutor(new ArenaCommand());
        getCommand("fstart").setExecutor(new FStartCommand(this));
        getCommand("fstop").setExecutor(new FStopCommand(this));
        getCommand("team").setExecutor(new TeamCommand(this));

        log().info("Version " + getDescription().getVersion() + " enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (api != null) {
            game.stop();
            Arena.saveArenas();
        }

        log().info("Version " + getDescription().getVersion() + " has been disabled");
    }

    public static Dodgeball getInstance() {
        return instance;
    }
    
    public static void sendMessage(Player player, String message){
    	player.sendMessage(ChatColor.GREEN + "< " + ChatColor.GOLD + "DB" + ChatColor.GREEN + " > " + ChatColor.GRAY + message);
    }
    
    public static void broadcastMessage(String message){
    	for(Player player : Bukkit.getOnlinePlayers()){
    		player.sendMessage(ChatColor.GREEN + "< " + ChatColor.GOLD + "DB" + ChatColor.GREEN + " > " + ChatColor.GRAY + message);
    	}
    }

    public Logger log() {
        return getLogger();
    }

    public EnderPearl getApi() {
        return api;
    }

    public Game getGame() {
        return game;
    }

}
