package com.example.manhunt.sponge;

import com.example.manhunt.SpongeMain;
import com.example.manhunt.sponge.impl.SpongePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;

public class SpongeCommand {

    private final SpongeMain plugin;
    private final Parameter.Value<String> messageParam;

    public SpongeCommand(SpongeMain plugin) {
        this.plugin = plugin;
        this.messageParam = Parameter.string().key("message").build();
    }

    public void register(RegisterCommandEvent<Command.Parameterized> event) {
        Command.Parameterized hcCommand = Command.builder()
                .addParameter(messageParam)
                .executor(this::executeHC)
                .permission("manhunt.hunter")
                .build();

        Command.Parameterized startCommand = Command.builder()
                .executor(context -> {
                    plugin.getGameManager().startCountdown();
                    return CommandResult.success();
                })
                .permission("manhunt.admin")
                .build();

        Command.Parameterized stopCommand = Command.builder()
                .executor(context -> {
                    plugin.getGameManager().endGame(null, true);
                    return CommandResult.success();
                })
                .permission("manhunt.admin")
                .build();

        Command.Parameterized trackCommand = Command.builder()
                .executor(this::executeTrack)
                .addParameter(Parameter.player().key("target").build())
                .build();

        Command.Parameterized surroundCommand = Command.builder()
                .executor(this::executeSurround)
                .addParameter(Parameter.player().key("target").build())
                .permission("manhunt.admin")
                .build();

        Command.Parameterized donateCommand = Command.builder()
                .executor(this::executeDonate)
                .addParameter(Parameter.player().key("target").build())
                .build();

        Command.Parameterized mainCommand = Command.builder()
                .addChild(startCommand, "start")
                .addChild(stopCommand, "stop")
                .addChild(trackCommand, "track")
                .addChild(surroundCommand, "surround")
                .addChild(donateCommand, "donate")
                .executor(this::executeMain)
                .build();

        event.register(plugin.getContainer(), mainCommand, "manhunt", "mh");
        event.register(plugin.getContainer(), hcCommand, "hc");
    }

    private CommandResult executeTrack(CommandContext context) {
        if (!(context.cause().root() instanceof ServerPlayer)) return CommandResult.error(Component.text("Only players can use this!"));
        ServerPlayer player = (ServerPlayer) context.cause().root();
        
        if (plugin.getGameManager().isRunner(new SpongePlayer(player))) {
            ServerPlayer target = context.requireOne(Parameter.player().key("target").build());
            if (!plugin.getGameManager().isHunter(new SpongePlayer(target))) {
                player.sendMessage(Component.text("Bu oyuncu bir avcı değil!", NamedTextColor.RED));
                return CommandResult.success();
            }
            plugin.getCompassManager().setRunnerTrackTarget(target.uniqueId());
            player.sendMessage(Component.text("Hedef avcı olarak ayarlandı: " + target.name(), NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Sadece kaçan kişi takip hedefi belirleyebilir!", NamedTextColor.RED));
        }
        return CommandResult.success();
    }

    private CommandResult executeHC(CommandContext context) {
        if (!(context.cause().root() instanceof ServerPlayer)) return CommandResult.error(Component.text("Only players can use this!"));
        ServerPlayer player = (ServerPlayer) context.cause().root();
        
        if (!plugin.getGameManager().isHunter(new SpongePlayer(player))) {
            player.sendMessage(Component.text("Sadece avcılar takım sohbetini kullanabilir!", NamedTextColor.RED));
            return CommandResult.success();
        }

        String message = context.requireOne(messageParam);
        Component formatted = Component.text("[AVCI-CHAT] ", NamedTextColor.GOLD)
                .append(Component.text(player.name() + ": ", NamedTextColor.YELLOW))
                .append(Component.text(message, NamedTextColor.WHITE));

        for (ServerPlayer online : org.spongepowered.api.Sponge.server().onlinePlayers()) {
            if (plugin.getGameManager().isHunter(new SpongePlayer(online))) {
                online.sendMessage(formatted);
            }
        }
        return CommandResult.success();
    }

    private CommandResult executeSurround(CommandContext context) {
        ServerPlayer target = context.requireOne(Parameter.player().key("target").build());
        if (!plugin.getGameManager().isRunner(new SpongePlayer(target))) {
            return CommandResult.error(Component.text("Hedef bir runner değil!", NamedTextColor.RED));
        }
        plugin.getGameManager().surroundRunner(new SpongePlayer(target));
        context.cause().audience().sendMessage(Component.text("Runner kuşatıldı!", NamedTextColor.GREEN));
        return CommandResult.success();
    }

    private CommandResult executeDonate(CommandContext context) {
        if (!(context.cause().root() instanceof ServerPlayer)) return CommandResult.error(Component.text("Only players can use this!"));
        ServerPlayer player = (ServerPlayer) context.cause().root();

        if (!plugin.getGameManager().isHunter(new SpongePlayer(player))) {
            player.sendMessage(Component.text("Sadece avcılar eşya gönderebilir!", NamedTextColor.RED));
            return CommandResult.success();
        }

        ServerPlayer target = context.requireOne(Parameter.player().key("target").build());
        if (!plugin.getGameManager().isHunter(new SpongePlayer(target))) {
            player.sendMessage(Component.text("Eşyayı sadece başka bir avcıya gönderebilirsin!", NamedTextColor.RED));
            return CommandResult.success();
        }

        org.spongepowered.api.item.inventory.ItemStack item = player.itemInHand(org.spongepowered.api.data.type.HandTypes.MAIN_HAND);
        if (item.isEmpty()) {
            player.sendMessage(Component.text("Elinde bir eşya tutmalısın!", NamedTextColor.RED));
            return CommandResult.success();
        }

        player.setItemInHand(org.spongepowered.api.data.type.HandTypes.MAIN_HAND, org.spongepowered.api.item.inventory.ItemStack.empty());
        target.inventory().offer(item);
        
        player.sendMessage(Component.text(target.name() + " adlı oyuncuya eşya gönderildi.", NamedTextColor.GREEN));
        target.sendMessage(Component.text(player.name() + " sana bir eşya gönderdi!", NamedTextColor.GOLD));
        
        return CommandResult.success();
    }

    private CommandResult executeMain(CommandContext context) {
        if (context.cause().root() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) context.cause().root();
            new com.example.manhunt.sponge.menu.MainMenu(plugin).open(player);
        }
        return CommandResult.success();
    }
}
