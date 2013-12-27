package com.eyeofender.dodgeball.listener;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.Game;
import com.eyeofender.dodgeball.Game.State;
import com.eyeofender.dodgeball.perk.ActivePerks;
import com.eyeofender.dodgeball.perk.Perk;
import com.google.common.collect.Maps;

public class GameListener implements Listener {

    private Dodgeball plugin;
    private Map<UUID, ProjectileType> trackedProjectiles;

    public GameListener(Dodgeball plugin) {
        this.plugin = plugin;
        this.trackedProjectiles = Maps.newHashMap();
    }

    private boolean isInGame(Player player) {
        Game game = plugin.getGame();
        return game.isPlayer(player) && game.getState() == State.IN_GAME;
    }

    private enum ProjectileType {
        AIRSTRIKE,
        DOOMED;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
        if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.PROJECTILE || !(event.getDamager() instanceof Snowball || event.getDamager() instanceof Arrow)) return;
        Projectile proj = (Projectile) event.getDamager();
        Player defender = (Player) event.getEntity();
        if (!isInGame(defender)) return;
        ProjectileType previous = null;

        if (proj instanceof Snowball) previous = trackedProjectiles.put(proj.getUniqueId(), ProjectileType.DOOMED);

        Player shooter = (Player) proj.getShooter();
        if (!isInGame(shooter)) return;

        double health = defender.getHealth() - 2.0;

        String defenderName = plugin.getGame().getTeam(defender).getChatColour() + defender.getName();
        String shooterName = plugin.getGame().getTeam(shooter).getChatColour() + shooter.getName();

        defender.sendMessage(ChatColor.RED + "You were hit by " + shooterName);
        shooter.sendMessage(ChatColor.GREEN + "You hit " + defenderName);
        Bukkit.broadcastMessage(defenderName + ChatColor.GRAY + " was hit by " + shooterName);

        if (ActivePerks.get(shooter).isActive(Perk.LIFE_GAINED_ON_HIT)) {
            double healthAfterGain = shooter.getHealth() + 2.0;
            shooter.setHealth(healthAfterGain > shooter.getMaxHealth() ? shooter.getMaxHealth() : healthAfterGain);
        }

        if (health <= 0) {
            defender.getWorld().strikeLightningEffect(defender.getLocation());
            Bukkit.broadcastMessage(defenderName + " has been eliminated!");
            Bukkit.broadcastMessage(ChatColor.AQUA + "" + (plugin.getGame().getPlayerCount() - 1) + " players remain!");

            plugin.getGame().addSpectator(defender, false);
            return;
        } else {
            defender.getWorld().createExplosion(defender.getLocation(), 0, false);
            defender.setHealth(health);
        }

        if (previous != ProjectileType.DOOMED || previous != ProjectileType.AIRSTRIKE) plugin.getGame().dropDodgeball(proj.getLocation());

        plugin.getGame().respawn(defender);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.SNOWBALL || !(event.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) event.getEntity().getShooter();

        if (!isInGame(shooter)) {
            event.setCancelled(true);
            return;
        }

        switch (ActivePerks.get(shooter).getFiringMode()) {
            case STANDARD:
                break;
            case AIRSTRIKE:
                trackedProjectiles.put(event.getEntity().getUniqueId(), ProjectileType.AIRSTRIKE);
                break;
            case TRIPLE_SHOT:
                Vector vel = event.getEntity().getVelocity().clone();
                Projectile ball = (Projectile) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.SNOWBALL);
                ball.setVelocity(rotateAroundYAxis(vel, -15.0));
                ball.setShooter(shooter);
                trackedProjectiles.put(ball.getUniqueId(), ProjectileType.DOOMED);
                ball = (Projectile) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.SNOWBALL);
                ball.setVelocity(rotateAroundYAxis(vel, 30.0));
                ball.setShooter(shooter);
                trackedProjectiles.put(ball.getUniqueId(), ProjectileType.DOOMED);
                break;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball) || !(event.getEntity().getShooter() instanceof Player)) return;
        Player player = (Player) event.getEntity().getShooter();

        if (!isInGame(player)) {
            return;
        }

        ProjectileType type = trackedProjectiles.remove(event.getEntity().getUniqueId());
        if (type == ProjectileType.DOOMED) return;
        if (type == ProjectileType.AIRSTRIKE) {
            Bukkit.broadcastMessage("" + ChatColor.RED + ChatColor.ITALIC + "Airstrike inbound!");
            launchAirstrike(player, event.getEntity().getLocation(), 3, 3);
            return;
        }

        plugin.getGame().dropDodgeball(event.getEntity().getLocation());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (plugin.getGame().isSpectator(player)) {
            if (event.getTo().getY() != event.getFrom().getY()) {
                event.setTo(event.getFrom());
            }
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGame().isSpectator(player)) event.setCancelled(true);
    }

    private void launchAirstrike(LivingEntity shooter, Location target, int radiusX, int radiusZ) {
        target.add((double) -(radiusX + 1), 10.0, (double) -(radiusZ + 1));
        int distZ = radiusZ * 2;
        for (int i = 0; i <= radiusX * 2; i++) {
            target.add(1.0, 0.0, 0.0);
            for (int j = 0; j <= distZ; j++) {
                target.add(0.0, 0.0, 1.0);
                if ((i + j) % 2 != 0) continue;
                Projectile bomb = (Projectile) target.getWorld().spawnEntity(target, EntityType.ARROW);
                bomb.setShooter(shooter);
                trackedProjectiles.put(bomb.getUniqueId(), ProjectileType.DOOMED);
            }
            target.subtract(0.0, 0.0, distZ + 1);
        }
    }

    private Vector rotateAroundYAxis(Vector vector, double degrees) {
        degrees *= Math.PI / 180.0; // Convert to radians for trig functions
        double sin = Math.sin(degrees), cos = Math.cos(degrees);
        double x = vector.getX() * cos - vector.getZ() * sin;
        double z = vector.getX() * sin + vector.getZ() * cos;
        return vector.setX(x).setZ(z);
    }

}
