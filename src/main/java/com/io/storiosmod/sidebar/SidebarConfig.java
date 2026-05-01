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
            if (config != null) return config;
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

    public static SidebarConfig createDefault() {
        SidebarConfig config = new SidebarConfig();
        config.name = "Default";
        config.updateTicks = 20;
        config.titleChangeTicks = 40;
        config.titles.add("§6§lStorios Server");
        config.lines.addAll(List.of(
                "",
                "§7» §fOnline:",
                "§7 ▪ §e{online}§7/§e{max_players}",
                "",
                "§7» §fPing:",
                "§7 ▪ §6{ping}ms",
                "",
                "§7» §fTPS:",
                "§7 ▪ {tps}"
        ));
        return config;
    }

    public static SidebarConfig createInfo() {
        SidebarConfig config = new SidebarConfig();
        config.name = "Info";
        config.updateTicks = 10;
        config.titleChangeTicks = 20;
        config.titles.addAll(List.of("§e§lServer Info", "§6§lServer Info"));
        config.lines.addAll(List.of(
                "",
                "§7» §fPlayer:",
                "§7 ▪ §a{player}",
                "",
                "§7» §fPosition:",
                "§7 ▪ §e{x} {y} {z}",
                "",
                "§7» §fHealth:",
                "§7 ▪ §c{health} §7HP",
                "",
                "§7» §fOnline:",
                "§7 ▪ §e{online}§7/§e{max_players}",
                "",
                "§7» §fTPS:",
                "§7 ▪ {tps}"
        ));
        return config;
    }
}
