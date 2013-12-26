package com.eyeofender.dodgeball.util;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import com.eyeofender.dodgeball.perk.PerkManager;

public class Menu {

    private static final int START = 1;
    private static final int FINISH = 7;

    public static ItemStack flame;
    public static ItemStack star;
    public static ItemStack eye;
    public static ItemStack book;

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
        ArrayList<String> Lore = new ArrayList<String>();
        Lore.add("Click to pick server");
        meta.setLore(Lore);
        eye.setItemMeta(meta);

        book = new ItemStack(Material.WRITTEN_BOOK, 1);
        meta = book.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Help");
        book.setItemMeta(meta);
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
                event.setCancelled(true);
                return;
            }
        }

        if (item.isSimilar(flame)) {
            player.closeInventory();
            event.setCancelled(true);
        } else if (item.isSimilar(star)) {
            player.closeInventory();
            player.openInventory(PerkManager.getPerkMenu(player));
            event.setCancelled(true);
        } else if (item.isSimilar(eye)) {
            player.closeInventory();
            event.setCancelled(true);
        } else if (item.isSimilar(book)) {
            player.closeInventory();
            event.setCancelled(true);
        }

        Inventory inv = event.getInventory();
        if (inv != null && type == SlotType.CONTAINER) {
            if (inv.getTitle().equals(PerkManager.TITLE)) {
                event.setCancelled(true);
                PerkManager.toggleActive(item, player);
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
            event.setCancelled(true);
        }
    }
}
