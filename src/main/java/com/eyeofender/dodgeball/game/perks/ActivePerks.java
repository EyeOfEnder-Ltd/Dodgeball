package com.eyeofender.dodgeball.game.perks;

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

import com.eyeofender.dodgeball.connect.DatabaseConnection;
import com.eyeofender.dodgeball.connect.table.Perks;
import com.eyeofender.dodgeball.game.Arena;

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

    private FiringMode firingMode;

    public ActivePerks(String name) {
        this.name = name;
        disableAll();
        this.firingMode = FiringMode.STANDARD;
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
        return firingMode;
    }

    public void setFiringMode(FiringMode firingMode) {
        this.firingMode = firingMode;
    }

    public void apply(Arena arena) {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) return;

        Perks perks = DatabaseConnection.getPerks(player);

        if (startingBalls) {
            ItemStack ball = FiringMode.STANDARD.getDodgeball(arena);
            ball.setAmount(perks.getStartingBalls());
            player.getInventory().addItem(ball);
        }

        if (tripleShots) {
            ItemStack ball = FiringMode.TRIPLE_SHOT.getDodgeball(arena);
            ball.setAmount(perks.getTripleShots());
            player.getInventory().addItem(ball);
        }

        if (airstrikes) {
            ItemStack ball = FiringMode.AIRSTRIKE.getDodgeball(arena);
            ball.setAmount(perks.getAirstrikes());
            player.getInventory().addItem(ball);
        }

        if (lifeBoost) applyExpendable(Perk.LIFE_BOOST, perks.getLifeBoost(), player);
        if (ballBoost) applyExpendable(Perk.BALL_BOOST, perks.getBallBoost(), player);

        if (extraLives) {
            double newHealth = player.getHealth() + perks.getExtraLives() * 2.0;
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
