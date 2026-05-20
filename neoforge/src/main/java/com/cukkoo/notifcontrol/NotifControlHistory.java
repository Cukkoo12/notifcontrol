package com.cukkoo.notifcontrol;

import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;

public class NotifControlHistory {

    public record Entry(long timestamp, String type, String title, boolean blocked) {
        public Component toText() {
            String status = blocked ? "§c" + Component.translatable("text.notifcontrol.blocked").getString() + "§r" : "§a" + Component.translatable("text.notifcontrol.shown").getString() + "§r";
            String time = LocalTime.ofInstant(
                    java.time.Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()
            ).format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            return Component.literal(String.format("%s %s - %s: %s", status, time, type, title));
        }
    }

    private static final int MAX_ENTRIES = 50;
    private static final LinkedList<Entry> entries = new LinkedList<>();

    public static void add(String type, String title, boolean blocked) {
        entries.addFirst(new Entry(System.currentTimeMillis(), type, title, blocked));
        if (entries.size() > MAX_ENTRIES) {
            entries.removeLast();
        }
    }

    public static List<Entry> getRecent() {
        return List.copyOf(entries);
    }

    public static void clear() {
        entries.clear();
    }
}
