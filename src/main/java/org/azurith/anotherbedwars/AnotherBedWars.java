package org.azurith.anotherbedwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.azurith.anotherbedwars.game.GameListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;

public final class AnotherBedWars extends JavaPlugin {

    private static boolean restartNeeded = false;

    @Override
    public void onLoad() {
        if (Bukkit.getAllowNether()) {
            getLogger().log(Level.WARNING,"检测到服务器已启用 Nether 正在禁用 Nether 并重启服务器");
            try {
                File file = new File(Bukkit.getServer().getWorldContainer(), "server.properties");
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).startsWith("allow-nether")) {
                        lines.set(i, "allow-nether=false");
                    }
                }
                Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
                restartNeeded = true;
                getLogger().log(Level.WARNING,"已修改 server.properties -> allow-nether=false 服务器即将重启");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE,"尝试读取 server.properties 时发生错误",e);
            }
        }
        if (Bukkit.getAllowEnd()) {
            getLogger().log(Level.WARNING,"检测到服务器已启用 End 正在禁用 End 并重启服务器");
            try {
                File file = new File(Bukkit.getServer().getWorldContainer(), "bukkit.yml");
                YamlConfiguration bukkitYaml = YamlConfiguration.loadConfiguration(file);
                bukkitYaml.set("settings.allow-end", false);
                bukkitYaml.save(file);
                restartNeeded = true;
                getLogger().log(Level.WARNING,"已修改 bukkit.yml -> allow-end=false 服务器即将重启");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE,"尝试读取 bukkit.yml 时发生错误",e);
            }
        }

    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (restartNeeded) {Bukkit.getScheduler().runTaskLater(this, Bukkit::restart, 20);}
        Bukkit.getPluginManager().registerEvents(new GameListener(),this);

        Bukkit.getConsoleSender().sendMessage(Component.newline()
                .appendNewline()
                .append(Component.text(
                        """
                                 █████╗ ███╗   ██╗ ██████╗ ████████╗██╗  ██╗███████╗██████╗   ██████╗ ███████╗██████╗ ██╗    ██╗ █████╗ ██████╗ ███████╗
                                ██╔══██╗████╗  ██║██╔═══██╗╚══██╔══╝██║  ██║██╔════╝██╔══██╗  ██╔══██╗██╔════╝██╔══██╗██║    ██║██╔══██╗██╔══██╗██╔════╝
                                ███████║██╔██╗ ██║██║   ██║   ██║   ███████║█████╗  ██████╔╝  ██████╔╝█████╗  ██║  ██║██║ █╗ ██║███████║██████╔╝███████╗
                                ██╔══██║██║╚██╗██║██║   ██║   ██║   ██╔══██║██╔══╝  ██╔══██╗  ██╔══██╗██╔══╝  ██║  ██║██║███╗██║██╔══██║██╔══██╗╚════██║
                                ██║  ██║██║ ╚████║╚██████╔╝   ██║   ██║  ██║███████╗██║  ██║  ██████╔╝███████╗██████╔╝╚███╔███╔╝██║  ██║██║  ██║███████║
                                ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝    ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝  ╚═════╝ ╚══════╝╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝
                                """,NamedTextColor.GREEN)
                .appendNewline()
                .append(Component.text("                             AnotherBedWars v1.0 by Mengbill",NamedTextColor.BLUE)))
                .appendNewline()
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getScheduler().cancelTasks(this);
    }
}
