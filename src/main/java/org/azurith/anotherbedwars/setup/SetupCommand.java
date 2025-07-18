package org.azurith.anotherbedwars.setup;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.azurith.anotherbedwars.exception.WorldImportException;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;

public class SetupCommand {

    private final SetupManager setupManager;

    public SetupCommand(SetupManager setupManager) {
        this.setupManager = setupManager;
    }

    public LiteralArgumentBuilder<CommandSourceStack> createCommand() {
        return Commands.literal("abwa")
                .then(Commands.literal("import")
                        .then(Commands.argument("VANILLA_WORLD_NAME", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    Optional.ofNullable(setupManager.getServerManager().getPlugin().getServer().getWorldContainer().listFiles(File::isDirectory))
                                            .stream()
                                            .flatMap(Arrays::stream)
                                            .map(File::getName)
                                            .filter(entry -> entry.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                                            .forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String worldName = context.getArgument("VANILLA_WORLD_NAME", String.class);
                                    try {
                                        setupManager.importVanillaWorld(worldName);
                                        context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                        "[AnotherBedWars] <green>导入临时世界 <yellow><world> <green>成功 <gray>(类型: Vanilla World)",
                                                        Placeholder.unparsed("world", worldName))
                                                .clickEvent(ClickEvent.runCommand("/abwa teleport " + worldName))
                                                .hoverEvent(HoverEvent.showText(Component.text("点击传送至该世界", NamedTextColor.BLUE)))
                                        );
                                    } catch (WorldImportException e) {
                                        context.getSource().getSender().sendMessage(MiniMessage.miniMessage().deserialize(
                                                "[AnotherBedWars] <red>导入临时世界 <yellow><world> <red>失败 原因: <reason>",
                                                Placeholder.unparsed("world", worldName),
                                                Placeholder.unparsed("reason", e.getMessage())
                                        ));
                                        setupManager.getServerManager().getPlugin().getLogger().log(Level.SEVERE, "导入临时世界 " + worldName + " 失败 " + e.getMessage());
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        ));
    }

}
