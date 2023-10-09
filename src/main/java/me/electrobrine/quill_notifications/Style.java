package me.electrobrine.quill_notifications;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Style {
    public static MutableText stylize(String message, me.electrobrine.quill_notifications.api.Scribe type) {
        return switch (type) {
            case ACHIEVEMENT -> Text.literal(message).formatted(Formatting.AQUA);
            case INFO -> Text.literal(message).formatted(Formatting.ITALIC).formatted(Formatting.GREEN);
            case EVENT -> Text.literal(message).formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
            case WARN -> Text.literal(message).formatted(Formatting.GOLD).formatted(Formatting.ITALIC);
            case ERROR -> Text.literal(message).formatted(Formatting.DARK_RED).formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE);
            case NONE -> (MutableText) Text.of(message);
        };
    }
}
