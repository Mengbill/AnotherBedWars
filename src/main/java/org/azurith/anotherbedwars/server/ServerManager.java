package org.azurith.anotherbedwars.server;

import org.azurith.anotherbedwars.AnotherBedWars;
import org.azurith.anotherbedwars.setup.SetupManager;
import org.bukkit.event.HandlerList;

public class ServerManager {

    private boolean enabled = false;
    private final ServerMode serverMode;

    private final AnotherBedWars plugin;

    private final SetupManager setupManager;

    private final ServerGlobalListener serverGlobalListener;


    public ServerManager(AnotherBedWars plugin) {
        this.plugin = plugin;
        this.setupManager = new SetupManager(this);
        this.serverMode = plugin.getConfigManager().getPluginConfig().server.server_mode;
        this.serverGlobalListener = new ServerGlobalListener();
    }

    public void enable() {
        if (enabled) return;
        if (serverMode == ServerMode.SETUP) {
            setupManager.enable();
        }
        plugin.getServer().getPluginManager().registerEvents(serverGlobalListener, plugin);
        enabled = true;
    }

    public void disable() {
        if (!enabled) return;
        if (serverMode == ServerMode.SETUP) {
            setupManager.disable();
        }
        HandlerList.unregisterAll(serverGlobalListener);
        enabled = false;
    }

    public ServerMode getServerMode() {
        return serverMode;
    }

    public AnotherBedWars getPlugin() {
        return plugin;
    }
}
