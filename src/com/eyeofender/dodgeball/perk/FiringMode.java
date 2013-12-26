package com.eyeofender.dodgeball.perk;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Maps;

public enum FiringMode {

    /**
     * A single ball is thrown as in standard firing mode, and a 7x7 area around
     * where it lands is carpeted with dodgeballs. The airstrike balls deal
     * damage as usual, but are removed upon impact.
     */
    AIRSTRIKE(ChatColor.RED + "Airstrike Launcher"),

    /**
     * When a player throws a dodgeball, two extra balls are launched on either
     * side of it, at slightly different horizontal angles. The balls deal
     * damage as usual, but only the center ball remains after impact.
     */
    TRIPLE_SHOT(ChatColor.AQUA + "Triple Shot"),

    /**
     * Default behavior: a single ball is launched and handled with vanilla
     * Dodgeball mechanics
     */
    STANDARD("Dodgeball");

    /**
     * The displayname to apply to the ItemMeta of dodgeballs held by players
     */
    private final String name;

    private static final Map<String, FiringMode> BY_NAME = Maps.newHashMap();
    private static final ItemStack DODGEBALL = new ItemStack(Material.SNOW_BALL, 1);

    private FiringMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ItemStack getDodgeball() {
        ItemStack ball = DODGEBALL.clone();
        ItemMeta meta = ball.getItemMeta();
        meta.setDisplayName(getName());
        ball.setItemMeta(meta);
        return ball;
    }

    public static FiringMode getByName(String name) {
        return BY_NAME.get(name);
    }

    public static FiringMode getByItem(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return null;
        return getByName(item.getItemMeta().getDisplayName());
    }

    static {
        for (FiringMode mode : values()) {
            BY_NAME.put(mode.getName(), mode);
        }
    }
}
