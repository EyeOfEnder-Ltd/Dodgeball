package com.eyeofender.dodgeball.connect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.bukkit.entity.Player;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.connect.table.Perks;
import com.eyeofender.dodgeball.connect.table.Stats;
import com.google.common.collect.Maps;

public class DatabaseConnection {

    private Dodgeball plugin;
    private static Map<String, Stats> statsCache = Maps.newHashMap();

    public DatabaseConnection(Dodgeball plugin) {
        this.plugin = plugin;

        try {
            plugin.getDatabase().find(Perks.class).findRowCount();
            plugin.getDatabase().find(Stats.class).findRowCount();
        } catch (PersistenceException ex) {
            Dodgeball.instance.logInfo("Installing database due to first time usage");
            Dodgeball.instance.installDDL();
        }
    }

    public Perks getPerks(Player player) {
        Perks perks = plugin.getDatabase().find(Perks.class).where().ieq("name", player.getName()).findUnique();

        if (perks == null) {
            perks = new Perks();
            perks.setPlayer(player);
            perks.setLifeBoost(0);
            perks.setBallBoost(0);
            perks.setAirstrikes(0);
            perks.setTripleShots(0);
            perks.setLifeGainedOnHit(false);
            perks.setSpeedBoost(false);
            perks.setStartingBalls(0);
            perks.setExtraLives(0);
            plugin.getDatabase().save(perks);
        }

        return perks;
    }

    public void savePerks(Perks perks) {
        plugin.getDatabase().update(perks);
    }

    public Stats getStats(Player player, boolean refresh) {
        Stats stats;

        if (!refresh) {
            stats = statsCache.get(player.getName());
            return stats != null ? stats : getStats(player, true);
        }

        stats = plugin.getDatabase().find(Stats.class).where().ieq("name", player.getName()).findUnique();

        if (stats == null) {
            stats = new Stats();
            stats.setPlayer(player);
            stats.setGamesPlayed(0);
            stats.setGamesWon(0);
            stats.setGamesLost(0);
            stats.setTotalHits(0);
            stats.setTotalMisses(0);
            stats.setTotalHarm(0);
            stats.setLastSeen(null);
            plugin.getDatabase().save(stats);
            statsCache.put(stats.getName(), stats);
        }

        return stats;
    }

    public void saveStats(Stats stats) {
        statsCache.put(stats.getName(), stats);
        plugin.getDatabase().update(stats);
    }

    public void setupPlayer(Player player) {
        getPerks(player);
        getStats(player, true);
    }

    public static List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Perks.class);
        list.add(Stats.class);
        return list;
    }

}
