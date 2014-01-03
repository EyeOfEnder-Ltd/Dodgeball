package com.eyeofender.dodgeball.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.eyeofender.dodgeball.Dodgeball;

public class GameCountdown {

    private Dodgeball plugin;

    private long time = 0;

    private long timeRemaining = 0;
    private boolean running;
    private int taskID;

    public GameCountdown(Dodgeball plugin, long time) {
        this.plugin = plugin;
        this.time = time;
        this.timeRemaining = time;
    }

    public long getTimeRemaining() {
        return timeRemaining / 10;
    }

    public boolean isRunning() {
        return running;
    }

    public void onTimeChange(long time) {
        if (time > 15 && time % 15 == 0) {
            long minutes = time / 60;
            long seconds = time - (minutes * 60);
            StringBuilder builder = new StringBuilder();

            if (minutes > 0) {
                builder.append(minutes + " minute");
                if (minutes != 1) builder.append("s");
            }

            if (minutes > 0 && seconds > 0) builder.append(", ");

            if (seconds > 0) {
                builder.append(seconds + " second");
                if (seconds != 1) builder.append("s");
            }

            Bukkit.broadcastMessage(ChatColor.AQUA + "Game starting in " + builder.toString() + "!");
        } else if (time <= 15) {
            Bukkit.broadcastMessage(ChatColor.AQUA + "Game starting in " + time + "!");
            Location source = Bukkit.getWorlds().get(0).getSpawnLocation();
            source.getWorld().playSound(source, Sound.NOTE_PLING, 10f, 10f);
        }
    }

    public void onTimerEnd() {
        plugin.getGame().start();
    }

    public void setTime(long time) {
        this.time = time * 10;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining * 10;
    }

    public void start() {
        BukkitTask task = new TimerTask().runTaskTimer(plugin, 20L, 20L);
        taskID = task.getTaskId();
        running = true;
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(taskID);
        running = false;
        timeRemaining = time;
    }

    class TimerTask extends BukkitRunnable {

        @Override
        public void run() {
            timeRemaining--;

            if (timeRemaining <= 0) {
                onTimerEnd();
                stop();
                return;
            }

            onTimeChange(timeRemaining);
        }

    }

}