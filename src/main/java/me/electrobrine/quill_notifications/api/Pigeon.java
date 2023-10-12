package me.electrobrine.quill_notifications.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Data;
import me.electrobrine.quill_notifications.QuillNotifications;
import me.electrobrine.quill_notifications.Style;
import mrnavastar.sqlib.DataContainer;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.Component;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
public class Pigeon {
    @Data
    public static class Message {
        private UUID uuid;
        private MutableText text;
        private Component component;
        private JsonElement metadata;
        private SoundEvent sound;

        public Message(UUID uuid, MutableText text, Component component, JsonElement metadata, SoundEvent sound) {
            this.uuid = uuid;
            this.text = text;
            this.component = component;
            this.metadata = metadata;
            this.sound = sound;
        }
    }
    public static void send(UUID uuid, String message, Scribe style) {send(uuid, Style.stylize(message, style), null, null);}

    public static void send(UUID uuid, String message, Scribe style, JsonElement metadata) {send(uuid, Style.stylize(message, style), metadata, null);}

    public static void send(UUID uuid, String message, Scribe style, SoundEvent sound) {send(uuid, Style.stylize(message, style), null, sound);}

    public static void send(UUID uuid, String message, Scribe style, JsonElement metadata, SoundEvent sound) {send(uuid, Style.stylize(message, style), metadata, sound);}

    public static void send(UUID uuid, Component message) {send(uuid, (MutableText) FabricAudiences.nonWrappingSerializer().serialize(message), null, null);}

    public static void send(UUID uuid, Component message, JsonElement metadata) {send(uuid, (MutableText) FabricAudiences.nonWrappingSerializer().serialize(message), metadata, null);}

    public static void send(UUID uuid, Component message, SoundEvent sound) {send(uuid, (MutableText) FabricAudiences.nonWrappingSerializer().serialize(message), null, sound);}

    public static void send(UUID uuid, Component message, JsonElement metadata, SoundEvent sound) {send(uuid, (MutableText) FabricAudiences.nonWrappingSerializer().serialize(message), metadata, sound);}
    public static void send(UUID uuid, MutableText message) {
        send(uuid, message, null, null);
    }

    public static void send(UUID uuid, MutableText message, JsonElement metadata) {send(uuid, message, metadata, null);}

    public static void send(UUID uuid, MutableText message, SoundEvent sound) {
        send(uuid, message, null, sound);
    }

    public static void send(UUID uuid, MutableText message, JsonElement metadata, SoundEvent sound) {
        ServerPlayerEntity player = QuillNotifications.playerManager.get(uuid);
        if (player == null) {
            store(uuid, message, metadata, sound);
            return;
        }
        Component messageAsComponent = FabricAudiences.nonWrappingSerializer().deserialize(message);
        Message message1 = new Message(uuid, message, messageAsComponent, metadata, sound);
        if (!QuillEvents.PRE_SEND_NOTIFICATION.invoker().trigger(message1)) return;

        if (sound != null)
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(RegistryEntry.of(message1.sound), SoundCategory.MASTER, player.getX(), player.getY(), player.getZ(), 1, 1, player.getWorld().getRandom().nextLong()));
        if (!message.equals(message1.text)) player.sendMessage(message1.text);
        else player.sendMessage(message1.component);
    }

    private static void store(UUID uuid, MutableText text, JsonElement metadata, SoundEvent sound) {
        ArrayList<Integer> list = QuillNotifications.mailbox.getIdsAsInts();
        Collections.sort(list);
        int id;
        if (!list.isEmpty()) {
            id = list.get(list.size() - 1) + 1;
        }
        else {id = 0;}
        DataContainer message = QuillNotifications.mailbox.createDataContainer(id);
        message.put("text", text);
        if (metadata != null) {
            message.put("metadata", metadata);
        }
        if (sound != null) {
            message.put("sound", sound.getId());
        }
        DataContainer player = QuillNotifications.players.get(uuid);
        JsonArray messages = new JsonArray();
        if (player == null) {
            player = QuillNotifications.players.createDataContainer(uuid);
            messages.add(id);
            player.put("messages", messages);
            return;
        }
        messages = (JsonArray) player.getJson("messages");
        messages.add(id);
        player.put("messages", messages);
    }
}
