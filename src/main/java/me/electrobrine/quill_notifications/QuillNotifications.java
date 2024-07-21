package me.electrobrine.quill_notifications;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.electrobrine.quill_notifications.api.NotificationBuilder;
import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.api.DataContainer;
import me.mrnavastar.sqlib.api.DataStore;
import me.mrnavastar.sqlib.api.types.JavaTypes;
import me.mrnavastar.sqlib.api.types.MinecraftTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.util.*;

public class QuillNotifications implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    public static DataStore mailbox = SQLib.getDatabase().dataStore("Quill", "Messages");
    public static HashMap<UUID, ServerPlayerEntity> playerManager = new HashMap<>();
    @Override
    public void onInitialize() {
        log("dipping the ink quill", Level.INFO);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> NotifyCommand.registerCommand(dispatcher));
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();
            playerManager.put(playerUUID, player);
            NotificationBuilder notification = new NotificationBuilder(playerUUID);
            mailbox.getContainers("receiver", playerUUID).forEach(message -> {
                notification.setMessage((MutableText) message.get(MinecraftTypes.TEXT, "text"));
                notification.setSound(SoundEvent.of(message.get(MinecraftTypes.IDENTIFIER, "sound")));
                notification.setMetadata(message.get(MinecraftTypes.JSON, "metadata"));
                ArrayList<String> stringCommands = new ArrayList<>();
                for (JsonElement command : (JsonArray) message.get(MinecraftTypes.JSON, "commands")) {
                    stringCommands.add(command.getAsString());
                }
                notification.setCommands(stringCommands.toArray(String[] :: new));
                notification.setCommandDelay(message.get(JavaTypes.LONG, "commandDelay"));
                notification.send();
                mailbox.getContainer(message.getId()).ifPresent(DataContainer::delete);
            });
        }));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID playerUUID = handler.getPlayer().getUuid();
            playerManager.remove(playerUUID);
        });
    }
    private static void log(String message, Level level) {
        LogManager.getLogger().log(level, "[Quill Notifications] " + message);
    }

    public static ArrayList<Notification> getNotifications(UUID uuid) {
        ArrayList<Notification> notifications = new ArrayList<>();
        mailbox.getContainers("receiver", uuid).forEach(container -> {
            ArrayList<String> stringCommands = new ArrayList<>();
            for (JsonElement command : (JsonArray) container.get(MinecraftTypes.JSON, "commands")) {
                stringCommands.add(command.getAsString());
            }
            Notification notification = new Notification(
                    container.getId(),
                    uuid,
                    null,
                    (MutableText) container.get(MinecraftTypes.TEXT, "text"),
                    FabricAudiences.nonWrappingSerializer().deserialize(container.get(MinecraftTypes.TEXT, "text")),
                    container.get(MinecraftTypes.JSON, "metadata"),
                    SoundEvent.of(container.get(MinecraftTypes.IDENTIFIER, "sound")),
                    stringCommands,
                    container.get(JavaTypes.LONG, "commandDelay"),
                    container.get(JavaTypes.LONG, "expiry"),
                    container.get(JavaTypes.LONG, "creationTime"));
            notifications.add(notification);
        });
        return notifications;
    }
}

