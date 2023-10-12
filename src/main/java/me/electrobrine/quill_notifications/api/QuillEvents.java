package me.electrobrine.quill_notifications.api;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.kyori.adventure.text.Component;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;

import java.util.UUID;

public class QuillEvents {
    public static final Event<PRE_SEND_NOTIFICATION> PRE_SEND_NOTIFICATION = EventFactory.createArrayBacked(PRE_SEND_NOTIFICATION.class, callbacks -> (message) -> {
        for (PRE_SEND_NOTIFICATION callback : callbacks) {
            return callback.trigger(message);
        }
        return true;
    });

    @FunctionalInterface
    public interface PRE_SEND_NOTIFICATION {
        boolean trigger(Pigeon.Message message);
    }
}
