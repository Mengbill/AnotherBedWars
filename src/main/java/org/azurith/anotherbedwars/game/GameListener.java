package org.azurith.anotherbedwars.game;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class GameListener implements Listener {
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPortal(PlayerPortalEvent event){
        World target = event.getTo().getWorld();
        if (target != null && (target.getEnvironment() == World.Environment.THE_END || target.getEnvironment() == World.Environment.NETHER)){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        World target = event.getTo().getWorld();
        if (target != null && (target.getEnvironment() == World.Environment.THE_END || target.getEnvironment() == World.Environment.NETHER)){
            event.setCancelled(true);
        }
    }

}
