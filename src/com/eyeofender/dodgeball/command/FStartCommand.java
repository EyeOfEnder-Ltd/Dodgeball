package com.eyeofender.dodgeball.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.eyeofender.dodgeball.Dodgeball;

public class FStartCommand implements CommandExecutor {

    private Dodgeball plugin;

    public FStartCommand(Dodgeball plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        plugin.getGame().start();
        return true;
    }

}
