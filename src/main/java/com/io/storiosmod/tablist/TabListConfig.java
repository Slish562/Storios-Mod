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
            if (config != null) {
                if (config.header != null) {
                    for (List<String> list : config.header) {
                        for (int i = 0; i < list.size(); i++) {
                            list.set(i, fixEncoding(list.get(i)));
                        }
                    }
                }
                if (config.footer != null) {
                    for (List<String> list : config.footer) {
                        for (int i = 0; i < list.size(); i++) {
                            list.set(i, fixEncoding(list.get(i)));
                        }
                    }
                }
                return config;
            }
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

    private static String fixEncoding(String text) {
        if (text == null) return null;
        return text.replace("Â§", "\u00A7")
                   .replace("Â»", "\u00BB")
                   .replace("â–ª", "\u25AA")
                   .replace("â€”", "\u2014");
    }

    public static TabListConfig createDefault() {
        TabListConfig config = new TabListConfig();
        config.name = "Default";
        config.updateTicks = 20;
        config.header.add(List.of(
                "",
                "\u00A76\u00A7lStorios Server \u00A7e\u26E9",
                "",
                "\u00A78\u2014\u2014[ \u00A7c{online}\u00A77/\u00A7c{max_players} \u00A78]\u2014\u2014",
                ""
        ));
        config.footer.add(List.of(
                "",
                "\u00A78\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014",
                "",
                "\u00A77TPS: {tps} \u00A78| \u00A77Ping: \u00A76{ping}ms",
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
                "\u00A76\u00A7lStorios Server \u00A7e\u26E9",
                "",
                "\u00A78\u2014\u2014[ \u00A7c{online}\u00A77/\u00A7c{max_players} \u00A78]\u2014\u2014",
                ""
        ));
        config.header.add(List.of(
                "",
                "\u00A7e\u00A7lStorios Server \u00A76\u26E9",
                "",
                "\u00A78\u2014\u2014[ \u00A7c{online}\u00A77/\u00A7c{max_players} \u00A78]\u2014\u2014",
                ""
        ));
        config.footer.add(List.of(
                "",
                "\u00A78\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014",
                "",
                "\u00A77TPS: {tps} \u00A78| \u00A77Ping: \u00A76{ping}ms",
                ""
        ));
        config.footer.add(List.of(
                "",
                "\u00A78\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014\u2014",
                "",
                "\u00A77Player: \u00A7a{player} \u00A78| \u00A77Online: \u00A7a{online}",
                ""
        ));
        return config;
    }
}
