package org.azurith.anotherbedwars.config;

import org.azurith.anotherbedwars.AnotherBedWars;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.nio.file.Files;
import java.util.logging.Level;

public class ConfigManager {

    private final AnotherBedWars plugin;

    private PluginConfig pluginConfig;

    private final HoconConfigurationLoader pluginConfigLoader;

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public ConfigManager(AnotherBedWars plugin) {
        this.plugin = plugin;
        this.pluginConfigLoader = HoconConfigurationLoader.builder()
                .path(plugin.getDataPath().resolve("config.conf"))
                .build();
    }

    public void init() {
        createDataFolder();
        if (Files.notExists(plugin.getDataPath().resolve("config.conf"))) {
            this.pluginConfig = new PluginConfig();
            savePluginConfig();
        }
        loadPluginConfig();
    }

    private void loadPluginConfig() {
        try {
            this.pluginConfig = pluginConfigLoader.load().get(PluginConfig.class);
        } catch (ConfigurateException e) {
            plugin.getLogger().log(Level.SEVERE, "尝试读取配置文件 config.conf 时发生错误", e);
            plugin.terminateWithShutdown();
        }
    }

    public void savePluginConfig() {
        try {
            pluginConfigLoader.save(pluginConfigLoader.createNode().set(PluginConfig.class, pluginConfig));
        } catch (ConfigurateException e) {
            plugin.getLogger().log(Level.SEVERE, "尝试保存配置文件 config.conf 时发生错误", e);
            plugin.terminateWithShutdown();
        }
    }

    private void createDataFolder() {
        File[] folders = {
                plugin.getDataFolder(),
                new File(plugin.getDataFolder(), "slime_worlds"),
                new File(plugin.getDataFolder(), "arena_configs")
        };
        for (File folder : folders) {
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    plugin.getLogger().log(Level.SEVERE, "无法创建插件数据目录: " + folder.getAbsolutePath());
                    plugin.terminateWithShutdown();
                }
                plugin.getLogger().log(Level.WARNING, "已创建插件数据目录: " + folder.getAbsolutePath());
            }
        }
    }

}
