package com.io.storiosmod.sidebar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SidebarConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public String name = "Default";
    public int updateTicks = 20;
    public List<String> titles = new ArrayList<>();
    public int titleChangeTicks = 20;
    public List<String> lines = new ArrayList<>();

    public static SidebarConfig loadFromFile(java.nio.file.Path file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
            SidebarConfig config = GSON.fromJson(reader, SidebarConfig.class);
            if (config != null) {
                if (config.titles != null) {
                    for (int i = 0; i < config.titles.size(); i++) {
                        config.titles.set(i, fixEncoding(config.titles.get(i)));
                    }
                }
                if (config.lines != null) {
                    for (int i = 0; i < config.lines.size(); i++) {
                        config.lines.set(i, fixEncoding(config.lines.get(i)));
                    }
                }
                return config;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load sidebar style from {}", file.getFileName(), e);
        }
        return new SidebarConfig();
    }

    public static void saveToFile(java.nio.file.Path file, SidebarConfig config) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save sidebar style to {}", file.getFileName(), e);
        }
    }

    private static String fixEncoding(String text) {
        if (text == null) return null;
        return text.replace("Â§", "\u00A7")
                   .replace("Â»", "\u00BB")
                   .replace("â–ª", "\u25AA")
                   .replace("â€”", "\u2014");
    }

    public static SidebarConfig createDefault() {
        SidebarConfig config = new SidebarConfig();
        config.name = "Default";
        config.updateTicks = 20;
        config.titleChangeTicks = 40;
        config.titles.add("\u00A76\u00A7lStorios Server");
        config.lines.addAll(List.of(
                "",
                "\u00A77\u00BB \u00A7fOnline:",
                "\u00A77 \u25AA \u00A7e{online}\u00A77/\u00A7e{max_players}",
                "",
                "\u00A77\u00BB \u00A7fPing:",
                "\u00A77 \u25AA \u00A76{ping}ms",
                "",
                "\u00A77\u00BB \u00A7fTPS:",
                "\u00A77 \u25AA {tps}"
        ));
        return config;
    }

    public static SidebarConfig createInfo() {
        SidebarConfig config = new SidebarConfig();
        config.name = "Info";
        config.updateTicks = 10;
        config.titleChangeTicks = 20;
        config.titles.addAll(List.of("\u00A7e\u00A7lServer Info", "\u00A76\u00A7lServer Info"));
        config.lines.addAll(List.of(
                "",
                "\u00A77\u00BB \u00A7fPlayer:",
                "\u00A77 \u25AA \u00A7a{player}",
                "",
                "\u00A77\u00BB \u00A7fPosition:",
                "\u00A77 \u25AA \u00A7e{x} {y} {z}",
                "",
                "\u00A77\u00BB \u00A7fHealth:",
                "\u00A77 \u25AA \u00A7c{health} \u00A77HP",
                "",
                "\u00A77\u00BB \u00A7fOnline:",
                "\u00A77 \u25AA \u00A7e{online}\u00A77/\u00A7e{max_players}",
                "",
                "\u00A77\u00BB \u00A7fTPS:",
                "\u00A77 \u25AA {tps}"
        ));
        return config;
    }
}
