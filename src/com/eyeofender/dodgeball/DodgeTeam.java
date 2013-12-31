package com.eyeofender.dodgeball;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import com.google.common.collect.Maps;

public class DodgeTeam {

    private static Map<String, DodgeTeam> BY_NAME = Maps.newHashMap();

    static {
        register(new DodgeTeam("Orange", ChatColor.GOLD, Color.ORANGE));
        register(new DodgeTeam("Magenta", ChatColor.DARK_PURPLE, Color.MAROON));
        register(new DodgeTeam("Aqua", ChatColor.AQUA, Color.AQUA));
        register(new DodgeTeam("Yellow", ChatColor.YELLOW, Color.YELLOW));
        register(new DodgeTeam("Green", ChatColor.GREEN, Color.LIME));
        register(new DodgeTeam("Pink", ChatColor.LIGHT_PURPLE, Color.PURPLE));
        register(new DodgeTeam("Dark Gray", ChatColor.DARK_GRAY, Color.GRAY));
        register(new DodgeTeam("Gray", ChatColor.GRAY, Color.SILVER));
        register(new DodgeTeam("Cyan", ChatColor.DARK_AQUA, Color.AQUA));
        register(new DodgeTeam("Blue", ChatColor.BLUE, Color.BLUE));
        register(new DodgeTeam("Dark Green", ChatColor.DARK_GREEN, Color.GREEN));
        register(new DodgeTeam("Red", ChatColor.RED, Color.RED));
        register(new DodgeTeam("Black", ChatColor.BLACK, Color.BLACK));
    }

    private String displayName;
    private ChatColor chatColour;
    private Color colour;
    private int players;

    private DodgeTeam(String displayName, ChatColor chatColour, Color colour) {
        this.displayName = displayName;
        this.chatColour = chatColour;
        this.colour = colour;
        this.players = 0;

        BY_NAME.put(toString(), this);
    }

    @Override
    public String toString() {
        return displayName.toLowerCase().replace(' ', '-');
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getChatColour() {
        return chatColour;
    }

    public int getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {
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

        players++;
    }

    public void removePlayer(Player player) {
        players--;
    }

    public void reset() {
        players = 0;
    }

    private static void register(DodgeTeam team) {
        BY_NAME.put(team.toString(), team);
    }

    public static DodgeTeam fromString(String team) {
        return BY_NAME.get(team.toLowerCase());
    }

    public static List<DodgeTeam> getTeams() {
        return new ArrayList<DodgeTeam>(BY_NAME.values());
    }

}
