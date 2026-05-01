package com.io.storiosmod.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChatStyleConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path configFile;
    private static ChatStyleConfig INSTANCE = new ChatStyleConfig();

    public String chatFormat = "<{player}> {message}";
    public String joinFormat = "";
    public String leaveFormat = "";
    public String deathFormat = "{death_message}";
    public String advancementFormat = "§a{player} has made the advancement §e[{advancement}]";
    public boolean enableFormatCodes = true;
    public boolean enableClickableLinks = true;

    public static ChatStyleConfig get() {
        return INSTANCE;
    }

    public static void init(Path serverDir) {
        Path configDir = serverDir.resolve("config").resolve("storios_mod");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create config directory", e);
        }
        configFile = configDir.resolve("chat_style.json");
        load();
    }

    public static boolean reload() {
        return load();
    }

    private static boolean load() {
        if (configFile == null) return false;

        if (!Files.exists(configFile)) {
            INSTANCE = new ChatStyleConfig();
            save();
            LOGGER.info("Created default chat style config");
            return true;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(configFile.toFile()), StandardCharsets.UTF_8)) {
            ChatStyleConfig loaded = GSON.fromJson(reader, ChatStyleConfig.class);
            if (loaded != null) {
                boolean needsSave = false;
                if ("§e{player} joined the game".equals(loaded.joinFormat)) {
                    loaded.joinFormat = "";
                    needsSave = true;
                }
                if ("§e{player} left the game".equals(loaded.leaveFormat)) {
                    loaded.leaveFormat = "";
                    needsSave = true;
                }
                INSTANCE = loaded;
                if (needsSave) save();
                LOGGER.info("Loaded chat style config");
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load chat style config", e);
        }
        return false;
    }

    private static void save() {
        if (configFile == null) return;

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save chat style config", e);
        }
    }
}
