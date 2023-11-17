package me.electrobrine.quill_notifications;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.fabric.FabricAudiences;
import net.kyori.adventure.text.Component;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.UUID;

import static me.electrobrine.quill_notifications.QuillNotifications.mailbox;
import static me.electrobrine.quill_notifications.QuillNotifications.players;

@AllArgsConstructor
public class Notification {
    private int databaseID = -1;
    @Getter
    private final UUID uuid;
    private ServerPlayerEntity player;
    @Getter
    private MutableText message;
    @Getter
    private Component component;
    @Setter
    @Getter
    private JsonElement metadata;
    @Setter
    @Getter
    private SoundEvent sound;
    @Setter
    @Getter
    private ArrayList<String> commands;
    @Setter
    @Getter
    private long commandDelay;

    public void setMessage(MutableText newMessage) {
        this.message = newMessage;
        this.component = FabricAudiences.nonWrappingSerializer().deserialize(newMessage);
    }

    public void setComponent(Component newComponent) {
        this.component = newComponent;
        this.message = (MutableText) FabricAudiences.nonWrappingSerializer().serialize(newComponent);
    }
    public ServerPlayerEntity getPlayerEntity() {
        if (this.player == null) this.player = QuillNotifications.playerManager.get(this.getUuid());
        return player;
    }

    public void cancel() {
        if (this.databaseID == -1) return;
        mailbox.drop(this.databaseID);
        JsonArray newMessages = ((JsonArray) players.get(this.uuid).getJson("messages")).deepCopy();
        newMessages.remove(this.databaseID);
        players.get(this.uuid).put("messages", newMessages);
    }
}
