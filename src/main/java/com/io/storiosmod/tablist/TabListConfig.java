package com.io.storiosmod.tablist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TabListConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public String name = "Default";
    public int updateTicks = 20;
    public List<List<String>> header = new ArrayList<>();
    public List<List<String>> footer = new ArrayList<>();
    public String playerNameFormat = "{player}";

    public static TabListConfig loadFromFile(Path file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
            TabListConfig config = GSON.fromJson(reader, TabListConfig.class);
            if (config != null) return config;
        } catch (Exception e) {
            LOGGER.error("Failed to load tab style from {}", file.getFileName(), e);
        }
        return new TabListConfig();
    }

    public static void saveToFile(Path file, TabListConfig config) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file.toFile()), StandardCharsets.UTF_8)) {
            GSON.toJson(config, writer);
        } catch (Exception e) {
            LOGGER.error("Failed to save tab style to {}", file.getFileName(), e);
        }
    }

    public static TabListConfig createDefault() {
        TabListConfig config = new TabListConfig();
        config.name = "Default";
        config.updateTicks = 20;
        config.header.add(List.of(
                "",
                "§6§lStorios Server §e⛏",
                "",
                "§8——[ §c{online}§7/§c{max_players} §8]——",
                ""
        ));
        config.footer.add(List.of(
                "",
                "§8————————————————",
                "",
                "§7TPS: {tps} §8| §7Ping: §6{ping}ms",
                ""
        ));
        return config;
    }

    public static TabListConfig createAnimated() {
        TabListConfig config = new TabListConfig();
        config.name = "Animated";
        config.updateTicks = 10;
        config.header.add(List.of(
                "",
                "§6§lStorios Server §e⛏",
                "",
                "§8——[ §c{online}§7/§c{max_players} §8]——",
                ""
        ));
        config.header.add(List.of(
                "",
                "§e§lStorios Server §6⛏",
                "",
                "§8——[ §c{online}§7/§c{max_players} §8]——",
                ""
        ));
        config.footer.add(List.of(
                "",
                "§8————————————————",
                "",
                "§7TPS: {tps} §8| §7Ping: §6{ping}ms",
                ""
        ));
        config.footer.add(List.of(
                "",
                "§8————————————————",
                "",
                "§7Player: §a{player} §8| §7Online: §a{online}",
                ""
        ));
        return config;
    }
}
