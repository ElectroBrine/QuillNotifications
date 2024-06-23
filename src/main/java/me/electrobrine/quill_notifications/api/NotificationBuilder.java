package me.electrobrine.quill_notifications.api;

import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import me.electrobrine.quill_notifications.Notification;
import me.electrobrine.quill_notifications.Style;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.Component;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class NotificationBuilder {
    private final UUID uuid;
    private MutableText message = null;
    private String stringMessage = null;
    private Scribe style = Scribe.NONE;
    private Component componentMessage = null;
    private JsonElement metadata = null;
    private SoundEvent sound = null;
    private final ArrayList<String> commands = new ArrayList<>();
    private long commandDelay = 0;
    private long expiry = 0;
    private final Long creationTime = new Date().getTime();

    public static NotificationBuilder Notification(UUID uuid) {
        return new NotificationBuilder(uuid);
    }
    /**
     * setMessage will set any String or Component messages to null
     */
    public NotificationBuilder setMessage(MutableText message) {
        this.stringMessage = null;
        this.componentMessage = null;
        this.message = message;
        return this;
    }
    /**
     * setMessage will set any Component or MutableText messages to null
     */
    public NotificationBuilder setMessage(String message) {
        this.message = null;
        this.componentMessage = null;
        this.stringMessage = message;
        return this;
    }

    /**
     * setMessage will set any String or MutableText messages to null
     */
    public NotificationBuilder setMessage(Component message) {
        this.message = null;
        this.stringMessage = null;
        this.componentMessage = message;
        return this;
    }

    /**
     * setStyle is used to apply styling to String messages
     * setStyle will have no effect on MutableText or Component Messages
     */
    public NotificationBuilder setStyle(Scribe style) {
        this.style = style;
        return this;
    }

    public NotificationBuilder setMetadata(JsonElement metadata) {
        this.metadata = metadata;
        return this;
    }

    public NotificationBuilder setSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    public NotificationBuilder setCommands(String ... commands) {
        this.commands.addAll(Arrays.asList(commands));
        return this;
    }

    public NotificationBuilder setCommandDelay(int amount, TimeUnit unit) {
        this.commandDelay = unit.toMillis(amount);
        return this;
    }

    public NotificationBuilder setCommandDelay(long timeMillis) {
        this.commandDelay = timeMillis;
        return this;
    }

    public NotificationBuilder setExpiry(int amount, TimeUnit unit) {
        this.expiry = unit.toMillis(amount);
        return this;
    }

    public NotificationBuilder setExpiry(long timeMillis) {
        this.expiry = timeMillis;
        return this;
    }

    public Notification build(){
        if (this.stringMessage != null) {
            message = Style.stylize(stringMessage, style);
        }
        else if (this.componentMessage != null) {
            message = (MutableText) FabricAudiences.nonWrappingSerializer().serialize(componentMessage);
        }
        return new Notification(-1, uuid, null, message, FabricAudiences.nonWrappingSerializer().deserialize(message), metadata, sound, commands, commandDelay, expiry, creationTime);
    }

    public void send() {
        Pigeon.send(this.build());
    }
}
