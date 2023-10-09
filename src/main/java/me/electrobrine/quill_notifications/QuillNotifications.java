package me.electrobrine.quill_notifications;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mc.microconfig.MicroConfig;
import me.electrobrine.quill_notifications.api.Pigeon;
import mrnavastar.sqlib.DataContainer;
import mrnavastar.sqlib.Table;
import mrnavastar.sqlib.database.Database;
import mrnavastar.sqlib.database.MySQLDatabase;
import mrnavastar.sqlib.database.SQLiteDatabase;
import mrnavastar.sqlib.sql.SQLDataType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;


public class QuillNotifications implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    public static Config config = MicroConfig.getOrCreate("Quill Notifications", new Config());
    public static Table players;
    public static Table mailbox;
    public static HashMap<UUID, ServerPlayerEntity> playerManager = new HashMap<>();
    @Override
    public void onInitialize() {
        Database database;
        if (Objects.equals(config.databaseType, "MYSQL")) {
            if (Objects.equals(config.databaseUser, "Quillium")) {
                log("Please provide a new database username", Level.ERROR);
                return;
            }
            database = new MySQLDatabase("Quill", config.databaseName, config.databaseIP, config.databasePort, config.databaseUser, config.databasePassword);
        }
        else {
            if (Objects.equals(config.databaseDirectory,"/path/to/folder")) {
                log("Please put in a valid folder path", Level.ERROR);
                return;
            }
            database = new SQLiteDatabase("Quill", config.databaseName, config.databaseDirectory);
        }
        log("dipping the ink quill", Level.INFO);
        players = database.createTable("Notifications")
                .addColumn("messages", SQLDataType.JSON)
                .finish();
        mailbox = database.createTable("messages")
                        .addColumn("text", SQLDataType.MUTABLE_TEXT)
                        .addColumn("metadata", SQLDataType.JSON)
                        .addColumn("sound", SQLDataType.IDENTIFIER)
                        .finish();
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID playerUUID = player.getUuid();
            playerManager.put(playerUUID, player);
            DataContainer data = players.get(playerUUID);
            if (data == null) return;
            JsonArray messages = (JsonArray) data.getJson("messages");
            if (messages == null) return;
            for (JsonElement message : messages) {
                DataContainer messageData = mailbox.get(message.getAsInt());
                SoundEvent sound;
                if (messageData.getIdentifier("sound") == null) sound = null;
                else sound = SoundEvent.of(messageData.getIdentifier("sound"));
                Pigeon.send(playerUUID, messageData.getMutableText("text"), messageData.getJson("metadata"), sound);
                mailbox.drop(message.getAsInt());
            }
        }));
    }
    private static void log(String message, Level level) {
        LogManager.getLogger().log(level, "[Quill Notifications] " + message);
    }
}

