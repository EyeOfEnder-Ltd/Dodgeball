package com.eyeofender.dodgeball.game.perks;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import com.google.common.collect.Maps;

public enum Perk {

    STARTING_BALLS(PerkType.DODGEBALL, new ItemStack(Material.SNOW_BALL), ChatColor.RESET + "Starting Balls", "Start round holding dodgeballs"),
    TRIPPLE_SHOTS(PerkType.DODGEBALL, new ItemStack(Material.ENDER_PEARL), ChatColor.RESET + "Tripple Shots", "Start round with Triple-shot dodgeballs"),
    AIRSTRIKES(PerkType.DODGEBALL, new ItemStack(Material.ARROW), ChatColor.RESET + "Airstrikes", "Start round with Airstrikes"),

    BALL_BOOST(PerkType.EXPENDABLE, new ItemStack(Material.SLIME_BALL), ChatColor.GREEN + "Ball Boost", "+1 Dodgeball"),
    LIFE_BOOST(PerkType.EXPENDABLE, new Potion(PotionType.INSTANT_HEAL).toItemStack(1), ChatColor.GREEN + "Life Boost", "+1 Life"),

    EXTRA_LIVES(PerkType.STATIC, new ItemStack(Material.CHAINMAIL_CHESTPLATE), ChatColor.LIGHT_PURPLE + "Extra Lives", "Start round with extra lives"),
    LIFE_GAINED_ON_HIT(PerkType.STATIC, new ItemStack(Material.SKULL_ITEM, 1, (short) 3), ChatColor.LIGHT_PURPLE + "Life Gained on Hit", "Gain lives by hitting enemies"),

    SPEED_BOOST(PerkType.TOGGLE, new ItemStack(Material.DIAMOND_BOOTS), ChatColor.AQUA + "Speed Boost", "Toggleable Super speed");

    private final PerkType type;
    private final ItemStack icon;
    private final String name;
    private final String description;
    private static final Map<Material, Perk> BY_ICON = Maps.newHashMap();

    private Perk(PerkType type, ItemStack icon, String name, String description) {
        this.type = type;
        this.icon = icon;
        this.name = name;
        this.description = ChatColor.YELLOW + description;
    }

    public PerkType getType() {
        return type;
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

    public static Perk getByIcon(ItemStack icon) {
        return BY_ICON.get(icon.getType());
    }

    static {
        for (Perk perk : values()) {
            BY_ICON.put(perk.getIcon().getType(), perk);
        }
    }

}
