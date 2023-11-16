package me.electrobrine.quill_notifications.api;

import me.electrobrine.quill_notifications.Notification;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class QuillEvents {
    public static final Event<PRE_SEND_NOTIFICATION> PRE_SEND_NOTIFICATION = EventFactory.createArrayBacked(PRE_SEND_NOTIFICATION.class, callbacks -> (notification) -> {
        for (PRE_SEND_NOTIFICATION callback : callbacks) {
            return callback.trigger(notification);
        }
        return true;
    });

    @FunctionalInterface
    public interface PRE_SEND_NOTIFICATION {
        boolean trigger(Notification notification);
    }
}
