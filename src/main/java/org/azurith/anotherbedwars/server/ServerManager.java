package org.azurith.anotherbedwars.server;

import org.azurith.anotherbedwars.AnotherBedWars;
import org.azurith.anotherbedwars.setup.SetupManager;

public class ServerManager {

    private final ServerMode serverMode;

    private final AnotherBedWars plugin;

    private final SetupManager setupManager;

    public ServerManager(AnotherBedWars plugin) {
        this.plugin = plugin;
        this.setupManager = new SetupManager(this);
        this.serverMode = plugin.getConfigManager().getPluginConfig().server.server_mode;
    }

    public void enable(){
        if (serverMode == ServerMode.SETUP){
            setupManager.enable();
        }
        plugin.getServer().getPluginManager().registerEvents(new ServerGlobalListener(), plugin);
    }

    public AnotherBedWars getPlugin() {
        return plugin;
    }
}
