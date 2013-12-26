package com.eyeofender.dodgeball;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public enum DodgeTeam {

    ORANGE("Orange", ChatColor.GOLD, Color.ORANGE),
    MAGENTA("Magenta", ChatColor.DARK_PURPLE, Color.MAROON),
    AQUA("Aqua", ChatColor.AQUA, Color.AQUA),
    YELLOW("Yellow", ChatColor.YELLOW, Color.YELLOW),
    GREEN("Green", ChatColor.GREEN, Color.LIME),
    PINK("Pink", ChatColor.LIGHT_PURPLE, Color.PURPLE),
    DARK_GRAY("Dark Gray", ChatColor.DARK_GRAY, Color.GRAY),
    GRAY("Gray", ChatColor.GRAY, Color.SILVER),
    CYAN("Cyan", ChatColor.DARK_AQUA, Color.AQUA),
    BLUE("Blue", ChatColor.BLUE, Color.BLUE),
    DARK_GREEN("Dark Green", ChatColor.DARK_GREEN, Color.GREEN),
    RED("Red", ChatColor.RED, Color.RED),
    BLACK("Black", ChatColor.BLACK, Color.BLACK);

    private String displayName;
    private ChatColor chatColour;
    private Color colour;

    private DodgeTeam(String displayName, ChatColor chatColour, Color colour) {
        this.displayName = displayName;
        this.chatColour = chatColour;
        this.colour = colour;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getChatColour() {
        return chatColour;
    }

    public void equip(Player player) {
        ItemStack armour = new ItemStack(Material.LEATHER_HELMET, 1);
        LeatherArmorMeta meta = (LeatherArmorMeta) armour.getItemMeta();
        meta.setColor(colour);
        armour.setItemMeta(meta);
        player.getInventory().setHelmet(armour);

        armour = new ItemStack(Material.LEATHER_CHESTPLATE, 1);
        meta = (LeatherArmorMeta) armour.getItemMeta();
        meta.setColor(colour);
        armour.setItemMeta(meta);
        player.getInventory().setChestplate(armour);

        armour = new ItemStack(Material.LEATHER_LEGGINGS, 1);
        meta = (LeatherArmorMeta) armour.getItemMeta();
        meta.setColor(colour);
        armour.setItemMeta(meta);
        player.getInventory().setLeggings(armour);

        armour = new ItemStack(Material.LEATHER_BOOTS, 1);
        meta = (LeatherArmorMeta) armour.getItemMeta();
        meta.setColor(colour);
        armour.setItemMeta(meta);
        player.getInventory().setBoots(armour);
    }

    public static DodgeTeam fromString(String team) {
        for (DodgeTeam t : values())
            if (t.toString().equalsIgnoreCase(team)) return t;
        return null;
    }

}
