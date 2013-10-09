package com.eyeofender.dodgeball.game.perks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.connect.table.Perks;
import com.eyeofender.massapi.chat.Messenger;
import com.eyeofender.massapi.database.MassDatabase;
import com.eyeofender.massapi.database.table.Membership;

public class PerkManager {

    private static final int STATE_LINE = 0;
    private static final int DESCRIPTION_LINE = 1;

    private static final String LOCKED = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Locked";
    private static final String ENABLED = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Enabled";
    private static final String DISABLED = ChatColor.DARK_RED + "" + ChatColor.BOLD + "Disabled";

    private PerkManager() {

    }

    public static void toggleActive(ItemStack stack, Player player) {
        Perk perk = Perk.getByIcon(stack);
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore.get(STATE_LINE).equals(LOCKED)) {
            Messenger.tellPlayer(player, ChatColor.RED + "This perk is locked.  Visit $1 to purchase it.", Messenger.STORE_URL);
            return;
        }

        ActivePerks active = ActivePerks.get(player);
        boolean disabled = !active.isActive(perk);
        lore.set(STATE_LINE, disabled ? ENABLED : DISABLED);
        meta.setLore(lore);
        stack.setItemMeta(meta);

        active.setActive(perk, disabled);
    }

    private static ItemStack getPerkIcon(Player player, Perk perk, int amount) {
        return getPerkIcon(player, perk, amount > 0, amount);
    }

    private static ItemStack getPerkIcon(Player player, Perk perk, boolean unlocked, int amount) {
        ItemStack stack = perk.getMenuIcon();
        ItemMeta meta = stack.getItemMeta();

        List<String> lore = new ArrayList<String>();
        lore.add(STATE_LINE, unlocked ? (ActivePerks.get(player).isActive(perk) ? ENABLED : DISABLED) : LOCKED);
        lore.add(DESCRIPTION_LINE, ChatColor.YELLOW + perk.getDescription());

        if (unlocked) {
            stack.setAmount(amount);
        }

        meta.setLore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    public static Inventory getPerkMenu(Player player) {
        Inventory menu = Dodgeball.instance.getServer().createInventory(null, 9, "Perk Menu");
        Perks perk = Dodgeball.instance.getDatabaseConnection().getPerks(player);
        Membership membership = MassDatabase.getMembership(player);
        int priority = membership != null ? membership.getPriority() : 0;

        menu.addItem(getPerkIcon(player, Perk.STARTING_BALLS, perk.getStartingBalls() + (priority > 2 ? 1 : 0)));
        menu.addItem(getPerkIcon(player, Perk.TRIPPLE_SHOTS, perk.getTripleShots()));
        menu.addItem(getPerkIcon(player, Perk.AIRSTRIKES, perk.getAirstrikes()));
        menu.addItem(getPerkIcon(player, Perk.BALL_BOOST, perk.getBallBoost()));
        menu.addItem(getPerkIcon(player, Perk.LIFE_BOOST, perk.getLifeBoost()));
        menu.addItem(getPerkIcon(player, Perk.EXTRA_LIVES, perk.getExtraLives()));
        menu.addItem(getPerkIcon(player, Perk.LIFE_GAINED_ON_HIT, perk.isLifeGainedOnHit(), 1));
        menu.addItem(getPerkIcon(player, Perk.SPEED_BOOST, perk.isSpeedBoost() || priority > 1, 1));

        return menu;
    }
}
