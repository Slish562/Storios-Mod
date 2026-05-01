package com.io.storiosmod.tablist;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TabListManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NBT_KEY = "storios_mod:tab_style";

    private static final LinkedHashMap<String, TabListConfig> STYLES = new LinkedHashMap<>();
    private static Path stylesDir;
    private static int tickCounter = 0;

    public static void init(Path serverDir) {
        stylesDir = serverDir.resolve("config").resolve("storios_mod").resolve("tab_styles");
        try {
            Files.createDirectories(stylesDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create tab_styles directory", e);
        }
        loadAllStyles();
    }

    public static boolean reload() {
        STYLES.clear();
        tickCounter = 0;
        return loadAllStyles();
    }

    private static boolean loadAllStyles() {
        try {
            boolean hasFiles = false;

            if (Files.exists(stylesDir)) {
                try (var files = Files.list(stylesDir)) {
                    var jsonFiles = files.filter(p -> p.toString().endsWith(".json")).toList();
                    for (Path file : jsonFiles) {
                        hasFiles = true;
                        String fileName = file.getFileName().toString();
                        String styleId = fileName.substring(0, fileName.length() - 5);
                        TabListConfig config = TabListConfig.loadFromFile(file);
                        STYLES.put(styleId, config);
                    }
                }
            }

            if (!hasFiles) {
                TabListConfig defaultStyle = TabListConfig.createDefault();
                TabListConfig animatedStyle = TabListConfig.createAnimated();
                TabListConfig.saveToFile(stylesDir.resolve("default.json"), defaultStyle);
                TabListConfig.saveToFile(stylesDir.resolve("animated.json"), animatedStyle);
                STYLES.put("default", defaultStyle);
                STYLES.put("animated", animatedStyle);
            }

            LOGGER.info("Loaded {} tab list styles", STYLES.size());
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to load tab styles", e);
            return false;
        }
    }

    public static void tick(MinecraftServer server) {
        if (STYLES.isEmpty()) return;
        tickCounter++;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.connection == null) continue;

            String styleId = getPlayerStyle(player);
            TabListConfig style = STYLES.getOrDefault(styleId, STYLES.values().iterator().next());

            if (style.updateTicks <= 0 || tickCounter % style.updateTicks != 0) continue;

            int frame = (tickCounter / Math.max(style.updateTicks, 1));

            Component header = buildFrame(style.header, frame, server, player);
            Component footer = buildFrame(style.footer, frame, server, player);

            player.connection.send(new ClientboundTabListPacket(header, footer));
        }
    }

    public static void sendImmediate(ServerPlayer player) {
        if (STYLES.isEmpty()) return;

        String styleId = getPlayerStyle(player);
        TabListConfig style = STYLES.getOrDefault(styleId, STYLES.values().iterator().next());

        Component header = buildFrame(style.header, 0, player.getServer(), player);
        Component footer = buildFrame(style.footer, 0, player.getServer(), player);

        player.connection.send(new ClientboundTabListPacket(header, footer));
    }

    private static Component buildFrame(List<List<String>> frames, int frame, MinecraftServer server, ServerPlayer player) {
        if (frames == null || frames.isEmpty()) return Component.empty();

        int index = frame % frames.size();
        List<String> lines = frames.get(index);

        String joined = String.join("\n", lines);
        String resolved = resolvePlaceholders(joined, server, player);

        return Component.literal(resolved);
    }

    private static String resolvePlaceholders(String text, MinecraftServer server, ServerPlayer player) {
        text = text.replace("{online}", String.valueOf(server.getPlayerCount()));
        text = text.replace("{max_players}", String.valueOf(server.getMaxPlayers()));
        text = text.replace("{player}", player.getDisplayName().getString());
        text = text.replace("{ping}", String.valueOf(player.latency));

        double tps = Math.min(20.0, calculateTps(server));
        String tpsColor;
        if (tps >= 18.0) {
            tpsColor = "§a";
        } else if (tps >= 15.0) {
            tpsColor = "§e";
        } else {
            tpsColor = "§c";
        }
        text = text.replace("{tps}", tpsColor + String.format("%.1f", tps));

        text = text.replace("{server_name}", server.getServerModName());

        return text;
    }

    private static double calculateTps(MinecraftServer server) {
        long[] tickTimes = server.tickTimes;
        if (tickTimes == null || tickTimes.length == 0) return 20.0;

        long sum = 0;
        for (long t : tickTimes) {
            sum += t;
        }
        double avgTickMs = (sum / (double) tickTimes.length) / 1_000_000.0;
        if (avgTickMs <= 0) return 20.0;
        return Math.min(20.0, 1000.0 / avgTickMs);
    }

    private static CompoundTag getPersistedData(ServerPlayer player) {
        CompoundTag forgeData = player.getPersistentData();
        if (!forgeData.contains(Player.PERSISTED_NBT_TAG)) {
            forgeData.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return forgeData.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static String getPlayerStyle(ServerPlayer player) {
        CompoundTag data = getPersistedData(player);
        if (data.contains(NBT_KEY)) {
            return data.getString(NBT_KEY);
        }
        return getDefaultStyleId();
    }

    public static void setPlayerStyle(ServerPlayer player, String styleId) {
        getPersistedData(player).putString(NBT_KEY, styleId);
        sendImmediate(player);
    }

    public static boolean styleExists(String styleId) {
        return STYLES.containsKey(styleId);
    }

    public static Collection<String> getStyleIds() {
        return STYLES.keySet();
    }

    public static String getStyleName(String styleId) {
        TabListConfig config = STYLES.get(styleId);
        return config != null ? config.name : styleId;
    }

    private static String getDefaultStyleId() {
        if (STYLES.containsKey("default")) return "default";
        if (!STYLES.isEmpty()) return STYLES.keySet().iterator().next();
        return "default";
    }
}
