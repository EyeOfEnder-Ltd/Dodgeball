package com.eyeofender.dodgeball.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.eyeofender.dodgeball.Dodgeball;

public class GameTimer {

    private Dodgeball plugin;

    private long time = 0;

    private long timeRemaining = 0;
    private boolean running;
    private int taskID;

    public GameTimer(Dodgeball plugin, long time) {
        this.plugin = plugin;
        this.time = time;
        this.timeRemaining = time;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public boolean isRunning() {
        return running;
    }

    public void onTimeChange(long time) {
        if (time % 2 == 0) {
            plugin.getGame().spawnDodgeball(null, true);
        }

        if (time % 60 == 0) {
            Bukkit.broadcastMessage(ChatColor.AQUA + "" + (time / 60) + " minute(s) remaining!");
        }

        if (time <= 10) {
            Bukkit.broadcastMessage(ChatColor.AQUA + "Game ending in " + time + "!");
        }
    }

    public void onTimerEnd() {
        plugin.getGame().stop();
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
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