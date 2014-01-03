package com.eyeofender.dodgeball.perk;

import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.enderpearl.purchases.PlayerPurchase;
import com.google.common.collect.Maps;

public enum Perk {

    STARTING_BALLS(new ItemStack(Material.SNOW_BALL), ChatColor.RESET + "Starting Balls", "Start round holding dodgeballs", true),
    TRIPPLE_SHOTS(new ItemStack(Material.ENDER_PEARL), ChatColor.RESET + "Tripple Shots", "Start round with Triple-shot dodgeballs", true),
    AIRSTRIKES(new ItemStack(Material.ARROW), ChatColor.RESET + "Airstrikes", "Start round with Airstrikes", true),
    LIFE_BOOST(new Potion(PotionType.INSTANT_HEAL).toItemStack(1), ChatColor.GREEN + "Life Boost", "+1 Life", false),
    EXTRA_LIFE(new ItemStack(Material.CHAINMAIL_CHESTPLATE), ChatColor.LIGHT_PURPLE + "Extra Life", "Start round with an extra life", true),
    LIFE_GAINED_ON_HIT(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), ChatColor.LIGHT_PURPLE + "Life Gained on Hit", "Gain lives by hitting enemies", true),
    SPEED_BOOST(new ItemStack(Material.DIAMOND_BOOTS), ChatColor.AQUA + "Speed Boost", "Toggleable Super speed", false);

    private final ItemStack icon;
    private final String name;
    private final String description;
    private final boolean onSale;
    private static final Map<Material, Perk> BY_ICON = Maps.newHashMap();

    private Perk(ItemStack icon, String name, String description, boolean onSale) {
        this.icon = icon;
        this.name = name;
        this.description = ChatColor.YELLOW + description;
        this.onSale = onSale;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemStack getMenuIcon() {
        ItemStack stack = icon.clone();
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    public int getAmount(Player player) {
        List<PlayerPurchase> perks = Dodgeball.getInstance().getApi().getPurchaseManager().getPurchases(player, "Dodgeball", "perk", ChatColor.stripColor(name));
        return perks != null ? perks.size() : 0;
    }

    public boolean isOnSale() {
        return onSale;
    }

    public static Perk getByIcon(ItemStack icon) {
        return BY_ICON.get(icon.getType());
    }

    static {
        for (Perk perk : values()) {
            BY_ICON.put(perk.getIcon().getType(), perk);
        }
    }

}
