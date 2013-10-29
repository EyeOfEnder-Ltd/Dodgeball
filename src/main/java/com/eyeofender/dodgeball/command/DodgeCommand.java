package com.eyeofender.dodgeball.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

import com.eyeofender.dodgeball.Dodgeball;
import com.google.common.collect.ImmutableList;

public class DodgeCommand extends DodgeballExecutor {

    private static final List<String> PRIMARY_ARGS = ImmutableList.of("help", "reload", "version");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        PluginDescriptionFile pdf = Dodgeball.instance.getDescription();

        if (args.length == 0 || args[0].equalsIgnoreCase("version")) {
            String authors = pdf.getAuthors().toString().replace("[", "").replace("]", "");
            return sendMsg(sender, "Running version " + ChatColor.LIGHT_PURPLE + pdf.getVersion() + ChatColor.GRAY + " by " + ChatColor.BLUE + authors);
        } else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            StringBuffer sb = new StringBuffer(getPageHeader("Arena Commands"));
            Map<String, Map<String, Object>> commands = Dodgeball.instance.getDescription().getCommands();

            for (int i = 0; i < commands.size(); i++) {
                String usage = (String) commands.get(i).get("usage");
                if (usage != null) sb.append("\n" + (i % 2 == 0 ? ChatColor.AQUA : ChatColor.DARK_AQUA) + usage);
            }
            sender.sendMessage(sb.toString());
            return true;
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!isAllowed(sender, false, "dodgeball.reload")) return true;
            Dodgeball.instance.reloadConfig();
            return sendMsg(sender, ChatColor.GREEN + "Reloaded the config.");
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], PRIMARY_ARGS, new ArrayList<String>());
        }

        return ImmutableList.of();
    }

}
