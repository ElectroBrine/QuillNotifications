package me.electrobrine.quill_notifications.api;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;

import java.util.UUID;

public class QuillEvents {
    public static final Event<PRE_SEND_NOTIFICATION> PRE_SEND_NOTIFICATION = EventFactory.createArrayBacked(PRE_SEND_NOTIFICATION.class, callbacks -> (receiver, message, metadata, sound) -> {
        for (PRE_SEND_NOTIFICATION callback : callbacks) {
            callback.trigger(receiver, message, metadata, sound);
        }
    });

    @FunctionalInterface
    public interface PRE_SEND_NOTIFICATION {
        void trigger(UUID receiver, MutableText message, JsonElement metadata, SoundEvent sound);
    }
}
