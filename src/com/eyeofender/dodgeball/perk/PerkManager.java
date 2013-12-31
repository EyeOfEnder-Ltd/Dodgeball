package com.eyeofender.dodgeball.perk;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.ImmutableList;

public class PerkManager {

    public static final String TITLE = "        -=-=- Perks -=-=-";

    private static final int STATE_LINE = 0;
    private static final int DESCRIPTION_LINE = 1;

    private static final String FUTURE = ChatColor.YELLOW + "???";
    private static final String LOCKED = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Locked";
    private static final String ENABLED = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Enabled";
    private static final String DISABLED = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Disabled";

    private static ItemStack item;
    private static final ItemStack PLACEHOLDER;

    static {
        item = new ItemStack(Material.NETHER_STAR, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Perks");
        meta.setLore(ImmutableList.of("Click to open perk menu"));
        item.setItemMeta(meta);

        PLACEHOLDER = new ItemStack(Material.IRON_BARDING, 1);
        meta = PLACEHOLDER.getItemMeta();
        meta.setDisplayName(ChatColor.MAGIC + "Future");
        meta.setLore(ImmutableList.of(LOCKED, FUTURE));
        PLACEHOLDER.setItemMeta(meta);
    }

    private PerkManager() {
    }

    public static void toggleActive(ItemStack stack, Player player) {
        Perk perk = Perk.getByIcon(stack);
        if (perk == null) return;
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore.get(STATE_LINE).equals(LOCKED)) {
            player.sendMessage(ChatColor.RED + "This perk is locked.  Visit http://eyeofender.com/shop to purchase it.");
            return;
        }

        ActivePerks active = ActivePerks.get(player);
        boolean disabled = !active.isActive(perk);
        lore.set(STATE_LINE, disabled ? ENABLED : DISABLED);
        meta.setLore(lore);
        stack.setItemMeta(meta);

        active.setActive(perk, disabled);
    }

    private static ItemStack getPerkIcon(Player player, Perk perk) {
        ItemStack stack = perk.getMenuIcon();
        ItemMeta meta = stack.getItemMeta();
        int amount = perk.getAmount(player);

        List<String> lore = new ArrayList<String>();
        lore.add(STATE_LINE, amount > 0 ? (ActivePerks.get(player).isActive(perk) ? ENABLED : DISABLED) : LOCKED);
        lore.add(DESCRIPTION_LINE, ChatColor.YELLOW + perk.getDescription());

        if (amount > 0) stack.setAmount(amount);

        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack getItem() {
        return item.clone();
    }

    public static Inventory getPerkMenu(Player player) {
        Perk[] perks = Perk.values();

        int slots = perks.length * 2 - 1;
        int spaces = (int) (Math.ceil(slots / 9.0) * 9.0);
        Inventory menu = Bukkit.createInventory(null, spaces, TITLE);

        for (int i = 0; i < spaces; i++) {
            if (i % 2 == 0) {
                int index = i / 2;

                if (index >= perks.length) {
                    menu.setItem(i, PLACEHOLDER.clone());
                }

                menu.setItem(i, getPerkIcon(player, perks[index]));
            }
        }

        return menu;
    }
}
