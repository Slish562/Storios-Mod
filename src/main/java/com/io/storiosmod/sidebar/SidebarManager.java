package com.io.storiosmod.sidebar;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SidebarManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NBT_KEY = "storios_mod:sidebar_style";
    private static final String OBJECTIVE_NAME = "storios_sidebar";

    private static final LinkedHashMap<String, SidebarConfig> STYLES = new LinkedHashMap<>();
    private static final Map<UUID, String[]> PREVIOUS_LINES = new HashMap<>();
    private static Path stylesDir;
    private static int tickCounter = 0;

    public static void init(Path serverDir) {
        stylesDir = serverDir.resolve("config").resolve("storios_mod").resolve("sidebars");
        try {
            Files.createDirectories(stylesDir);
        } catch (IOException e) {
            LOGGER.error("Failed to create sidebars directory", e);
        }
        loadAllStyles();
    }

    public static boolean reload() {
        STYLES.clear();
        PREVIOUS_LINES.clear();
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
                        SidebarConfig config = SidebarConfig.loadFromFile(file);
                        STYLES.put(styleId, config);
                    }
                }
            }

            if (!hasFiles) {
                SidebarConfig defaultStyle = SidebarConfig.createDefault();
                SidebarConfig infoStyle = SidebarConfig.createInfo();
                SidebarConfig.saveToFile(stylesDir.resolve("default.json"), defaultStyle);
                SidebarConfig.saveToFile(stylesDir.resolve("info.json"), infoStyle);
                STYLES.put("default", defaultStyle);
                STYLES.put("info", infoStyle);
            }

            LOGGER.info("Loaded {} sidebar styles", STYLES.size());
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to load sidebar styles", e);
            return false;
        }
    }

    public static void tick(MinecraftServer server) {
        if (STYLES.isEmpty()) return;
        tickCounter++;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.connection == null) continue;

            String styleId = getPlayerStyle(player);

            if ("off".equals(styleId)) continue;

            SidebarConfig style = STYLES.getOrDefault(styleId, STYLES.values().iterator().next());

            if (style.updateTicks <= 0 || tickCounter % style.updateTicks != 0) continue;

            sendSidebar(player, style);
        }
    }

    public static void sendSidebar(ServerPlayer player, SidebarConfig style) {
        int titleFrame = (tickCounter / Math.max(style.titleChangeTicks, 1)) % Math.max(style.titles.size(), 1);
        String titleStr = style.titles.isEmpty() ? "" : style.titles.get(titleFrame);
        titleStr = resolvePlaceholders(titleStr, player.getServer(), player);
        Component title = Component.literal(titleStr);

        Objective objective = new Objective(
                player.getServer().getScoreboard(),
                OBJECTIVE_NAME,
                ObjectiveCriteria.DUMMY,
                title,
                ObjectiveCriteria.RenderType.INTEGER
        );

        player.connection.send(new ClientboundSetObjectivePacket(objective, ClientboundSetObjectivePacket.METHOD_ADD));
        player.connection.send(new ClientboundSetDisplayObjectivePacket(1, objective));

        String[] prevLines = PREVIOUS_LINES.get(player.getUUID());
        if (prevLines != null) {
            for (String prevLine : prevLines) {
                player.connection.send(new ClientboundSetScorePacket(
                        ServerScoreboard.Method.REMOVE, OBJECTIVE_NAME, prevLine, 0));
            }
        }

        List<String> resolvedLines = new ArrayList<>();
        for (String line : style.lines) {
            resolvedLines.add(resolvePlaceholders(line, player.getServer(), player));
        }

        int lineCount = resolvedLines.size();
        String[] lineKeys = new String[lineCount];

        for (int i = 0; i < lineCount; i++) {
            String resolved = resolvedLines.get(i);
            if (resolved.isEmpty()) {
                StringBuilder spacer = new StringBuilder();
                for (int s = 0; s <= i; s++) spacer.append("§r");
                resolved = spacer.toString();
            }
            lineKeys[i] = resolved;

            player.connection.send(new ClientboundSetScorePacket(
                    ServerScoreboard.Method.CHANGE, OBJECTIVE_NAME, resolved, lineCount - 1 - i));
        }

        PREVIOUS_LINES.put(player.getUUID(), lineKeys);
    }

    public static void removeSidebar(ServerPlayer player) {
        Objective objective = new Objective(
                player.getServer().getScoreboard(),
                OBJECTIVE_NAME,
                ObjectiveCriteria.DUMMY,
                Component.empty(),
                ObjectiveCriteria.RenderType.INTEGER
        );
        player.connection.send(new ClientboundSetObjectivePacket(objective, ClientboundSetObjectivePacket.METHOD_REMOVE));
        PREVIOUS_LINES.remove(player.getUUID());
    }

    public static void sendImmediate(ServerPlayer player) {
        if (STYLES.isEmpty()) return;

        String styleId = getPlayerStyle(player);
        if ("off".equals(styleId)) return;

        SidebarConfig style = STYLES.getOrDefault(styleId, STYLES.values().iterator().next());
        sendSidebar(player, style);
    }

    private static String resolvePlaceholders(String text, MinecraftServer server, ServerPlayer player) {
        text = text.replace("{online}", String.valueOf(server.getPlayerCount()));
        text = text.replace("{max_players}", String.valueOf(server.getMaxPlayers()));
        text = text.replace("{player}", player.getDisplayName().getString());
        text = text.replace("{ping}", String.valueOf(player.latency));
        text = text.replace("{health}", String.format("%.0f", player.getHealth()));
        text = text.replace("{x}", String.valueOf((int) player.getX()));
        text = text.replace("{y}", String.valueOf((int) player.getY()));
        text = text.replace("{z}", String.valueOf((int) player.getZ()));

        double tps = Math.min(20.0, calculateTps(server));
        String tpsColor;
        if (tps >= 18.0) tpsColor = "§a";
        else if (tps >= 15.0) tpsColor = "§e";
        else tpsColor = "§c";
        text = text.replace("{tps}", tpsColor + String.format("%.1f", tps));

        return text;
    }

    private static double calculateTps(MinecraftServer server) {
        long[] tickTimes = server.tickTimes;
        if (tickTimes == null || tickTimes.length == 0) return 20.0;
        long sum = 0;
        for (long t : tickTimes) sum += t;
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
        if ("off".equals(styleId)) {
            removeSidebar(player);
        } else {
            sendImmediate(player);
        }
    }

    public static boolean styleExists(String styleId) {
        return STYLES.containsKey(styleId);
    }

    public static Collection<String> getStyleIds() {
        return STYLES.keySet();
    }

    public static String getStyleName(String styleId) {
        SidebarConfig config = STYLES.get(styleId);
        return config != null ? config.name : styleId;
    }

    public static void cleanupPlayer(UUID uuid) {
        PREVIOUS_LINES.remove(uuid);
    }

    public static String getDefaultStyleId() {
        return "off";
    }
}
