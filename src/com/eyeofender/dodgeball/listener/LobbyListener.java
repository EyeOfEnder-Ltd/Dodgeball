package com.eyeofender.dodgeball.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

import com.eyeofender.dodgeball.Dodgeball;
import com.eyeofender.dodgeball.Game;
import com.eyeofender.dodgeball.Game.State;
import com.eyeofender.dodgeball.util.Menu;

public class LobbyListener implements Listener {

    private Dodgeball plugin;

    public LobbyListener(Dodgeball plugin) {
        this.plugin = plugin;
    }

    private boolean isInLobby(Player player) {
        Game game = plugin.getGame();
        return game.contains(player) && (game.getState() == State.WAITING || game.getState() == State.STARTING);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isInLobby(event.getPlayer())) return;
        Menu.handleInteract(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        if (!isInLobby(event.getPlayer())) return;
        Menu.handleHold(event);
    }
}
