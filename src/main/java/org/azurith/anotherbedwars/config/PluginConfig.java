package org.azurith.anotherbedwars.config;

import org.azurith.anotherbedwars.server.ServerMode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class PluginConfig {

    @Setting("server")
    public final Server server = new Server();

    @ConfigSerializable
    public static class Server {
        @Comment("当前服务器节点运行模式 可选: SETUP, RUNTIME")
        public ServerMode server_mode = ServerMode.SETUP;
    }

}
