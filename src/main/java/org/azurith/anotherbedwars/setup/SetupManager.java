package org.azurith.anotherbedwars.setup;

import com.infernalsuite.asp.api.exceptions.*;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.azurith.anotherbedwars.AnotherBedWars;
import org.azurith.anotherbedwars.server.ServerManager;
import org.azurith.anotherbedwars.setup.task.SetupActionBarTask;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.function.Consumer;
import java.util.logging.Level;

public class SetupManager {

    private final ServerManager serverManager;
    private final AnotherBedWars plugin;

    private final SetupCommand setupCommand;
    private final SetupListener setupListener;

    private final SetupActionBarTask setupActionBarTask;

    private boolean enabled = false;

    public SetupManager(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.plugin = serverManager.getPlugin();
        this.setupCommand = new SetupCommand(this);
        this.setupListener = new SetupListener(this);
        this.setupActionBarTask = new SetupActionBarTask(this);
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public void enable() {
        if (enabled) return;
        plugin.getServer().getPluginManager().registerEvents(setupListener, plugin);
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> commands.registrar().register(setupCommand.createCommand().build()));
        setupActionBarTask.start();
        enabled = true;
    }

    public void disable() {
        if (!enabled) return;
        setupActionBarTask.stop();
        HandlerList.unregisterAll(setupListener);
        enabled = false;
    }

    public boolean convertVanillaWorld(String worldFolder, Consumer<String> errorHandler) {
        try {
            File worldDir = new File(plugin.getServer().getWorldContainer(), worldFolder);
            SlimeWorld world = plugin.getAsp().readVanillaWorld(worldDir, worldFolder, plugin.getSlimeLoader());
            plugin.getAsp().saveWorld(world);
            return true;
        } catch (InvalidWorldException e) {
            errorHandler.accept("世界不存在或格式不支持");
        } catch (WorldLoadedException e) {
            errorHandler.accept("存在已被加载的同名世界");
        } catch (WorldTooBigException e) {
            errorHandler.accept("世界过大");
        } catch (WorldAlreadyExistsException e) {
            errorHandler.accept("存在已被转换的同名世界");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "转换临时世界 " + worldFolder + " 失败 ", e);
            errorHandler.accept("发生未知错误 请查看控制台");
        }
        return false;
    }

    public boolean teleportSlimeWorld(Player player, String worldName) {
        SlimeWorldInstance slimeWorldInstance = plugin.getAsp().getLoadedWorld(worldName);
        if (slimeWorldInstance == null) return false;
        player.teleport(slimeWorldInstance.getBukkitWorld().getSpawnLocation());
        return true;
    }

    public boolean loadSlimeWorld(String worldName, Consumer<String> errorHandler) {
        try {
            SlimeWorld world = plugin.getAsp().readWorld(plugin.getSlimeLoader(), worldName, true, new SlimePropertyMap());
            plugin.getAsp().loadWorld(world, false);
            return true;
        } catch (UnknownWorldException e) {
            errorHandler.accept("世界不存在");
        } catch (CorruptedWorldException e) {
            errorHandler.accept("世界格式不正确");
        } catch (NewerFormatException e) {
            errorHandler.accept("世界格式不兼容");
        } catch (IllegalArgumentException e) {
            errorHandler.accept("世界已被加载");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "加载临时世界 " + worldName + " 失败 ", e);
            errorHandler.accept("发生未知错误 请查看控制台");
        }
        return false;
    }

    public boolean unloadSlimeWorld(String worldName) {
        SlimeWorldInstance slimeWorldInstance = plugin.getAsp().getLoadedWorld(worldName);
        if (slimeWorldInstance == null) return false;
        World world = slimeWorldInstance.getBukkitWorld();
        world.getPlayers().forEach(player -> player.teleport(plugin.getServer().getWorlds().getFirst().getSpawnLocation()));
        return plugin.getServer().unloadWorld(world, false);
    }

}