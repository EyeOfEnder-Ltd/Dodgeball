package com.eyeofender.dodgeball.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.eyeofender.dodgeball.perk.PerkManager;
import com.eyeofender.enderpearl.Util;
import com.google.common.collect.ImmutableList;

public class Menu {

    private static final int START = 1;
    private static final int FINISH = 7;

    public static ItemStack flame;
    public static ItemStack star;
    public static ItemStack eye;
    public static ItemStack book;

    private static Inventory serverMenu;
    private static ItemStack hub;

    static {
        ItemMeta meta;

        flame = new ItemStack(Material.FIRE, 1);
        meta = flame.getItemMeta();
        meta.setDisplayName(ChatColor.RESET.toString());
        flame.setItemMeta(meta);

        star = PerkManager.getItem();

        eye = new ItemStack(Material.EYE_OF_ENDER, 1);
        meta = eye.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + "EOE Servers");
        meta.setLore(ImmutableList.of("Click to pick server"));
        eye.setItemMeta(meta);

        StringBuilder helpContents = new StringBuilder();
        String heading = "  -=- " + ChatColor.DARK_RED + ChatColor.BOLD + "Dodgeball" + ChatColor.RESET + ChatColor.BLACK + " -=-\n\n";
        helpContents.append(heading);
        helpContents.append("        " + ChatColor.UNDERLINE + "Gameplay" + ChatColor.RESET + "\n");
        helpContents.append("The main objective of the game is to make sure your team stays alive the longest. ");
        helpContents.append("Players are given 3 lives to start with plus an extra life if you are a member and/or if you have the Extra Life perk. ");
        helpContents.append("// TODO");
        helpContents.append("<pagebreak>");
        helpContents.append(heading);
        helpContents.append("          " + ChatColor.UNDERLINE + "Perks" + ChatColor.RESET + "\n");
        helpContents.append("Perks can be accessed by right clicking with the perk menu item (Nether Star) while you are in the lobby. ");
        helpContents.append("From there you can enable/disable perks that you have purchased from the online shop.");

        book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setDisplayName(ChatColor.GOLD + "Help");
        bookMeta.addPage(helpContents.toString().split("<pagebreak>"));
        book.setItemMeta(bookMeta);

        serverMenu = Bukkit.createInventory(null, 9, "      -=-=- Servers -=-=-");
        hub = new ItemStack(Material.EYE_OF_ENDER, 1);
        meta = hub.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_GREEN + "Hub");
        meta.setLore(ImmutableList.of("Click to join server"));
        hub.setItemMeta(meta);
        serverMenu.setItem(4, hub);
    }

    public static void apply(Player player) {
        PlayerInventory inv = player.getInventory();

        inv.setItem(0, Menu.flame.clone());
        inv.setItem(3, Menu.star.clone());
        inv.setItem(4, Menu.eye.clone());
        inv.setItem(5, Menu.book.clone());
        inv.setItem(8, Menu.flame.clone());
        inv.setHeldItemSlot(4);
    }

    public static void handleHold(PlayerItemHeldEvent event) {
        if (event.getNewSlot() < START) event.getPlayer().getInventory().setHeldItemSlot(FINISH);
        if (event.getNewSlot() > FINISH) event.getPlayer().getInventory().setHeldItemSlot(START);
    }

    public static void handleClick(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();

        SlotType type = event.getSlotType();
        if (type != null) {
            if (type == SlotType.ARMOR || type == SlotType.OUTSIDE) {
                return;
            }
        }

        if (item.isSimilar(flame)) {
            player.closeInventory();
            event.setResult(Result.DENY);
        } else if (item.isSimilar(star)) {
            player.closeInventory();
            player.openInventory(PerkManager.getPerkMenu(player));
            event.setResult(Result.DENY);
        } else if (item.isSimilar(eye)) {
            player.closeInventory();
            player.openInventory(serverMenu);
        } else if (item.isSimilar(book)) {
            player.closeInventory();
        }

        Inventory inv = event.getInventory();
        if (inv != null && type == SlotType.CONTAINER) {
            if (inv.getTitle().equals(PerkManager.TITLE)) {
                PerkManager.toggleActive(item, player);
            } else if (inv.getTitle().equals(serverMenu.getTitle())) {
                if (item.isSimilar(hub)) {
                    Util.sendPM(player, "Connect", "hub");
                }
            }
        }
    }

    public static void handleInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;

        Player player = (Player) event.getPlayer();

        if (item.isSimilar(flame)) {
            event.setCancelled(true);
        } else if (item.isSimilar(star)) {
            player.openInventory(PerkManager.getPerkMenu(player));
            event.setCancelled(true);
        } else if (item.isSimilar(eye)) {
            player.openInventory(serverMenu);
            event.setCancelled(true);
        }
    }
}
