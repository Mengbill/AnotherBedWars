package org.azurith.anotherbedwars.setup;

import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.azurith.anotherbedwars.AnotherBedWars;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Stream;

public class SetupCommand {

    private final SetupManager setupManager;
    private final AnotherBedWars plugin;

    public SetupCommand(SetupManager setupManager) {
        this.setupManager = setupManager;
        this.plugin = setupManager.getServerManager().getPlugin();
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("abwa")
                .then(Commands.literal("convert")
                        .then(Commands.argument("VANILLA_WORLD_FOLDER", StringArgumentType.word())
                                .suggests(((context, builder) -> suggestPaths(builder, plugin.getServer().getWorldContainer().toPath(), false)))
                                .executes(context -> {
                                    String worldFolder = context.getArgument("VANILLA_WORLD_FOLDER", String.class);
                                    boolean success = setupManager.convertVanillaWorld(worldFolder, errorMsg -> {
                                        context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                "[AnotherBedWars] <red>转换临时世界 <yellow><world> <red>失败 原因: <reason>",
                                                Placeholder.unparsed("world", worldFolder),
                                                Placeholder.unparsed("reason", errorMsg)
                                        ));
                                        plugin.getLogger().log(Level.SEVERE, "导入临时世界 " + worldFolder + " 失败 原因: " + errorMsg);
                                    });
                                    if (success) {
                                        context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                "[AnotherBedWars] <green>转换临时世界 <yellow><world> <green>成功",
                                                Placeholder.unparsed("world", worldFolder))
                                        );
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        ))
                .then(Commands.literal("teleport")
                        .then(Commands.argument("SLIME_WORLD_NAME", StringArgumentType.word())
                                .suggests((context, builder) -> suggestLoadedSlimeWorlds(builder))
                                .executes(context -> {
                                    String worldName = context.getArgument("SLIME_WORLD_NAME", String.class);
                                    if (context.getSource().getExecutor() instanceof Player player) {
                                        if (setupManager.teleportSlimeWorld(player, worldName)) {
                                            context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                    "[AnotherBedWars] <green>传送至临时世界 <yellow><world> <green>成功",
                                                    Placeholder.unparsed("world", worldName)
                                            ));
                                        } else {
                                            context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                    "[AnotherBedWars] <red>传送至临时世界 <yellow><world> <red>失败 原因: <reason>",
                                                    Placeholder.unparsed("world", worldName),
                                                    Placeholder.unparsed("reason", "世界不存在或未被正确加载为SLIME_WORLD")
                                            ));
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        ))
                .then(Commands.literal("load")
                        .then(Commands.argument("SLIME_WORLD_FILE", StringArgumentType.word())
                                .suggests(((context, builder) -> suggestPaths(builder, plugin.getDataPath().resolve("slime_worlds"), true)))
                                .executes(context -> {
                                    String worldName = context.getArgument("SLIME_WORLD_FILE", String.class).replaceAll(".slime", "");
                                    boolean success = setupManager.loadSlimeWorld(worldName, errorMsg -> {
                                        context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                "[AnotherBedWars] <red>加载临时世界 <yellow><world> <red>失败 原因: <reason>",
                                                Placeholder.unparsed("world", worldName),
                                                Placeholder.unparsed("reason", errorMsg)
                                        ));
                                        plugin.getLogger().log(Level.SEVERE, "加载临时世界 " + worldName + " 失败 原因: " + errorMsg);
                                    });
                                    if (success) {
                                        context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                        "[AnotherBedWars] <green>加载临时世界 <yellow><world> <green>成功",
                                                        Placeholder.unparsed("world", worldName))
                                                .clickEvent(ClickEvent.runCommand("/abwa teleport " + worldName))
                                                .hoverEvent(HoverEvent.showText(Component.text("点击传送至该世界", NamedTextColor.BLUE)))
                                        );
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        ))
                .then(Commands.literal("unload")
                        .then(Commands.argument("SLIME_WORLD_NAME", StringArgumentType.word())
                                .suggests((context, builder) -> suggestLoadedSlimeWorlds(builder))
                                .executes(context -> {
                                    String worldName = context.getArgument("SLIME_WORLD_NAME", String.class);
                                    if (setupManager.unloadSlimeWorld(worldName)) {
                                        context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                "[AnotherBedWars] <green>卸载临时世界 <yellow><world> <green>成功",
                                                Placeholder.unparsed("world", worldName)
                                        ));
                                    } else {
                                        context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                "[AnotherBedWars] <red>卸载临时世界 <yellow><world> <red>失败 原因: <reason>",
                                                Placeholder.unparsed("world", worldName),
                                                Placeholder.unparsed("reason", "世界不存在或未被正确加载为SLIME_WORLD")
                                        ));
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        ));
    }

    private CompletableFuture<Suggestions> suggestLoadedSlimeWorlds(SuggestionsBuilder builder) {
        List<SlimeWorldInstance> slimeWorldInstances = plugin.getAsp().getLoadedWorlds();
        if (slimeWorldInstances == null || slimeWorldInstances.isEmpty()) return builder.buildFuture();
        slimeWorldInstances.stream()
                .map(SlimeWorld::getName)
                .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                .sorted()
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestPaths(SuggestionsBuilder builder, Path path, boolean filesOnly) {
        CompletableFuture<Suggestions> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Stream<Path> pathStream = Files.list(path)) {
                pathStream.filter(filesOnly ? Files::isRegularFile : Files::isDirectory)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                        .sorted()
                        .forEach(builder::suggest);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "发生异常", e);
            }
            future.complete(builder.build());
        });
        return future;
    }


}
