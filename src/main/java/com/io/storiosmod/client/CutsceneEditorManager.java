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
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
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
        NeoForge.EVENT_BUS.register(CutsceneEditorManager.class);
    }

    public static void enterEditor(CutsceneTimeline timeline) {
        activeTimeline = timeline;
        selectedIndex = -1;
        editorActive = true;
        undoStack.clear();
        CutscenePathRenderer.setEditorTimeline(timeline);
        setStatus("\u00A7aEditor mode \u2014 fly around, press P to add points");
    }

    public static void exitEditor() {
        editorActive = false;
        CutscenePathRenderer.clearEditorTimeline();
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CutsceneEditorScreen) {
            mc.setScreen(null);
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new CutsceneExitEditorPacket());
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
        setStatus("\u00A7eUndo: " + action.description);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

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
            setStatus("\u00A7a+ Point #" + (insertIdx + 1) + " inserted at " +
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
                setStatus("\u00A7c- Point removed");
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
                setStatus("\u00A7bInterpolation \u2192 " + vals[next].name());
            }
        }

        if (KEY_EXIT_EDITOR.consumeClick() && !screenOpen) {
            long win = mc.getWindow().getWindow();
            boolean shiftHeld = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                    || GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (shiftHeld && !activeTimeline.getName().isEmpty() && !activeTimeline.getPoints().isEmpty()) {
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(new CutsceneSavePacket(activeTimeline));
                setStatus("\u00A7aSaved & exited editor");
            } else {
                setStatus("\u00A7cExited editor without saving");
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
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        if (!editorActive || activeTimeline == null) return;
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CutsceneEditorScreen) return;

        GuiGraphics gui = event.getGuiGraphics();
        int sw = gui.guiWidth();
        int sh = gui.guiHeight();

        gui.fill(0, 0, sw, 32, 0x90000000);

        int y = 4;
        int points = activeTimeline.getPoints().size();
        String title = "\u00A7b\u00A7l\u2726 CUTSCENE EDITOR \u00A7r\u00A78| \u00A77" + activeTimeline.getName()
                + " \u00A78| \u00A7f" + points + " pts";
        gui.drawString(mc.font, title, sw / 2 - mc.font.width(title) / 2, y, 0xFFFFFF);

        y += 12;
        if (selectedIndex >= 0 && selectedIndex < activeTimeline.getPoints().size()) {
            CutscenePoint sp = activeTimeline.getPoints().get(selectedIndex);
            String sel = "\u00A7ePoint #" + (selectedIndex + 1)
                    + " \u00A7f(" + String.format("%.1f", sp.getX())
                    + ", " + String.format("%.1f", sp.getY())
                    + ", " + String.format("%.1f", sp.getZ()) + ")"
                    + "  \u00A77Yaw:" + String.format("%.0f", sp.getYaw())
                    + " Pitch:" + String.format("%.0f", sp.getPitch())
                    + "  \u00A7d" + sp.getInterpolation().name()
                    + "  \u00A78" + sp.getDurationTicks() + "t";
            gui.drawString(mc.font, sel, sw / 2 - mc.font.width(sel) / 2, y, 0xFFFFFF);
        } else {
            String sel = "\u00A77No point selected";
            gui.drawString(mc.font, sel, sw / 2 - mc.font.width(sel) / 2, y, 0xAAAAAA);
        }

        if (statusTimer > 0 && !statusMessage.isEmpty()) {
            int alpha = Math.min(255, statusTimer * 8);
            gui.drawString(mc.font, statusMessage, sw / 2 - mc.font.width(statusMessage) / 2, 36, (alpha << 24) | 0xFFFFFF);
        }

        gui.fill(0, sh - 16, sw, sh, 0x90000000);
        String hints = "\u00A7a[P]\u00A77 Add  \u00A7c[Del]\u00A77 Remove  \u00A7a[G]\u00A77 Panel  \u00A7b[I]\u00A77 Interp  \u00A7e[Ctrl+Z]\u00A77 Undo  \u00A76[Shift+]]\u00A77 Save+Exit  \u00A7c[]]\u00A77 Exit";
        gui.drawString(mc.font, hints, sw / 2 - mc.font.width(hints) / 2, sh - 12, 0xDDDDDD);

        int totalTicks = activeTimeline.getTotalDurationTicks();
        float totalSec = totalTicks / 20.0f;
        String info = "\u00A78Duration: " + String.format("%.1fs", totalSec) + " (" + totalTicks + "t)  |  Undo: " + undoStack.size();
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


