package me.electrobrine.quill_notifications;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import me.electrobrine.quill_notifications.api.Pigeon;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

public class NotifyCommand {
    public static void registerCommand(CommandDispatcher<ServerCommandSource> commandDispatcher) {
        commandDispatcher.register(
                CommandManager.literal("notify")
                        .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile()).suggests((context, builder) -> {
                            PlayerManager playerManager = ((ServerCommandSource) context.getSource()).getServer().getPlayerManager();
                            return CommandSource.suggestMatching(playerManager.getPlayerList().stream().map((player -> {
                                return player.getGameProfile().getName();
                            })), builder);
                        }).then(CommandManager.argument("message", MessageArgumentType.message()).executes(context -> notify(context, GameProfileArgumentType.getProfileArgument(context, "player"), MessageArgumentType.getMessage(context, "message"))))));
    }


    private static int notify(CommandContext<ServerCommandSource> context, Collection<GameProfile> players, Text message) {
        for (GameProfile profile : players) {
            NotificationBuilder notification = new NotificationBuilder(profile.getId());
            notification.setMessage(profile.getName() + ": " + message.getString());
            Pigeon.send(notification.build());
        }
        if (context.getSource().getPlayer() != null) context.getSource().getPlayer().sendMessage(Text.of("Notification sent"));
        return 0;
    }
}
