package me.electrobrine.quill_notifications.api;

import com.google.gson.JsonArray;
import me.electrobrine.quill_notifications.Notification;
import me.electrobrine.quill_notifications.QuillNotifications;
import me.mrnavastar.sqlib.api.DataContainer;
import me.mrnavastar.sqlib.api.types.JavaTypes;
import me.mrnavastar.sqlib.api.types.MinecraftTypes;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

import java.util.Date;

public class Pigeon {

    public static void send(Notification notification) {
        ServerPlayerEntity player = QuillNotifications.playerManager.get(notification.getUuid());
        notification.getPlayerEntity();
        if (player == null) {
            store(notification);
            return;
        }
        if (notification.getExpiry() != 0 && notification.getExpiry() + notification.getCreationTime() <= new Date().getTime()) {
            notification.cancel();
            return;
        }
        if (!QuillEvents.PRE_SEND_NOTIFICATION.invoker().trigger(notification)) return;
        if (notification.getSound() != null)
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(RegistryEntry.of(notification.getSound()), SoundCategory.MASTER, player.getX(), player.getY(), player.getZ(), 1, 1, player.getWorld().getRandom().nextLong()));
        if (notification.getCommands() != null) {
            new Thread(() -> {
                try {
                    Thread.sleep(notification.getCommandDelay());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                MinecraftServer server = player.getServer();
                assert server != null;
                CommandManager commandManager = server.getCommandManager();
                for (String command : notification.getCommands()) {
                    if (!command.startsWith("/")) command = "/" + command;
                    commandManager.executeWithPrefix(server.getCommandSource(), command);
                }
            }).start();
        }
        if (notification.getMessage() == null) return;
        player.sendMessage(notification.getMessage());
    }


    private static void store(Notification notification) {
        long time = new Date().getTime();
        DataContainer message = QuillNotifications.mailbox.createContainer();
        message.put(JavaTypes.UUID, "receiver", notification.getUuid());
        if (notification.getMessage() != null) {
            message.put(MinecraftTypes.TEXT, "text", notification.getMessage());
        }
        if (notification.getMetadata() != null) {
            message.put(MinecraftTypes.JSON, "metadata", notification.getMetadata());
        }
        if (notification.getSound() != null) {
            message.put(MinecraftTypes.SOUND, "sound", notification.getSound());
        }
        if (notification.getCommands() != null) {
            JsonArray jsonCommands = new JsonArray();
            for (String command : notification.getCommands()) {
                jsonCommands.add(command);
            }
            message.put(MinecraftTypes.JSON, "commands", jsonCommands);
        }
        message.put(JavaTypes.LONG, "commandDelay", notification.getCommandDelay());
        message.put(JavaTypes.LONG,"expiry", notification.getExpiry());
        message.put(JavaTypes.LONG, "creationTime", time);
    }
}
