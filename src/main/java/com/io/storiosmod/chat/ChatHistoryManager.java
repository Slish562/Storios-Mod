package com.io.storiosmod.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatHistoryManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_STORED_MESSAGES = 200;

    private static Path saveFile;
    private static final List<ChatEntry> messageHistory = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, Long> logoutTimes = new ConcurrentHashMap<>();

    public static void init(Path serverDir) {
        Path configDir = serverDir.resolve("config").resolve("storios_mod");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }
        saveFile = configDir.resolve("chat_history.json");
        load();
    }

    public static void recordMessage(String senderName, String message, long timestamp) {
        ChatEntry entry = new ChatEntry(senderName, message, timestamp);
        messageHistory.add(entry);
        if (messageHistory.size() > MAX_STORED_MESSAGES) {
            messageHistory.remove(0);
        }
        save();
    }

    public static void recordLogout(UUID playerUUID) {
        logoutTimes.put(playerUUID.toString(), System.currentTimeMillis());
        save();
    }

    public static List<ChatEntry> getMissedMessages(UUID playerUUID) {
        Long logoutTime = logoutTimes.get(playerUUID.toString());
        if (logoutTime == null)
            return Collections.emptyList();

        List<ChatEntry> missed = new ArrayList<>();
        synchronized (messageHistory) {
            for (ChatEntry entry : messageHistory) {
                if (entry.timestamp > logoutTime) {
                    missed.add(entry);
                }
            }
        }
        return missed;
    }

    public static void clearLogoutTime(UUID playerUUID) {
        logoutTimes.remove(playerUUID.toString());
    }

    public static Component formatMissedMessages(List<ChatEntry> messages) {
        MutableComponent root = Component.literal("");

        root.append(Component.literal("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF))));
        root.append(Component.literal("  \uD83D\uDCE8 Missed Messages (" + messages.size() + ")\n")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFF55)).withBold(true)));
        root.append(Component.literal("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF))));

        for (ChatEntry entry : messages) {
            long minutesAgo = (System.currentTimeMillis() - entry.timestamp) / 60000;
            String timeStr;
            if (minutesAgo < 1) {
                timeStr = "<1m ago";
            } else if (minutesAgo < 60) {
                timeStr = minutesAgo + "m ago";
            } else {
                long hours = minutesAgo / 60;
                timeStr = hours + "h " + (minutesAgo % 60) + "m ago";
            }

            root.append(Component.literal(" [" + timeStr + "] ")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA))));
            root.append(Component.literal(entry.senderName)
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x55FF55))));
            root.append(Component.literal(": ")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAAAAAA))));
            root.append(Component.literal(entry.message + "\n")
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))));
        }

        root.append(Component.literal("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x55FFFF))));

        return root;
    }

    private static void load() {
        if (saveFile == null || !Files.exists(saveFile))
            return;

        try (Reader reader = new InputStreamReader(new FileInputStream(saveFile.toFile()), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<SaveData>() {
            }.getType();
            SaveData data = GSON.fromJson(reader, type);
            if (data != null) {
                if (data.messages != null) {
                    messageHistory.clear();
                    messageHistory.addAll(data.messages);
                }
                if (data.logoutTimes != null) {
                    logoutTimes.clear();
                    logoutTimes.putAll(data.logoutTimes);
                }
                LOGGER.info("Loaded {} chat entries, {} logout records", messageHistory.size(), logoutTimes.size());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load chat history", e);
        }
    }

    private static void save() {
        if (saveFile == null)
            return;

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(saveFile.toFile()), StandardCharsets.UTF_8)) {
            SaveData data = new SaveData();
            data.messages = new ArrayList<>(messageHistory);
            data.logoutTimes = new HashMap<>(logoutTimes);
            GSON.toJson(data, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save chat history", e);
        }
    }

    public static class ChatEntry {
        public String senderName;
        public String message;
        public long timestamp;

        public ChatEntry() {
        }

        public ChatEntry(String senderName, String message, long timestamp) {
            this.senderName = senderName;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    private static class SaveData {
        public List<ChatEntry> messages;
        public Map<String, Long> logoutTimes;
    }
}

