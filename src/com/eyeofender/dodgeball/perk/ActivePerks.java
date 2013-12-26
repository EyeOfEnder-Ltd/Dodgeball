package com.eyeofender.dodgeball.perk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.eyeofender.dodgeball.Arena;

public class ActivePerks {

    private static Map<String, ActivePerks> activePerks = new HashMap<String, ActivePerks>();

    private String name;

    private boolean startingBalls;
    private boolean tripleShots;
    private boolean airstrikes;
    private boolean lifeBoost;
    private boolean ballBoost;
    private boolean extraLives;
    private boolean lifeGainedOnHit;
    private boolean speedBoost;

    public ActivePerks(String name) {
        this.name = name;
        disableAll();
        activePerks.put(name, this);
    }

    public static ActivePerks get(Player player) {
        ActivePerks perks = activePerks.get(player.getName());
        return perks != null ? perks : new ActivePerks(player.getName());
    }

    public boolean isActive(Perk perk) {
        switch (perk) {
            case AIRSTRIKES:
                return airstrikes;
            case BALL_BOOST:
                return ballBoost;
            case EXTRA_LIVES:
                return extraLives;
            case LIFE_BOOST:
                return lifeBoost;
            case LIFE_GAINED_ON_HIT:
                return lifeGainedOnHit;
            case SPEED_BOOST:
                return speedBoost;
            case STARTING_BALLS:
                return startingBalls;
            case TRIPPLE_SHOTS:
                return tripleShots;
            default:
                return false;
        }
    }

    public void setActive(Perk perk, boolean active) {
        switch (perk) {
            case AIRSTRIKES:
                this.airstrikes = active;
                break;
            case BALL_BOOST:
                this.ballBoost = active;
                break;
            case EXTRA_LIVES:
                this.extraLives = active;
                break;
            case LIFE_BOOST:
                this.lifeBoost = active;
                break;
            case LIFE_GAINED_ON_HIT:
                this.lifeGainedOnHit = active;
                break;
            case SPEED_BOOST:
                this.speedBoost = active;
                break;
            case STARTING_BALLS:
                this.startingBalls = active;
                break;
            case TRIPPLE_SHOTS:
                this.tripleShots = active;
                break;
            default:
                break;
        }
    }

    public FiringMode getFiringMode() {
        return FiringMode.getByItem(Bukkit.getPlayerExact(name).getItemInHand());
    }

    public void apply(Arena arena) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) return;

        if (startingBalls) {
            ItemStack ball = FiringMode.STANDARD.getDodgeball();
            ball.setAmount(Perk.STARTING_BALLS.getAmount(player));
            player.getInventory().addItem(ball);
        }

        if (tripleShots) {
            ItemStack ball = FiringMode.TRIPLE_SHOT.getDodgeball();
            ball.setAmount(Perk.TRIPPLE_SHOTS.getAmount(player));
            player.getInventory().addItem(ball);
        }

        if (airstrikes) {
            ItemStack ball = FiringMode.AIRSTRIKE.getDodgeball();
            ball.setAmount(Perk.AIRSTRIKES.getAmount(player));
            player.getInventory().addItem(ball);
        }

        if (lifeBoost) applyExpendable(Perk.LIFE_BOOST, Perk.LIFE_BOOST.getAmount(player), player);
        if (ballBoost) applyExpendable(Perk.BALL_BOOST, Perk.BALL_BOOST.getAmount(player), player);

        if (extraLives) {
            double newHealth = player.getHealth() + Perk.EXTRA_LIVES.getAmount(player) * 2.0;
            if (newHealth > player.getMaxHealth()) player.setMaxHealth(newHealth);
            player.setHealth(newHealth);
        }

        if (speedBoost) {
            ItemStack speedMode = new ItemStack(Material.DIAMOND_BOOTS, 1);
            ItemMeta meta = speedMode.getItemMeta();
            meta.setDisplayName("Super Speed: " + ChatColor.DARK_RED + "OFF");
            List<String> lore = new ArrayList<String>();
            lore.add("Right-click to toggle");
            meta.setLore(lore);
            speedMode.setItemMeta(meta);
            player.getInventory().addItem(speedMode);
        }
    }

    private static void applyExpendable(Perk perk, int amount, Player player) {
        ItemStack stack = perk.getMenuIcon();
        stack.setAmount(amount);
        player.getInventory().addItem(stack);
    }

    public void disableAll() {
        this.lifeBoost = false;
        this.ballBoost = false;
        this.airstrikes = false;
        this.tripleShots = false;
        this.startingBalls = false;
        this.extraLives = false;
        this.lifeGainedOnHit = false;
        this.speedBoost = false;
    }

}
