package com.eyeofender.dodgeball.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.Game.State;
import com.eyeofender.dodgeball.util.Menu;

public class EventListener implements Listener {

    private Dodgeball plugin;

    public EventListener(Dodgeball plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp() || event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().isOp() || event.getPlayer().getGameMode() != GameMode.CREATIVE) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (plugin.getGame().getState() == State.DISABLED && !event.getPlayer().isOp()) {
            event.disallow(Result.KICK_OTHER, "This server is disabled.");
        } else if (plugin.getGame().getState() == State.RESTARTING) {
            event.disallow(Result.KICK_OTHER, "Server restarting!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getGame().addPlayer(event.getPlayer());
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGame().removePlayer(event.getPlayer(), false);
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        plugin.getGame().removePlayer(event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerPing(ServerListPingEvent event) {
        event.setMotd(plugin.getGame().getState().getMotd());
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() != Material.SNOW_BALL) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!canMove(player, event.getFrom(), event.getTo())) {
            event.setTo(event.getFrom());
        }
    }

    private boolean canMove(Player player, Location from, Location to) {
        if (from.getWorld() != to.getWorld()) return true;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return true;
        if (plugin.getGame().getState() != State.IN_GAME || plugin.getGame().isSpectator(player)) return true;

        return !plugin.getGame().getArena().isLocationInMidline(to);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClickHandle(InventoryClickEvent event) {
        Menu.handleClick(event);
    }

    @EventHandler
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if (event.getPlayer().getGameMode() == event.getNewGameMode()) return;
        if (event.getNewGameMode() == GameMode.CREATIVE) {
            plugin.getGame().removePlayer(event.getPlayer(), false);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!plugin.getGame().isSpectator(player)) event.getPlayer().showPlayer(player);
            }
        } else {
            plugin.getGame().addPlayer(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!event.getCause().equals(DamageCause.PROJECTILE)) {
            event.setCancelled(true);
            if (event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK)) {
                event.getEntity().setFireTicks(0);
            } else if (event.getCause() == DamageCause.VOID) {
                event.getEntity().teleport(event.getEntity().getWorld().getSpawnLocation());
            }

        }
    }

}
