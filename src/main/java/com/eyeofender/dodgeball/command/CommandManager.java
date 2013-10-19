package com.eyeofender.dodgeball.command;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Maps;

public class CommandManager {

    private static Map<String, DodgeballExecutor> commands = Maps.newHashMap();

    static {
        commands.put("arenas", new ArenasCommand());
        commands.put("dodge", new DodgeCommand());
        commands.put("spawn", new SpawnCommand());
    }

    private CommandManager() {

    }

    public static void registerCommands(JavaPlugin plugin) {
        for (Entry<String, DodgeballExecutor> entry : commands.entrySet()) {
            PluginCommand command = plugin.getCommand(entry.getKey());
            command.setExecutor(entry.getValue());
            command.setTabCompleter(entry.getValue());
        }
    }
}
