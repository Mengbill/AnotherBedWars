package org.azurith.anotherbedwars.setup;

import com.infernalsuite.asp.api.exceptions.InvalidWorldException;
import com.infernalsuite.asp.api.exceptions.WorldLoadedException;
import com.infernalsuite.asp.api.exceptions.WorldTooBigException;
import com.infernalsuite.asp.api.world.SlimeWorld;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.azurith.anotherbedwars.exception.WorldImportException;
import org.azurith.anotherbedwars.server.ServerManager;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.logging.Level;

public class SetupManager {

    private final ServerManager serverManager;

    private final SetupCommand setupCommand;
    private final SetupListener setupListener;

    private boolean enabled = false;

    public SetupManager(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.setupCommand = new SetupCommand(this);
        this.setupListener = new SetupListener(this);
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public void enable() {
        if (enabled) return;
        serverManager.getPlugin().getServer().getPluginManager().registerEvents(setupListener, serverManager.getPlugin());
        serverManager.getPlugin().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(setupCommand.createCommand().build());
        });
        enabled = true;
    }

    public void disable() {
        if (!enabled) return;
        HandlerList.unregisterAll(setupListener);
        enabled = false;
    }

    public void importVanillaWorld(String worldName) {
        try {
            SlimeWorld world = serverManager.getPlugin().getAsp().readVanillaWorld(new File(serverManager.getPlugin().getServer().getWorldContainer(), worldName), worldName, null);
            serverManager.getPlugin().getAsp().loadWorld(world, false);
        } catch (InvalidWorldException e) {
            throw new WorldImportException("该世界不存在或格式不正确");
        } catch (WorldLoadedException e) {
            throw new WorldImportException("该世界已经被加载");
        } catch (WorldTooBigException e) {
            throw new WorldImportException("该世界过大");
        } catch (Exception e) {
            serverManager.getPlugin().getLogger().log(Level.SEVERE, "导入临时世界 " + worldName + " 失败 ", e);
            throw new WorldImportException("发生未知错误 请查看控制台", e);
        }
    }

}