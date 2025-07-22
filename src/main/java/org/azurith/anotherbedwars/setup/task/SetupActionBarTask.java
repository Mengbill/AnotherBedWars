package org.azurith.anotherbedwars.setup.task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.azurith.anotherbedwars.AnotherBedWars;
import org.azurith.anotherbedwars.setup.SetupManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SetupActionBarTask {

    private static final Component ACTION_BAR_MESSAGE = Component.text("服务器当前处于 SETUP 模式", NamedTextColor.RED);
    private static final long DELAY_TICKS = 0L;
    private static final long PERIOD_TICKS = 50L;

    private final AnotherBedWars plugin;
    private BukkitTask task;

    public SetupActionBarTask(SetupManager setupManager) {
        this.plugin = setupManager.getServerManager().getPlugin();
    }

    public synchronized void start() {
        if (task != null) return;
        task = new BukkitRunnable(){
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(player -> player.sendActionBar(ACTION_BAR_MESSAGE));
            }
        }.runTaskTimer(plugin, DELAY_TICKS, PERIOD_TICKS);
    }

    public synchronized void stop() {
        if (task == null) return;
        task.cancel();
        task = null;
    }

}
