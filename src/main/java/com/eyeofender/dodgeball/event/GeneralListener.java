package com.eyeofender.dodgeball.event;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.connect.DatabaseConnection;
import com.eyeofender.dodgeball.connect.table.Stats;
import com.eyeofender.dodgeball.game.Arena;
import com.eyeofender.dodgeball.game.perks.ActivePerks;
import com.eyeofender.dodgeball.game.perks.FiringMode;
import com.eyeofender.dodgeball.game.perks.PerkManager;

public class GeneralListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (Dodgeball.instance.getGameManager().getArenaFromSpectator(event.getPlayer()) != null) {
            event.setCancelled(true);
            return;
        }
        if (Dodgeball.instance.getGameManager().getArena(event.getPlayer()) != null && !event.getPlayer().hasPermission("dodgeball.arena.build")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Dodgeball.prefix + ChatColor.RED + "You can't break blocks while in-game!");
            return;
        }
        Arena arena = Dodgeball.instance.getGameManager().getArenaFromSignLocation(event.getBlock().getLocation());
        if (arena == null) return;
        if (!event.getPlayer().hasPermission("dodgeball.arena.signs")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Dodgeball.prefix + ChatColor.RED + "You don't have permission to break arena signs!");
            return;
        }
        arena.removeSignLocation(event.getBlock().getLocation());
        event.getPlayer().sendMessage(Dodgeball.prefix + ChatColor.GREEN + "Removed a sign for arena: " + arena.getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (Dodgeball.instance.getGameManager().getArenaFromSignLocation(event.getBlock().getLocation()) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Dodgeball.prefix + ChatColor.RED + "Arena signs cannot be edited!");
            return;
        }
        if (!event.getLine(0).equalsIgnoreCase("dodgeball")) return;
        if (!event.getPlayer().hasPermission("dodgeball.arena.signs")) {
            event.getPlayer().sendMessage(Dodgeball.prefix + ChatColor.RED + "You don't have permission to create arena signs!");
            event.setCancelled(true);
            return;
        }
        Arena arena = Dodgeball.instance.getGameManager().getArena(event.getLine(1));
        if (arena == null) {
            event.getPlayer().sendMessage(Dodgeball.prefix + ChatColor.RED + "The sign contains an invalid arena name!");
            event.setCancelled(true);
            return;
        }
        event.setLine(0, arena.getName());
        event.setLine(1, arena.getStageName());
        event.setLine(2, arena.getPlayerCount() + "/" + arena.getPlayerLimit());
        event.setLine(3, arena.getStage() == 0 ? "To start: " + Integer.toString(arena.getStartCount() - arena.getPlayerCount()) : null);
        arena.addSignLocation(event.getBlock().getLocation());
        event.getPlayer().sendMessage(Dodgeball.prefix + ChatColor.GREEN + "Created a sign for arena: " + arena.getName());
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                if (event.getClickedBlock() != null) {
                    Arena arena = Dodgeball.instance.getGameManager().getArenaFromSignLocation(event.getClickedBlock().getLocation());
                    if (arena != null) {
                        if (!arena.addPlayer(player)) return;
                        player.teleport(arena.getLobby());
                        player.sendMessage(Dodgeball.prefix + "Now playing in arena " + arena.getName() + "!");
                        if (arena.getStage() == 0 && arena.getPlayerCount() >= arena.getStartCount()) {
                            arena.broadcast("Start count reached! The round will begin shortly.");
                            arena.setStage(1);
                        }
                        return;
                    }
                }
            case RIGHT_CLICK_AIR:
                ItemStack holding = event.getItem();
                if (holding == null) return;

                if (!holding.hasItemMeta() || !holding.getItemMeta().hasDisplayName()) return;
                if (holding.getItemMeta().getDisplayName().substring(2).equals("Open Arena Menu")) {
                    event.setCancelled(true);
                    player.openInventory(Dodgeball.instance.getGameManager().getArenaMenu("main"));
                    return;
                }
                if (holding.getItemMeta().getDisplayName().substring(2).equals("Open Perk Menu")) {
                    event.setCancelled(true);
                    player.openInventory(PerkManager.getPerkMenu(player));
                    return;
                }

                Arena arena = Dodgeball.instance.getGameManager().getArena(player);
                if (arena == null || arena.getStage() != 2) return;

                int newAmount = holding.getAmount() - 1;
                switch (holding.getType()) {
                    case SLIME_BALL:
                        event.setCancelled(true);
                        player.getInventory().addItem(FiringMode.STANDARD.getDodgeball(arena));

                        if (newAmount > 0) {
                            holding.setAmount(newAmount);
                        } else {
                            player.getInventory().remove(holding);
                        }
                        player.updateInventory();
                        return;
                    case POTION:
                        event.setCancelled(true);
                        double health = player.getHealth() + 2.0;
                        if (health > player.getMaxHealth()) return;
                        player.setHealth(health);

                        if (newAmount > 0) {
                            holding.setAmount(newAmount);
                        } else {
                            player.getInventory().remove(holding);
                        }
                        player.updateInventory();
                        return;
                    case DIAMOND_BOOTS:
                        event.setCancelled(true);
                        ItemMeta meta = event.getItem().getItemMeta();
                        boolean disabled = meta.getDisplayName().contains("OFF");
                        meta.setDisplayName("Super Speed: " + (disabled ? ChatColor.GREEN + "ON" : ChatColor.DARK_RED + "OFF"));
                        event.getItem().setItemMeta(meta);
                        player.setWalkSpeed(disabled ? 0.3f : 0.2f);
                        player.updateInventory();
                        return;
                    case SNOW_BALL:
                        FiringMode mode = FiringMode.getByName(holding.getItemMeta().getDisplayName());
                        if (mode != null) ActivePerks.get(player).setFiringMode(mode);
                    default:
                        return;
                }
            default:
                break;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (event.getSlot() == 39) {
            event.setCancelled(true);
            return;
        }
        if (!(clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName())) return;
        if (clicked.getItemMeta().getDisplayName().substring(2).equals("Open Arena Menu")) {
            event.setCancelled(true);
            player.closeInventory();
            player.openInventory(Dodgeball.instance.getGameManager().getArenaMenu("main"));
            return;
        }
        if (clicked.getItemMeta().getDisplayName().substring(2).equals("Open Perk Menu")) {
            event.setCancelled(true);
            player.closeInventory();
            player.openInventory(PerkManager.getPerkMenu(player));
            return;
        }

        if (event.getInventory().getSize() == 9) { // Arena or perk menu
            if (event.getInventory().getTitle().equals("Perk Menu")) {
                event.setCancelled(true);
                PerkManager.toggleActive(clicked, player);
                return;
            }
            if (event.getInventory().getTitle().equals("Arena Map Menu")) {
                event.setCancelled(true);
                player.closeInventory();
                player.openInventory(Dodgeball.instance.getGameManager().getArenaMenu(event.getCurrentItem().getItemMeta().getDisplayName().substring(2)));
                return;
            }
            if (Dodgeball.instance.getGameManager().containsArenaMenu(event.getInventory().getTitle().replace(" Arenas", ""))) {
                event.setCancelled(true);
                Arena arena = Dodgeball.instance.getGameManager().getArena(event.getCurrentItem().getItemMeta().getDisplayName().substring(2));
                if (arena == null) { // Take player back to previous menu
                    player.closeInventory();
                    player.openInventory(Dodgeball.instance.getGameManager().getArenaMenu("main"));
                    return;
                }

                if (!arena.addPlayer(player)) return;
                player.teleport(arena.getLobby());
                player.sendMessage(Dodgeball.prefix + "Now playing in arena " + arena.getName() + "!");
                if (arena.getStage() == 0 && arena.getPlayerCount() >= arena.getStartCount()) {
                    arena.broadcast("Start count reached! The round will begin shortly.");
                    arena.setStage(1);
                }
            }
            return;
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() != Material.SNOW_BALL && !event.getPlayer().isOp()) event.setCancelled(true);
    }

    @EventHandler
    public void onImpact(ProjectileHitEvent event) {
        event.getEntity().remove();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player joined = event.getPlayer();

        DatabaseConnection.setupPlayer(joined);

        Dodgeball.instance.getServer().getScheduler().scheduleSyncDelayedTask(Dodgeball.instance, new Runnable() {
            public void run() {
                Location globby = Dodgeball.instance.getGameManager().getGlobalLobby();
                if (globby != null) joined.teleport(globby);
                Dodgeball.instance.getGameManager().loadGeneralInventory(joined);
            }
        }, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Arena arena = Dodgeball.instance.getGameManager().getArena(event.getPlayer());
        if (arena != null) {
            arena.broadcast(event.getPlayer().getName() + " has fled the arena!");
            arena.removePlayer(event.getPlayer(), false, false);
            return;
        }
        arena = Dodgeball.instance.getGameManager().getArenaFromSpectator(event.getPlayer());
        if (arena != null) arena.removeSpectator(event.getPlayer(), false);

        Stats stats = DatabaseConnection.getStats(event.getPlayer(), false);
        stats.setLastSeen(new java.util.Date());
        DatabaseConnection.saveStats(stats);
    }

    @EventHandler
    public void onKicked(PlayerKickEvent event) {
        Arena arena = Dodgeball.instance.getGameManager().getArena(event.getPlayer());
        if (arena != null) {
            arena.broadcast(event.getPlayer().getName() + " has fled the arena!");
            arena.removePlayer(event.getPlayer(), false, false);
            return;
        }
        arena = Dodgeball.instance.getGameManager().getArenaFromSpectator(event.getPlayer());
        if (arena != null) arena.removeSpectator(event.getPlayer(), false);

        Stats stats = DatabaseConnection.getStats(event.getPlayer(), false);
        stats.setLastSeen(new java.util.Date());
        DatabaseConnection.saveStats(stats);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(Dodgeball.instance.getGameManager().getGlobalLobby());
        Dodgeball.instance.getGameManager().loadGeneralInventory(event.getPlayer());
    }
}
