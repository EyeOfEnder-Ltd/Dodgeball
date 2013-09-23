package com.eyeofender.dodgeball.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.connect.DatabaseConnection;
import com.eyeofender.dodgeball.connect.table.Stats;
import com.eyeofender.dodgeball.game.Arena;
import com.eyeofender.dodgeball.game.perks.ActivePerks;
import com.eyeofender.dodgeball.game.perks.Perk;

public class GameListener implements Listener {

    private final String[] commandLabels = { "create", "delete", "save", "rename", "join", "spectate", "leave", "team", "setgloballobby", "setlobby", "addspawn", "clearspawns", "setline",
            "clearlines", "arenalist", "arenainfo", "playerlist", "search", "eject", "fstart", "fstop", "addball", "clearballs", "friendlyfire", "setballlimit", "setlimit", "setstartcount" };
    private final ItemStack dodgeball;

    private boolean listening = false;
    private HashMap<UUID, ProjectileType> trackedProjectiles;

    public GameListener() {
        trackedProjectiles = new HashMap<UUID, ProjectileType>();
        dodgeball = new ItemStack(Material.SNOW_BALL, 1);
        ItemMeta meta = dodgeball.getItemMeta();
        meta.setDisplayName("Dodgeball");
        dodgeball.setItemMeta(meta);
    }

    public boolean isRegistered() {
        return listening;
    }

    // Lazy registration ftw!
    public void setRegistered(boolean registered) {
        if (listening == registered) // Redundant
            return;
        if (registered) Dodgeball.instance.getServer().getPluginManager().registerEvents(this, Dodgeball.instance);
        else
            HandlerList.unregisterAll(this);
        listening = registered;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBuild(BlockPlaceEvent event) {
        if (Dodgeball.instance.getGameManager().getArenaFromSpectator(event.getPlayer()) != null) {
            event.setCancelled(true);
            return;
        }
        if (Dodgeball.instance.getGameManager().getArena(event.getPlayer()) != null && !event.getPlayer().hasPermission("dodgeball.arena.build")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Dodgeball.prefix + ChatColor.RED + "You can't place blocks while in-game!");
            return;
        }
    }

    @EventHandler
    public void onCombat(EntityDamageByEntityEvent event) {
        event.setCancelled(true);
        if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.PROJECTILE || !(event.getDamager() instanceof Snowball || event.getDamager() instanceof Arrow)) return;
        Projectile proj = (Projectile) event.getDamager();
        Player defender = (Player) event.getEntity();
        if (Dodgeball.instance.getGameManager().getArenaFromSpectator(defender) != null) {
            Projectile newProj = (Projectile) proj.getWorld().spawnEntity(proj.getLocation().add(proj.getVelocity().normalize().multiply(4)), EntityType.SNOWBALL);
            newProj.setVelocity(proj.getVelocity());
            newProj.setShooter(proj.getShooter());
            if (proj instanceof Snowball) {
                ProjectileType previous = trackedProjectiles.put(proj.getUniqueId(), ProjectileType.DOOMED);
                if (previous != null) trackedProjectiles.put(newProj.getUniqueId(), previous);
            }
            return;
        }
        final Arena defenderArena = Dodgeball.instance.getGameManager().getArena(defender);
        if (defenderArena == null || defenderArena.getStage() != 2) return;
        Player shooter = (Player) ((Projectile) event.getDamager()).getShooter();
        Arena attackerArena = Dodgeball.instance.getGameManager().getArena(shooter);
        if (attackerArena == null || attackerArena != defenderArena) return;
        if (!defenderArena.isFriendlyFireEnabled() && defenderArena.getTeam(defender).equals(defenderArena.getTeam(shooter))) return;
        double health = defender.getHealth() - (Dodgeball.instance.getConfig().getInt((defender.hasPermission("dodgeball.lives.vip") ? "lives-vip" : "lives-standard") + ".lost-on-death") * 2.0);
        if (Dodgeball.broadcastOnHit) {
            defenderArena.broadcast(defender.getName() + " was hit by " + shooter.getName() + "!");
        } else {
            defender.sendMessage(Dodgeball.prefix + "You were hit by " + shooter.getName() + "!");
            shooter.sendMessage(Dodgeball.prefix + "You hit " + defender.getName() + "!");
        }

        DatabaseConnection database = Dodgeball.instance.getDatabaseConnection();

        Stats stats = database.getStats(defender, false);
        stats.setTotalHarm(stats.getTotalHarm() + 1);
        database.saveStats(stats);

        stats = database.getStats(shooter, false);
        stats.setTotalHits(stats.getTotalHits() + 1);

        int gain = Dodgeball.instance.getConfig().getInt((defender.hasPermission("dodgeball.lives.vip") ? "lives-vip" : "lives-standard") + ".gained-on-hit");
        if (ActivePerks.get(shooter).isActive(Perk.LIFE_GAINED_ON_HIT)) gain += 1;
        if (gain != 0) {
            double healthAfterGain = shooter.getHealth() + gain * 2.0;
            shooter.setHealth(healthAfterGain > shooter.getMaxHealth() ? shooter.getMaxHealth() : healthAfterGain);
        }
        final Player defenderCopy = defender;
        if (health <= 0) {
            defenderArena.broadcast(defenderCopy.getName() + " has been eliminated!");
            defenderArena.removePlayer(defenderCopy, true, true);
            return;
        }
        defenderCopy.setHealth(health);
        Dodgeball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Dodgeball.instance, new Runnable() {
            public void run() {
                defenderCopy.teleport(defenderArena.getRandomSpawnPoint(defenderArena.getTeam(defenderCopy)));
            }
        }, 1L);
        return;
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (Dodgeball.instance.getGameManager().getArena(event.getPlayer()) == null) return;
        if (event.getPlayer().hasPermission("dodgeball.arena.allowcommands")) return;
        for (String label : commandLabels)
            if (event.getMessage().startsWith(label, 1)) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(Dodgeball.prefix + ChatColor.RED + "Only Dodgeball commands are permitted in arenas.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player) || Dodgeball.instance.getGameManager().getArenaFromSpectator((Player) event.getEntity()) != null) return;
        if (Dodgeball.instance.getGameManager().getArena((Player) event.getEntity()) != null && !event.getCause().equals(DamageCause.PROJECTILE)) {
            event.setCancelled(true);
            if (event.getCause().equals(DamageCause.FIRE) || event.getCause().equals(DamageCause.FIRE_TICK)) event.getEntity().setFireTicks(0);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (Dodgeball.instance.getGameManager().getArena((Player) event.getEntity()) != null) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (Dodgeball.instance.getGameManager().getArena((Player) event.getEntity()) != null || Dodgeball.instance.getGameManager().getArenaFromSpectator((Player) event.getEntity()) != null)
            event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (!item.getType().equals(Material.SNOW_BALL)) return;

        Player player = event.getPlayer();
        Arena arena = Dodgeball.instance.getGameManager().getArenaFromSpectator(player);
        if (arena != null) {
            event.setCancelled(true);
        } else {
            arena = Dodgeball.instance.getGameManager().getArena(player);
            if (arena == null) return;
        }

        if (player.getInventory().contains(event.getItem().getItemStack(), Dodgeball.instance.getConfig().getInt("max-dodgeballs-held"))) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        Arena arena = Dodgeball.instance.getGameManager().getArena(player);
        if (arena == null || arena.getStage() != 2) return;
        if (arena.isLocationInMidline(event.getTo())) {
            event.setCancelled(true);
            Vector trajectory = event.getTo().toVector().subtract(event.getFrom().toVector());
            Location bounceBack = arena.asBlockLocation(event.getFrom().add(-Math.signum(trajectory.getX()), 0, -Math.signum(trajectory.getZ())));
            final Location destination = arena.isLocationInMidline(bounceBack) ? arena.asBlockLocation(event.getFrom()) : bounceBack;
            Dodgeball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Dodgeball.instance, new Runnable() {
                public void run() {
                    player.teleport(destination);
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntityType() != EntityType.SNOWBALL || !(event.getEntity().getShooter() instanceof Player)) return;
        Player shooter = (Player) event.getEntity().getShooter();
        DatabaseConnection database = Dodgeball.instance.getDatabaseConnection();
        Stats stats = database.getStats(shooter, false);

        switch (ActivePerks.get(shooter).getFiringMode()) {
            case STANDARD:
                break;
            case AIRSTRIKE:
                trackedProjectiles.put(event.getEntity().getUniqueId(), ProjectileType.AIRSTRIKE);
                stats.setAirstrikesFired(stats.getAirstrikesFired() + 1);
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
                stats.setTrippleShotsFired(stats.getTrippleShotsFired() + 1);
                break;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball) || !(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity().getShooter();
        Arena arena = Dodgeball.instance.getGameManager().getArena(player);
        if (arena == null || arena.getStage() != 2) return;
        ProjectileType type = trackedProjectiles.remove(event.getEntity().getUniqueId());
        if (type == ProjectileType.DOOMED) return;
        if (type == ProjectileType.AIRSTRIKE) {
            arena.broadcast("" + ChatColor.RED + ChatColor.ITALIC + "Airstrike inbound!");
            launchAirstrike(player, event.getEntity().getLocation(), 3, 3);
            return;
        }

        Stats stats = Dodgeball.instance.getDatabaseConnection().getStats(player, false);
        stats.setTotalMisses(stats.getTotalMisses() + 1);

        ItemStack ball = dodgeball.clone();
        ItemMeta meta = ball.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add("Arena:");
        lore.add(arena.getName());
        meta.setLore(lore);
        ball.setItemMeta(meta);
        event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), ball);
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

    /**
     * Used to track projectiles that are marked for removal or cause special
     * behavior upon impact
     */
    private enum ProjectileType {

        /**
         * Denotes a dodgeball that will trigger an airstrike where it lands
         */
        AIRSTRIKE,

        /**
         * Denotes a projectile marked for deletion upon impact
         */
        DOOMED;
    }
}
