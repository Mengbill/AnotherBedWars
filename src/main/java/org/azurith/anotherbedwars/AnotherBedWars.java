package org.azurith.anotherbedwars;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.loaders.file.FileLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.io.FileUtils;
import org.azurith.anotherbedwars.server.ServerManager;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public final class AnotherBedWars extends JavaPlugin {

    private static boolean restartNeeded = false;

    private AdvancedSlimePaperAPI asp;
    private SlimeLoader slimeLoader;

    private ServerManager serverManager;

    public AdvancedSlimePaperAPI getAsp() {
        return asp;
    }

    public SlimeLoader getSlimeLoader() {
        return slimeLoader;
    }

    @Override
    public void onLoad() {
        getLogger().log(Level.WARNING, "正在为插件运行配置所需环境");
        //修改 server.properties 以适配插件运行
        try {
            File file = new File(Bukkit.getServer().getWorldContainer(), "server.properties");
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            boolean modified = false;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).equalsIgnoreCase("allow-nether=true")) {
                    getLogger().log(Level.WARNING, "正在禁用世界 Nether");
                    lines.set(i, "allow-nether=false");
                    modified = true;
                }
                if (lines.get(i).startsWith("level-name") && !lines.get(i).equalsIgnoreCase("level-name=world_the_void")) {
                    getLogger().log(Level.WARNING, "正在配置世界 world_the_void 为主世界");
                    lines.set(i, "level-name=world_the_void");
                    modified = true;
                }
            }
            if (modified) {
                Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
                getLogger().log(Level.WARNING, "已修改 server.properties 配置");
                restartNeeded = true;
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "尝试操作 server.properties 时发生错误", e);
            getServer().getPluginManager().disablePlugin(this);
        }
        //修改 bukkit.yml 以适配插件运行
        try {
            File bukkitFile = new File(Bukkit.getServer().getWorldContainer(), "bukkit.yml");
            YamlConfiguration bukkitYaml = YamlConfiguration.loadConfiguration(bukkitFile);
            boolean modified = false;
            if (bukkitYaml.getBoolean("settings.allow-end")) {
                getLogger().log(Level.WARNING, "正在禁用世界 End");
                bukkitYaml.set("settings.allow-end", false);
                modified = true;
            }
            String generator = bukkitYaml.getString("worlds.world_the_void.generator");
            if (generator == null || !generator.equals("AnotherBedWars")) {
                getLogger().log(Level.WARNING, "正在为世界 world_the_void 配置 VoidGenerator");
                bukkitYaml.set("worlds.world_the_void.generator", "AnotherBedWars");
                modified = true;
            }
            if (modified) {
                bukkitYaml.save(bukkitFile);
                getLogger().log(Level.WARNING, "已修改 bukkit.yml 配置");
                restartNeeded = true;
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "尝试操作 bukkit.yml 时发生错误", e);
            getServer().getPluginManager().disablePlugin(this);
        }
        //创建插件数据目录
        createPluginFolder();
        if (restartNeeded) {
            getLogger().log(Level.WARNING, "即将重启服务器以应用配置更改");
            getServer().getScheduler().runTaskLater(this, getServer()::restart, 20);
            return;
        }
        //清理所有无用的世界
        clearUselessWorlds();
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        //检测 AdvancedSlimePaper API 依赖是否存在
        if (!isAdvancedSlimePaperAPIPresent()) {
            getLogger().log(Level.SEVERE, "未找到 AdvancedSlimePaperAPI 请检查服务器依赖");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //初始化 AdvancedSlimePaper API
        asp = AdvancedSlimePaperAPI.instance();
        slimeLoader = new FileLoader(new File(getDataFolder(), "slime_worlds"));
        serverManager = new ServerManager(this);
        serverManager.enable();
        getLogger().log(Level.WARNING, "插件所需运行环境已配置完成 正在启动插件");
        getServer().getConsoleSender().sendMessage(Component.newline()
                .appendNewline()
                .append(Component.text(
                                """
                                         █████╗ ███╗   ██╗ ██████╗ ████████╗██╗  ██╗███████╗██████╗   ██████╗ ███████╗██████╗ ██╗    ██╗ █████╗ ██████╗ ███████╗
                                        ██╔══██╗████╗  ██║██╔═══██╗╚══██╔══╝██║  ██║██╔════╝██╔══██╗  ██╔══██╗██╔════╝██╔══██╗██║    ██║██╔══██╗██╔══██╗██╔════╝
                                        ███████║██╔██╗ ██║██║   ██║   ██║   ███████║█████╗  ██████╔╝  ██████╔╝█████╗  ██║  ██║██║ █╗ ██║███████║██████╔╝███████╗
                                        ██╔══██║██║╚██╗██║██║   ██║   ██║   ██╔══██║██╔══╝  ██╔══██╗  ██╔══██╗██╔══╝  ██║  ██║██║███╗██║██╔══██║██╔══██╗╚════██║
                                        ██║  ██║██║ ╚████║╚██████╔╝   ██║   ██║  ██║███████╗██║  ██║  ██████╔╝███████╗██████╔╝╚███╔███╔╝██║  ██║██║  ██║███████║
                                        ╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝    ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝  ╚═════╝ ╚══════╝╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝
                                        """, NamedTextColor.GREEN)
                        .appendNewline()
                        .append(Component.text("                             Enabled AnotherBedWars v1.0 by Mengbill", NamedTextColor.BLUE)))
                .appendNewline()
        );
    }

    @Override
    public void onDisable() {
        // 清理所有 Task
        getServer().getScheduler().cancelTasks(this);
    }

    private void createPluginFolder() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
            getLogger().log(Level.SEVERE, "无法创建插件数据目录: " + getDataFolder().getAbsolutePath());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        File slime_worlds = new File(getDataFolder(), "slime_worlds");
        if (!slime_worlds.exists() && !slime_worlds.mkdirs()) {
            getLogger().log(Level.SEVERE, "无法创建插件数据目录: slime_worlds: " + slime_worlds.getAbsolutePath());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void clearUselessWorlds() {
        String[] useless_worlds = {"world", "world_nether", "world_the_end"};
        for (String world : useless_worlds) {
            File worldFolder = new File(getServer().getWorldContainer(), world);
            if (worldFolder.exists() && worldFolder.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(worldFolder);
                    getLogger().log(Level.WARNING, "正在清理世界 " + world);
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, "尝试清理世界 " + world + " 时发生错误", e);
                    getServer().getPluginManager().disablePlugin(this);
                }
            }
        }
    }

    private boolean isAdvancedSlimePaperAPIPresent() {
        try {
            Class.forName("com.infernalsuite.asp.api.AdvancedSlimePaperAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
        return new VoidGenerator();
    }

    public static class VoidGenerator extends ChunkGenerator {
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
