package me.electrobrine.quill_notifications;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.electrobrine.quill_notifications.api.NotificationBuilder;
import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.api.DataContainer;
import me.mrnavastar.sqlib.api.DataStore;
import me.mrnavastar.sqlib.api.types.GsonTypes;
import me.mrnavastar.sqlib.api.types.JavaTypes;
import me.mrnavastar.sqlib.api.types.MinecraftTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyori.adventure.platform.modcommon.MinecraftServerAudiences;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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
    public static MinecraftServer server;
    @Override
    public void onInitialize() {
        log("dipping the ink quill", Level.INFO);
        ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> NotifyCommand.registerCommand(dispatcher));
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();
            playerManager.put(playerUUID, player);
            NotificationBuilder notification = new NotificationBuilder(playerUUID);
            mailbox.getContainers("receiver", playerUUID).forEach(message -> {
                notification.setMessage((MutableText) message.get(MinecraftTypes.TEXT, "text").orElseGet(() -> null));
                notification.setSound(message.get(MinecraftTypes.SOUND, "sound").orElseGet(() -> null));
                notification.setMetadata(message.get(GsonTypes.ELEMENT, "metadata").orElseGet(() -> null));
                ArrayList<String> stringCommands = new ArrayList<>();
                for (JsonElement command : (JsonArray) Objects.requireNonNull(message.get(GsonTypes.ELEMENT, "commands").orElseGet(() -> null))) {
                    stringCommands.add(command.getAsString());
                }
                notification.setCommands(stringCommands.toArray(String[] :: new));
                notification.setCommandDelay(message.get(JavaTypes.LONG, "commandDelay").orElseGet(() -> 0L));
                notification.setExpiry(message.get(JavaTypes.LONG, "expiry").orElseGet(() -> 0L));
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
            for (JsonElement command : (JsonArray) Objects.requireNonNull(container.get(GsonTypes.ELEMENT, "commands").orElseGet(() -> null))) {
                stringCommands.add(command.getAsString());
            }
            Notification notification = new Notification(
                    container.getId(),
                    uuid,
                    null,
                    (MutableText) container.get(MinecraftTypes.TEXT, "text").orElseGet(() -> null),
                    MinecraftServerAudiences.of(server).nonWrappingSerializer().deserialize(Objects.requireNonNull(container.get(MinecraftTypes.TEXT, "text").orElseGet(() -> null))),
                    container.get(GsonTypes.ELEMENT, "metadata").orElseGet(() -> null),
                    container.get(MinecraftTypes.SOUND, "sound").orElseGet(() -> null),
                    stringCommands,
                    container.get(JavaTypes.LONG, "commandDelay").orElseGet(() -> null),
                    container.get(JavaTypes.LONG, "expiry").orElseGet(() -> null),
                    container.get(JavaTypes.LONG, "creationTime").orElseGet(() -> null));
            notifications.add(notification);
        });
        return notifications;
    }
}

