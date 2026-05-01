package com.io.storiosmod.cutscene;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class CutsceneStorage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path cutsceneDir;
    private static final Map<String, CutsceneTimeline> cache = new HashMap<>();

    public static void init(Path serverDir) {
        cutsceneDir = serverDir.resolve("storiosmod").resolve("cutscenes");
        try {
            Files.createDirectories(cutsceneDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create cutscene directory", e);
        }
        reloadAll();
    }

    public static void save(CutsceneTimeline timeline) {
        cache.put(timeline.getName(), timeline);
        Path file = cutsceneDir.resolve(timeline.getName() + ".json");
        try {
            String json = GSON.toJson(timeline.toJson());
            Files.writeString(file, json);
        } catch (IOException e) {
            LOGGER.error("Failed to save cutscene '{}'", timeline.getName(), e);
        }
    }

    public static CutsceneTimeline load(String name) {
        CutsceneTimeline cached = cache.get(name);
        if (cached != null)
            return cached;

        Path file = cutsceneDir.resolve(name + ".json");
        if (!Files.exists(file))
            return null;

        try {
            String content = Files.readString(file);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            CutsceneTimeline tl = CutsceneTimeline.fromJson(json);
            cache.put(name, tl);
            return tl;
        } catch (Exception e) {
            LOGGER.error("Failed to load cutscene '{}'", name, e);
            return null;
        }
    }

    public static void reloadAll() {
        cache.clear();
        if (cutsceneDir == null || !Files.exists(cutsceneDir))
            return;
        try (Stream<Path> files = Files.list(cutsceneDir)) {
            files.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                String name = p.getFileName().toString().replace(".json", "");
                load(name);
            });
        } catch (IOException e) {
            LOGGER.error("Failed to reload cutscenes", e);
        }
    }

    public static Collection<String> getNames() {
        return Collections.unmodifiableSet(cache.keySet());
    }

    public static void delete(String name) {
        cache.remove(name);
        Path file = cutsceneDir.resolve(name + ".json");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LOGGER.error("Failed to delete cutscene '{}'", name, e);
        }
    }
}
