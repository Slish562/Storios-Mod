package com.io.storiosmod.client;

import com.io.storiosmod.cutscene.CutscenePoint;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import com.io.storiosmod.cutscene.InterpolationType;
import com.io.storiosmod.network.CutsceneExitEditorPacket;
import com.io.storiosmod.network.CutsceneSavePacket;
import com.io.storiosmod.network.PacketHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public class CutsceneEditorManager {

    private static CutsceneTimeline activeTimeline;
    private static int selectedIndex = -1;
    private static boolean editorActive = false;

    private static final Deque<UndoAction> undoStack = new ArrayDeque<>();
    private static final int MAX_UNDO = 50;

    private static String statusMessage = "";
    private static int statusTimer = 0;

    public static KeyMapping KEY_TOGGLE_PANEL;
    public static KeyMapping KEY_ADD_POINT;
    public static KeyMapping KEY_DELETE_POINT;
    public static KeyMapping KEY_NEXT_INTERP;
    public static KeyMapping KEY_EXIT_EDITOR;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KEY_TOGGLE_PANEL = new KeyMapping("key.storiosmod.cutscene_panel", GLFW.GLFW_KEY_G, "key.categories.storiosmod");
        KEY_ADD_POINT = new KeyMapping("key.storiosmod.cutscene_add_point", GLFW.GLFW_KEY_P, "key.categories.storiosmod");
        KEY_DELETE_POINT = new KeyMapping("key.storiosmod.cutscene_delete_point", GLFW.GLFW_KEY_DELETE, "key.categories.storiosmod");
        KEY_NEXT_INTERP = new KeyMapping("key.storiosmod.cutscene_interp", GLFW.GLFW_KEY_I, "key.categories.storiosmod");
        KEY_EXIT_EDITOR = new KeyMapping("key.storiosmod.cutscene_exit", GLFW.GLFW_KEY_RIGHT_BRACKET, "key.categories.storiosmod");
        event.register(KEY_TOGGLE_PANEL);
        event.register(KEY_ADD_POINT);
        event.register(KEY_DELETE_POINT);
        event.register(KEY_NEXT_INTERP);
        event.register(KEY_EXIT_EDITOR);
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(CutsceneEditorManager.class);
    }

    public static void enterEditor(CutsceneTimeline timeline) {
        activeTimeline = timeline;
        selectedIndex = -1;
        editorActive = true;
        undoStack.clear();
        CutscenePathRenderer.setEditorTimeline(timeline);
        setStatus("§aEditor mode — fly around, press P to add points");
    }

    public static void exitEditor() {
        editorActive = false;
        CutscenePathRenderer.clearEditorTimeline();
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CutsceneEditorScreen) {
            mc.setScreen(null);
        }
        PacketHandler.INSTANCE.sendToServer(new CutsceneExitEditorPacket());
        activeTimeline = null;
        selectedIndex = -1;
        undoStack.clear();
    }

    public static boolean isEditorActive() {
        return editorActive;
    }

    public static CutsceneTimeline getTimeline() {
        return activeTimeline;
    }

    public static int getSelectedIndex() {
        return selectedIndex;
    }

    public static void setSelectedIndex(int idx) {
        selectedIndex = idx;
        CutscenePathRenderer.setSelectedIndex(idx);
    }

    private static void setStatus(String msg) {
        statusMessage = msg;
        statusTimer = 60;
    }

    private static void pushUndo(UndoAction action) {
        if (undoStack.size() >= MAX_UNDO) {
            undoStack.removeLast();
        }
        undoStack.push(action);
    }

    private static void performUndo() {
        if (undoStack.isEmpty() || activeTimeline == null) return;
        UndoAction action = undoStack.pop();
        action.undo(activeTimeline);
        selectedIndex = Math.min(selectedIndex, activeTimeline.getPoints().size() - 1);
        CutscenePathRenderer.setSelectedIndex(selectedIndex);
        setStatus("§eUndo: " + action.description);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (statusTimer > 0) statusTimer--;

        if (!editorActive || activeTimeline == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean screenOpen = mc.screen instanceof CutsceneEditorScreen;

        if (KEY_ADD_POINT.consumeClick() && !screenOpen) {
            InterpolationType lastInterp = InterpolationType.LINEAR;
            if (!activeTimeline.getPoints().isEmpty()) {
                int refIdx = (selectedIndex >= 0 && selectedIndex < activeTimeline.getPoints().size())
                        ? selectedIndex : activeTimeline.getPoints().size() - 1;
                lastInterp = activeTimeline.getPoints().get(refIdx).getInterpolation();
            }
            CutscenePoint p = new CutscenePoint(
                    mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(),
                    mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(),
                    lastInterp, 60);
            int insertIdx;
            if (selectedIndex >= 0 && selectedIndex < activeTimeline.getPoints().size()) {
                insertIdx = selectedIndex + 1;
                activeTimeline.insertPoint(insertIdx, p);
            } else {
                activeTimeline.addPoint(p);
                insertIdx = activeTimeline.getPoints().size() - 1;
            }
            selectedIndex = insertIdx;
            CutscenePathRenderer.setSelectedIndex(selectedIndex);
            pushUndo(new UndoAction("added point #" + (insertIdx + 1), UndoType.ADD, insertIdx, p));
            setStatus("§a+ Point #" + (insertIdx + 1) + " inserted at " +
                    String.format("%.0f %.0f %.0f", p.getX(), p.getY(), p.getZ()));
        }

        if (KEY_DELETE_POINT.consumeClick() && !screenOpen) {
            if (selectedIndex >= 0 && selectedIndex < activeTimeline.getPoints().size()) {
                CutscenePoint removed = activeTimeline.getPoints().get(selectedIndex);
                pushUndo(new UndoAction("removed point #" + (selectedIndex + 1), UndoType.REMOVE, selectedIndex, removed));
                activeTimeline.removePoint(selectedIndex);
                if (selectedIndex >= activeTimeline.getPoints().size()) {
                    selectedIndex = activeTimeline.getPoints().size() - 1;
                }
                CutscenePathRenderer.setSelectedIndex(selectedIndex);
                setStatus("§c- Point removed");
            }
        }

        if (KEY_TOGGLE_PANEL.consumeClick()) {
            if (screenOpen) {
                mc.setScreen(null);
            } else {
                mc.setScreen(new CutsceneEditorScreen(activeTimeline));
            }
        }

        if (KEY_NEXT_INTERP.consumeClick() && !screenOpen) {
            if (selectedIndex >= 0 && selectedIndex < activeTimeline.getPoints().size()) {
                CutscenePoint p = activeTimeline.getPoints().get(selectedIndex);
                InterpolationType[] vals = InterpolationType.values();
                int next = (p.getInterpolation().ordinal() + 1) % vals.length;
                p.setInterpolation(vals[next]);
                setStatus("§bInterpolation → " + vals[next].name());
            }
        }

        if (KEY_EXIT_EDITOR.consumeClick() && !screenOpen) {
            long win = mc.getWindow().getWindow();
            boolean shiftHeld = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                    || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (shiftHeld && !activeTimeline.getName().isEmpty() && !activeTimeline.getPoints().isEmpty()) {
                PacketHandler.INSTANCE.sendToServer(new CutsceneSavePacket(activeTimeline));
                setStatus("§aSaved & exited editor");
            } else {
                setStatus("§cExited editor without saving");
            }
            exitEditor();
        }

        long window = mc.getWindow().getWindow();
        boolean ctrlHeld = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        if (ctrlHeld && !screenOpen) {
            boolean zPressed = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_Z) == GLFW.GLFW_PRESS;
            if (zPressed && undoCooldown <= 0) {
                performUndo();
                undoCooldown = 8;
            }
        }
        if (undoCooldown > 0) undoCooldown--;
    }

    private static int undoCooldown = 0;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (!editorActive || activeTimeline == null) return;
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CutsceneEditorScreen) return;

        GuiGraphics gui = event.getGuiGraphics();
        int sw = event.getWindow().getGuiScaledWidth();
        int sh = event.getWindow().getGuiScaledHeight();

        gui.fill(0, 0, sw, 32, 0x90000000);

        int y = 4;
        int points = activeTimeline.getPoints().size();
        String title = "§b§l✦ CUTSCENE EDITOR §r§8| §7" + activeTimeline.getName()
                + " §8| §f" + points + " pts";
        gui.drawString(mc.font, title, sw / 2 - mc.font.width(title) / 2, y, 0xFFFFFF);

        y += 12;
        if (selectedIndex >= 0 && selectedIndex < activeTimeline.getPoints().size()) {
            CutscenePoint sp = activeTimeline.getPoints().get(selectedIndex);
            String sel = "§ePoint #" + (selectedIndex + 1)
                    + " §f(" + String.format("%.1f", sp.getX())
                    + ", " + String.format("%.1f", sp.getY())
                    + ", " + String.format("%.1f", sp.getZ()) + ")"
                    + "  §7Yaw:" + String.format("%.0f", sp.getYaw())
                    + " Pitch:" + String.format("%.0f", sp.getPitch())
                    + "  §d" + sp.getInterpolation().name()
                    + "  §8" + sp.getDurationTicks() + "t";
            gui.drawString(mc.font, sel, sw / 2 - mc.font.width(sel) / 2, y, 0xFFFFFF);
        } else {
            String sel = "§7No point selected";
            gui.drawString(mc.font, sel, sw / 2 - mc.font.width(sel) / 2, y, 0xAAAAAA);
        }

        if (statusTimer > 0 && !statusMessage.isEmpty()) {
            int alpha = Math.min(255, statusTimer * 8);
            gui.drawString(mc.font, statusMessage, sw / 2 - mc.font.width(statusMessage) / 2, 36, (alpha << 24) | 0xFFFFFF);
        }

        gui.fill(0, sh - 16, sw, sh, 0x90000000);
        String hints = "§a[P]§7 Add  §c[Del]§7 Remove  §a[G]§7 Panel  §b[I]§7 Interp  §e[Ctrl+Z]§7 Undo  §6[Shift+]]§7 Save+Exit  §c[]]§7 Exit";
        gui.drawString(mc.font, hints, sw / 2 - mc.font.width(hints) / 2, sh - 12, 0xDDDDDD);

        int totalTicks = activeTimeline.getTotalDurationTicks();
        float totalSec = totalTicks / 20.0f;
        String info = "§8Duration: " + String.format("%.1fs", totalSec) + " (" + totalTicks + "t)  |  Undo: " + undoStack.size();
        gui.drawString(mc.font, info, 4, sh - 26, 0x999999);
    }

    private enum UndoType { ADD, REMOVE }

    private static class UndoAction {
        final String description;
        final UndoType type;
        final int index;
        final CutscenePoint point;

        UndoAction(String desc, UndoType type, int index, CutscenePoint point) {
            this.description = desc;
            this.type = type;
            this.index = index;
            this.point = point;
        }

        void undo(CutsceneTimeline timeline) {
            switch (type) {
                case ADD -> {
                    if (index < timeline.getPoints().size()) {
                        timeline.removePoint(index);
                    }
                }
                case REMOVE -> {
                    timeline.getPoints().add(Math.min(index, timeline.getPoints().size()), point);
                }
            }
        }
    }
}
