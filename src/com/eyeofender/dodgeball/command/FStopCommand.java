package com.eyeofender.dodgeball.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.eyeofender.dodgeball.Dodgeball;

public class FStopCommand implements CommandExecutor {

    private Dodgeball plugin;

    public FStopCommand(Dodgeball plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        plugin.getGame().stop();
        return true;
    }

}
