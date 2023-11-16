package me.electrobrine.quill_notifications.api;

import com.google.gson.JsonArray;
import me.electrobrine.quill_notifications.Notification;
import me.electrobrine.quill_notifications.QuillNotifications;
import me.mrnavastar.sqlib.DataContainer;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

import java.util.ArrayList;
import java.util.Collections;
public class Pigeon {

    public static void send(Notification notification) {
        ServerPlayerEntity player = QuillNotifications.playerManager.get(notification.getUuid());
        notification.getPlayerEntity();
        if (player == null) {
            store(notification);
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
                    commandManager.executeWithPrefix(server.getCommandSource(), command);
                }
            }).start();
        }
        if (notification.getMessage() == null) return;
        player.sendMessage(notification.getMessage());
    }


    private static void store(Notification notification) {
        DataContainer message = QuillNotifications.mailbox.createDataContainerAutoID();
        message.put("text", notification.getMessage());
        if (notification.getMetadata() != null) {
            message.put("metadata", notification.getMetadata());
        }
        if (notification.getSound() != null) {
            message.put("sound", notification.getSound().getId());
        }
        if (notification.getCommands() != null) {
            JsonArray jsonCommands = new JsonArray();
            for (String command : notification.getCommands()) {
                jsonCommands.add(command);
            }
            message.put("commands", jsonCommands);
        }
        message.put("commandDelay", (double) notification.getCommandDelay());
        DataContainer player = QuillNotifications.players.get(notification.getUuid());
        JsonArray messages = new JsonArray();
        if (player == null) {
            player = QuillNotifications.players.createDataContainer(notification.getUuid());
            messages.add(message.getIdAsInt());
            player.put("messages", messages);
            return;
        }
        messages = (JsonArray) player.getJson("messages");
        messages.add(message.getIdAsInt());
        player.put("messages", messages);
    }
}
