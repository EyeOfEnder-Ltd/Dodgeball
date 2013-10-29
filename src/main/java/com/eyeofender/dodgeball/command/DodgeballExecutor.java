package com.eyeofender.dodgeball.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.game.Arena;
import com.google.common.collect.ImmutableList;

public abstract class DodgeballExecutor implements CommandExecutor, TabCompleter {

    private static final String PAGE_PREFIX = ChatColor.GRAY + "[]" + ChatColor.RED + "===" + ChatColor.GRAY + "[";
    private static final String PAGE_SUFFIX = ChatColor.GRAY + "]" + ChatColor.RED + "===" + ChatColor.GRAY + "[]";

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        return ImmutableList.of();
    }

    protected boolean isAllowed(CommandSender sender, boolean mustBePlayer) {
        return isAllowed(sender, mustBePlayer, null);
    }

    protected boolean isAllowed(CommandSender sender, boolean mustBePlayer, String permission) {
        if (mustBePlayer && !(sender instanceof Player)) {
            sender.sendMessage(Dodgeball.prefix + ChatColor.RED + "Only players can run that command.");
            return false;
        }
        if (permission != null && sender instanceof Player && !sender.hasPermission(permission)) {
            sender.sendMessage(Dodgeball.prefix + ChatColor.RED + "Access denied!");
            return false;
        }
        return true;
    }

    protected String getPageHeader(String title) {
        return PAGE_PREFIX + ChatColor.DARK_RED + title + PAGE_SUFFIX;
    }

    protected boolean sendMsg(CommandSender sender, String msg) {
        sender.sendMessage(Dodgeball.prefix + msg);
        return true;
    }

    protected List<String> getCurrentArenas() {
        List<String> list = new ArrayList<String>();
        for (Arena arena : Dodgeball.instance.getGameManager().getArenas()) {
            list.add(arena.getName());
        }
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list;
    }

}
