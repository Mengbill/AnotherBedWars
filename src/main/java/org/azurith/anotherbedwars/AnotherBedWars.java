package org.azurith.anotherbedwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.azurith.anotherbedwars.game.GameListener;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public final class AnotherBedWars extends JavaPlugin {

    private static boolean restartNeeded = false;

    @Override
    public void onLoad() {
        getLogger().log(Level.WARNING,"正在为插件运行配置所需环境");
            try {
                File file = new File(Bukkit.getServer().getWorldContainer(), "server.properties");
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).equalsIgnoreCase("allow-nether=true")) {
                        getLogger().log(Level.WARNING,"正在禁用世界 Nether");
                        lines.set(i, "allow-nether=false");
                        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
                        getLogger().log(Level.WARNING,"已修改 server.properties -> allow-nether=false");
                        restartNeeded = true;
                    }
                    if (lines.get(i).startsWith("level-name") && !lines.get(i).equalsIgnoreCase("level-name=AnotherBedWars_Arena")) {
                        getLogger().log(Level.WARNING,"正在配置世界 AnotherBedWars_Arena 为主世界");
                        lines.set(i, "level-name=AnotherBedWars_Arena");
                        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
                        getLogger().log(Level.WARNING,"已修改 server.properties -> level-name=AnotherBedWars_Arena");
                        restartNeeded = true;
                    }
                }
            } catch (Exception e) {
                getLogger().log(Level.SEVERE,"尝试操作 server.properties 时发生错误",e);
                Bukkit.getPluginManager().disablePlugin(this);
            }
            try {
                File bukkitFile = new File(Bukkit.getServer().getWorldContainer(), "bukkit.yml");
                YamlConfiguration bukkitYaml = YamlConfiguration.loadConfiguration(bukkitFile);
                if (bukkitYaml.getBoolean("settings.allow-end")) {
                    getLogger().log(Level.WARNING,"正在禁用世界 End");
                    bukkitYaml.set("settings.allow-end", false);
                    bukkitYaml.save(bukkitFile);
                    getLogger().log(Level.WARNING,"已修改 bukkit.yml -> allow-end=false");
                    restartNeeded = true;
                }
                String generator = bukkitYaml.getString("worlds.AnotherBedWars_Arena.generator");
                if (generator == null || !generator.equals("AnotherBedWars")) {
                    getLogger().log(Level.WARNING, "正在为世界 AnotherBedWars_Arena 配置 Generator");
                    bukkitYaml.set("worlds.AnotherBedWars_Arena.generator", "AnotherBedWars");
                    bukkitYaml.save(bukkitFile);
                    getLogger().log(Level.WARNING,"已修改 bukkit.yml -> worlds.AnotherBedWars_Arena.generator=AnotherBedWars");
                    restartNeeded = true;
                }
            } catch (Exception e) {
                getLogger().log(Level.SEVERE,"尝试操作 bukkit.yml 时发生错误",e);
                Bukkit.getPluginManager().disablePlugin(this);
            }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (restartNeeded) {
            getLogger().log(Level.WARNING,"插件所需运行环境已配置完成 即将重启服务器以应用配置更改");
            Bukkit.getScheduler().runTaskLater(this, Bukkit::restart, 20);
            return;
        }
        Bukkit.getPluginManager().registerEvents(new GameListener(),this);
        getLogger().log(Level.WARNING,"插件所需运行环境已配置完成 正在启动插件");
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
                .append(Component.text("                             Enabled AnotherBedWars v1.0 by Mengbill",NamedTextColor.BLUE)))
                .appendNewline()
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {return new VoidGenerator();}

    public static class VoidGenerator extends ChunkGenerator{
        @Override
        public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull WorldInfo worldInfo) {
            return new BiomeProvider() {
                @Override
                public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
                    return Biome.THE_VOID;
                }

                @Override
                public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
                    return Collections.singletonList(Biome.THE_VOID);
                }
            };
        }
    }

}
