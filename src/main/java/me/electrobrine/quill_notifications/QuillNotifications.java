package me.electrobrine.quill_notifications;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.electrobrine.quill_notifications.api.Pigeon;
import me.mrnavastar.sqlib.DataContainer;
import me.mrnavastar.sqlib.SQLib;
import me.mrnavastar.sqlib.Table;
import me.mrnavastar.sqlib.sql.SQLDataType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class QuillNotifications implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    public static Table players;
    public static Table mailbox;
    public static HashMap<UUID, ServerPlayerEntity> playerManager = new HashMap<>();
    @Override
    public void onInitialize() {
        log("dipping the ink quill", Level.INFO);
        players = SQLib.getDatabase().createTable("Notifications")
                .addColumn("messages", SQLDataType.JSON)
                .finish();
        mailbox = SQLib.getDatabase().createTable("messages")
                .addColumn("text", SQLDataType.MUTABLE_TEXT)
                .addColumn("metadata", SQLDataType.JSON)
                .addColumn("sound", SQLDataType.IDENTIFIER)
                .addColumn("commands", SQLDataType.JSON)
                .addColumn("commandDelay", SQLDataType.DOUBLE)
                .setAutoIncrement()
                .finish();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> NotifyCommand.registerCommand(dispatcher));
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();
            playerManager.put(playerUUID, player);
            DataContainer data = players.get(playerUUID);
            if (data == null) return;
            NotificationBuilder notification = new NotificationBuilder(playerUUID);
            JsonArray messages = (JsonArray) data.getJson("messages");
            if (messages == null) return;
            for (JsonElement message : messages) {
                DataContainer messageData = mailbox.get(message.getAsInt());
                notification.setMessage(messageData.getMutableText("text"));
                if (messageData.getIdentifier("sound") != null) notification.setSound(SoundEvent.of(messageData.getIdentifier("sound")));
                if (messageData.getJson("metadata") != null) notification.setMetadata(messageData.getJson("metadata"));
                if (messageData.getJson("commands") != null) {
                    ArrayList<String> stringCommands = new ArrayList<>();
                    for (JsonElement command : (JsonArray) messageData.getJson("commands")) {
                        stringCommands.add(command.getAsString());
                    }
                    notification.setCommands(stringCommands.toArray(String[] :: new));
                }
                notification.setCommandDelay((long) messageData.getDouble("commandDelay"));
                Pigeon.send(notification.build());
                notification.build().getPlayerEntity().getName().getString();
                mailbox.drop(message.getAsInt());
                JsonArray newMessages = messages.deepCopy();
                newMessages.remove(message);
                data.put("messages", newMessages);
            }
        }));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID playerUUID = handler.getPlayer().getUuid();
            playerManager.remove(playerUUID);
        });
    }
    private static void log(String message, Level level) {
        LogManager.getLogger().log(level, "[Quill Notifications] " + message);
    }
}

