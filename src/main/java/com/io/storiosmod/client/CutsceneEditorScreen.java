package com.io.storiosmod.client;

import com.io.storiosmod.cutscene.CutscenePoint;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import com.io.storiosmod.cutscene.InterpolationType;
import com.io.storiosmod.network.CutsceneSavePacket;
import com.io.storiosmod.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CutsceneEditorScreen extends Screen {

    private final CutsceneTimeline timeline;
    private int selectedIndex = -1;

    private EditBox nameField;
    private EditBox xField, yField, zField;
    private EditBox yawField, pitchField;
    private EditBox durationField;
    private EditBox speedField, delayField;
    private Button interpButton;

    private static final int LEFT_W = 180;
    private static final int RIGHT_W = 200;
    private static final int MARGIN = 8;
    private static final int ROW_H = 18;
    private static final int BTN_H = 20;
    private static final int FIELD_H = 16;
    private static final int HEADER_H = 22;

    private int scrollOffset = 0;

    private CutsceneGizmo.Axis draggingAxis = CutsceneGizmo.Axis.NONE;
    private double dragStartOffset;
    private Vec3 dragStartPoint;
    private boolean dragging = false;
    private boolean orbitDragging = false;
    private double orbitLastX, orbitLastY;
    private static final float ORBIT_SENSITIVITY = 0.25f;
    private static final float MOVE_SPEED = 0.35f;

    private final List<UndoState> undoStack = new ArrayList<>();
    private static final int MAX_UNDO = 50;

    private record UndoState(List<CutscenePoint> points, int selectedIndex) {
        static UndoState capture(CutsceneTimeline tl, int sel) {
            List<CutscenePoint> copy = new ArrayList<>();
            for (CutscenePoint p : tl.getPoints()) {
                copy.add(new CutscenePoint(p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch(), p.getInterpolation(), p.getDurationTicks(), p.getSpeed(), p.getDelayTicks()));
            }
            return new UndoState(copy, sel);
        }
    }

    private void pushUndo() {
        if (undoStack.size() >= MAX_UNDO) undoStack.remove(0);
        undoStack.add(UndoState.capture(timeline, selectedIndex));
    }

    private void popUndo() {
        if (undoStack.isEmpty()) return;
        UndoState state = undoStack.remove(undoStack.size() - 1);
        timeline.getPoints().clear();
        timeline.getPoints().addAll(state.points());
        selectedIndex = state.selectedIndex();
        CutsceneEditorManager.setSelectedIndex(selectedIndex);
        refreshProperties();
    }

    private void addPointAtPlayer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            pushUndo();
            InterpolationType lastInterp = InterpolationType.LINEAR;
            if (!timeline.getPoints().isEmpty()) {
                int refIdx = (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size())
                        ? selectedIndex : timeline.getPoints().size() - 1;
                lastInterp = timeline.getPoints().get(refIdx).getInterpolation();
            }
            CutscenePoint p = new CutscenePoint(
                    mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(),
                    mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot(),
                    lastInterp, 60);
            int insertIdx;
            if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
                insertIdx = selectedIndex + 1;
                timeline.insertPoint(insertIdx, p);
            } else {
                timeline.addPoint(p);
                insertIdx = timeline.getPoints().size() - 1;
            }
            selectedIndex = insertIdx;
            CutsceneEditorManager.setSelectedIndex(selectedIndex);
            refreshProperties();
        }
    }

    public CutsceneEditorScreen(CutsceneTimeline timeline) {
        super(Component.literal("Cutscene Editor"));
        this.timeline = timeline;
        this.selectedIndex = CutsceneEditorManager.getSelectedIndex();
    }

    @Override
    protected void init() {
        CutscenePathRenderer.setEditorTimeline(timeline);

        int leftX = MARGIN;

        nameField = new EditBox(font, leftX, HEADER_H + 4, LEFT_W - MARGIN * 2, FIELD_H,
                Component.literal("Name"));
        nameField.setMaxLength(64);
        nameField.setValue(timeline.getName());
        nameField.setResponder(timeline::setName);
        addRenderableWidget(nameField);

        int btnY = HEADER_H + 4 + FIELD_H + 4;
        int btnW = (LEFT_W - MARGIN * 2 - 4) / 2;

        addRenderableWidget(Button.builder(Component.literal("§a+ Add"), btn -> addPointAtPlayer())
                .bounds(leftX, btnY, btnW, BTN_H).build());

        addRenderableWidget(Button.builder(Component.literal("§c- Del"), btn -> {
            if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
                pushUndo();
                timeline.removePoint(selectedIndex);
                if (selectedIndex >= timeline.getPoints().size()) {
                    selectedIndex = timeline.getPoints().size() - 1;
                }
                CutsceneEditorManager.setSelectedIndex(selectedIndex);
                refreshProperties();
            }
        }).bounds(leftX + btnW + 4, btnY, btnW, BTN_H).build());

        btnY += BTN_H + 2;
        addRenderableWidget(Button.builder(Component.literal("▲ Up"), btn -> {
            if (selectedIndex > 0) {
                timeline.movePointUp(selectedIndex);
                selectedIndex--;
                CutsceneEditorManager.setSelectedIndex(selectedIndex);
            }
        }).bounds(leftX, btnY, btnW, BTN_H).build());

        addRenderableWidget(Button.builder(Component.literal("▼ Down"), btn -> {
            if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size() - 1) {
                timeline.movePointDown(selectedIndex);
                selectedIndex++;
                CutsceneEditorManager.setSelectedIndex(selectedIndex);
            }
        }).bounds(leftX + btnW + 4, btnY, btnW, BTN_H).build());

        btnY += BTN_H + 2;
        addRenderableWidget(Button.builder(Component.literal("§b⦿ Teleport"), btn -> {
            if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    CutscenePoint p = timeline.getPoints().get(selectedIndex);
                    mc.player.setPos(p.getX(), p.getY() - mc.player.getEyeHeight(), p.getZ());
                    mc.player.setYRot((float) p.getYaw());
                    mc.player.setXRot((float) p.getPitch());
                }
            }
        }).bounds(leftX, btnY, LEFT_W - MARGIN * 2, BTN_H).build());

        int rightX = width - RIGHT_W + MARGIN;
        int rY = HEADER_H + 4;
        int labelW = 35;
        int fieldW = RIGHT_W - MARGIN * 2 - labelW - 4;

        xField = addField(rightX + labelW + 4, rY, fieldW, "X");
        rY += ROW_H + 2;
        yField = addField(rightX + labelW + 4, rY, fieldW, "Y");
        rY += ROW_H + 2;
        zField = addField(rightX + labelW + 4, rY, fieldW, "Z");
        rY += ROW_H + 2;
        yawField = addField(rightX + labelW + 4, rY, fieldW, "Yaw");
        rY += ROW_H + 2;
        pitchField = addField(rightX + labelW + 4, rY, fieldW, "Pitch");
        rY += ROW_H + 2;
        durationField = addField(rightX + labelW + 4, rY, fieldW, "Ticks");
        rY += ROW_H + 2;
        speedField = addField(rightX + labelW + 4, rY, fieldW, "Speed");
        rY += ROW_H + 2;
        delayField = addField(rightX + labelW + 4, rY, fieldW, "Delay");
        rY += ROW_H + 4;

        interpButton = Button.builder(Component.literal("LINEAR"), btn -> {
            if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
                CutscenePoint p = timeline.getPoints().get(selectedIndex);
                InterpolationType[] vals = InterpolationType.values();
                int next = (p.getInterpolation().ordinal() + 1) % vals.length;
                p.setInterpolation(vals[next]);
                interpButton.setMessage(Component.literal("§d" + vals[next].name()));
            }
        }).bounds(rightX, rY, RIGHT_W - MARGIN * 2, BTN_H).build();
        addRenderableWidget(interpButton);

        rY += BTN_H + 4;

        int halfW = (RIGHT_W - MARGIN * 2 - 4) / 2;
        addRenderableWidget(Button.builder(Component.literal("§a✓ Apply"), btn -> applyFields())
                .bounds(rightX, rY, halfW, BTN_H).build());

        addRenderableWidget(Button.builder(Component.literal("§e↻ Here"), btn -> {
            if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    CutscenePoint p = timeline.getPoints().get(selectedIndex);
                    p.setX(mc.player.getX());
                    p.setY(mc.player.getY() + mc.player.getEyeHeight());
                    p.setZ(mc.player.getZ());
                    p.setYaw(mc.player.getYRot());
                    p.setPitch(mc.player.getXRot());
                    refreshProperties();
                }
            }
        }).bounds(rightX + halfW + 4, rY, halfW, BTN_H).build());

        int bottomY = height - BTN_H - MARGIN;
        int bw = (width - LEFT_W - RIGHT_W - MARGIN * 2) / 6;
        if (bw < 50) bw = 50;
        int bx = LEFT_W + MARGIN;

        addRenderableWidget(Button.builder(Component.literal("§b▶ Preview"), btn -> {
            applyFields();
            if (timeline.getPoints().size() >= 2) {
                onClose();
                CutsceneCameraHandler.startPreview(timeline);
            }
        }).bounds(bx, bottomY, bw, BTN_H).build());
        bx += bw + 2;

        addRenderableWidget(Button.builder(Component.literal("§a💾 Save"), btn -> {
            applyFields();
            if (!timeline.getName().isEmpty()) {
                PacketHandler.INSTANCE.sendToServer(new CutsceneSavePacket(timeline));
            }
        }).bounds(bx, bottomY, bw, BTN_H).build());
        bx += bw + 2;

        addRenderableWidget(Button.builder(Component.literal("§c✕ Exit"), btn -> onClose())
                .bounds(bx, bottomY, bw, BTN_H).build());

        refreshProperties();
    }

    private EditBox addField(int x, int y, int w, String hint) {
        EditBox box = new EditBox(font, x, y, w, FIELD_H, Component.literal(hint));
        box.setMaxLength(32);
        addRenderableWidget(box);
        return box;
    }

    private void refreshProperties() {
        if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
            CutscenePoint p = timeline.getPoints().get(selectedIndex);
            xField.setValue(String.format("%.2f", p.getX()));
            yField.setValue(String.format("%.2f", p.getY()));
            zField.setValue(String.format("%.2f", p.getZ()));
            yawField.setValue(String.format("%.1f", p.getYaw()));
            pitchField.setValue(String.format("%.1f", p.getPitch()));
            durationField.setValue(String.valueOf(p.getDurationTicks()));
            speedField.setValue(String.format("%.1f", p.getSpeed()));
            delayField.setValue(String.valueOf(p.getDelayTicks()));
            interpButton.setMessage(Component.literal("§d" + p.getInterpolation().name()));
        } else {
            xField.setValue("");
            yField.setValue("");
            zField.setValue("");
            yawField.setValue("");
            pitchField.setValue("");
            durationField.setValue("");
            speedField.setValue("");
            delayField.setValue("");
            interpButton.setMessage(Component.literal("§8---"));
        }
    }

    private void applyFields() {
        if (selectedIndex < 0 || selectedIndex >= timeline.getPoints().size()) return;
        CutscenePoint p = timeline.getPoints().get(selectedIndex);
        try { p.setX(Double.parseDouble(xField.getValue())); } catch (NumberFormatException ignored) {}
        try { p.setY(Double.parseDouble(yField.getValue())); } catch (NumberFormatException ignored) {}
        try { p.setZ(Double.parseDouble(zField.getValue())); } catch (NumberFormatException ignored) {}
        try { p.setYaw(Float.parseFloat(yawField.getValue())); } catch (NumberFormatException ignored) {}
        try { p.setPitch(Float.parseFloat(pitchField.getValue())); } catch (NumberFormatException ignored) {}
        try { p.setDurationTicks(Integer.parseInt(durationField.getValue())); } catch (NumberFormatException ignored) {}
        try { p.setSpeed(Float.parseFloat(speedField.getValue())); } catch (NumberFormatException ignored) {}
        try { p.setDelayTicks(Integer.parseInt(delayField.getValue())); } catch (NumberFormatException ignored) {}
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        boolean isEditingField = nameField.isFocused() || xField.isFocused() || yField.isFocused() || zField.isFocused()
                || yawField.isFocused() || pitchField.isFocused() || durationField.isFocused()
                || speedField.isFocused() || delayField.isFocused();

        if (!isEditingField) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                long window = mc.getWindow().getWindow();
                boolean w = com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, GLFW.GLFW_KEY_W);
                boolean s = com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, GLFW.GLFW_KEY_S);
                boolean a = com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, GLFW.GLFW_KEY_A);
                boolean dKey = com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, GLFW.GLFW_KEY_D);
                boolean space = com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, GLFW.GLFW_KEY_SPACE);
                boolean shift = com.mojang.blaze3d.platform.InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT);

                if (w || s || a || dKey || space || shift) {
                    float yaw = mc.player.getYRot();
                    double yawRad = Math.toRadians(yaw);
                    double forwardX = -Math.sin(yawRad);
                    double forwardZ = Math.cos(yawRad);
                    double rightX = -Math.cos(yawRad);
                    double rightZ = -Math.sin(yawRad);

                    float speed = MOVE_SPEED * 0.15f;
                    double mx = 0, my = 0, mz = 0;
                    if (w) { mx += forwardX * speed; mz += forwardZ * speed; }
                    if (s) { mx -= forwardX * speed; mz -= forwardZ * speed; }
                    if (a) { mx -= rightX * speed; mz -= rightZ * speed; }
                    if (dKey) { mx += rightX * speed; mz += rightZ * speed; }
                    if (space) { my += speed; }
                    if (shift) { my -= speed; }

                    mc.player.setPos(mc.player.getX() + mx, mc.player.getY() + my, mc.player.getZ() + mz);
                    mc.player.xOld = mc.player.getX();
                    mc.player.yOld = mc.player.getY();
                    mc.player.zOld = mc.player.getZ();
                    mc.player.xo = mc.player.getX();
                    mc.player.yo = mc.player.getY();
                    mc.player.zo = mc.player.getZ();
                    mc.player.getAbilities().flying = true;
                }
            }
        }

        if (!dragging && mouseX > LEFT_W && mouseX < width - RIGHT_W) {
            if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
                CutsceneGizmo.Axis hovered = CutsceneGizmo.pickAxis(mouseX, mouseY,
                        timeline.getPoints().get(selectedIndex).getPos());
                CutscenePathRenderer.setHoveredAxis(hovered);
            } else {
                CutscenePathRenderer.setHoveredAxis(CutsceneGizmo.Axis.NONE);
            }
        }

        g.fill(0, 0, LEFT_W, height, 0xE0101020);
        g.fill(LEFT_W - 1, 0, LEFT_W, height, 0xFF303060);

        g.fill(width - RIGHT_W, 0, width, height, 0xE0101020);
        g.fill(width - RIGHT_W, 0, width - RIGHT_W + 1, height, 0xFF303060);

        g.fill(0, 0, LEFT_W, HEADER_H, 0xFF1a1a3a);
        g.drawString(font, "§b§l✦ §rPoints §7(" + timeline.getPoints().size() + ")", MARGIN, 7, 0xFFCCDDFF);

        g.fill(width - RIGHT_W, 0, width, HEADER_H, 0xFF1a1a3a);
        String propTitle = selectedIndex >= 0 ? "§e§lProperties §r§7#" + (selectedIndex + 1) : "§7Properties";
        g.drawString(font, propTitle, width - RIGHT_W + MARGIN, 7, 0xFFCCDDFF);

        List<CutscenePoint> points = timeline.getPoints();
        int listStartY = HEADER_H + 4 + FIELD_H + 4 + (BTN_H + 2) * 3 + 6;
        int listEndY = height - MARGIN;
        int maxVisible = (listEndY - listStartY) / (ROW_H + 2);

        for (int i = scrollOffset; i < points.size() && i < scrollOffset + maxVisible; i++) {
            int py = listStartY + (i - scrollOffset) * (ROW_H + 2);
            boolean sel = (i == selectedIndex);

            if (sel) {
                g.fill(MARGIN - 2, py - 1, LEFT_W - MARGIN + 2, py + ROW_H + 1, 0xFF4040AA);
                g.fill(MARGIN - 1, py, LEFT_W - MARGIN + 1, py + ROW_H, 0xFF2a2a6e);
            } else {
                g.fill(MARGIN, py, LEFT_W - MARGIN, py + ROW_H, 0xFF1e1e38);
            }

            String idx = String.valueOf(i + 1);
            g.fill(MARGIN + 2, py + 2, MARGIN + 16, py + ROW_H - 2, sel ? 0xFF5555CC : 0xFF333355);
            g.drawCenteredString(font, idx, MARGIN + 9, py + 5, sel ? 0xFFFFFF : 0xBBBBBB);

            CutscenePoint pt = points.get(i);
            String coords = String.format("%.0f, %.0f, %.0f", pt.getX(), pt.getY(), pt.getZ());
            g.drawString(font, coords, MARGIN + 20, py + 5, sel ? 0xFFFFDD : 0xAAAAAA);
        }

        if (selectedIndex >= 0) {
            int rightX = width - RIGHT_W + MARGIN;
            int rY = HEADER_H + 4;
            String[] labels = {"§9X:", "§aY:", "§cZ:", "§eYaw:", "§dPitch:", "§7Dur:", "§6Spd:", "§3Dly:"};
            for (String label : labels) {
                g.drawString(font, label, rightX, rY + 4, 0xFFCCCCCC);
                rY += ROW_H + 2;
            }
        }

        if (selectedIndex >= 0) {
            int totalTicks = timeline.getTotalDurationTicks();
            float totalSec = totalTicks / 20.0f;
            String dur = "§8Duration: §f" + String.format("%.1fs", totalSec) + " §8(" + totalTicks + "t)";
            g.drawString(font, dur, width - RIGHT_W + MARGIN, height - MARGIN - BTN_H - 14, 0xFFAAAAAA);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    private void unfocusAllFields() {
        nameField.setFocused(false);
        xField.setFocused(false);
        yField.setFocused(false);
        zField.setFocused(false);
        yawField.setFocused(false);
        pitchField.setFocused(false);
        durationField.setFocused(false);
        speedField.setFocused(false);
        delayField.setFocused(false);
        this.setFocused(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        int listStartY = HEADER_H + 4 + FIELD_H + 4 + (BTN_H + 2) * 3 + 6;
        if (mouseX < LEFT_W && mouseY >= listStartY) {
            int idx = scrollOffset + (int) ((mouseY - listStartY) / (ROW_H + 2));
            if (idx >= 0 && idx < timeline.getPoints().size()) {
                applyFields();
                selectedIndex = idx;
                CutsceneEditorManager.setSelectedIndex(selectedIndex);
                refreshProperties();
                unfocusAllFields();
                return true;
            }
        }

        if (mouseX > LEFT_W && mouseX < width - RIGHT_W) {
            unfocusAllFields();
        }

        if (button == 1 && mouseX > LEFT_W && mouseX < width - RIGHT_W) {
            orbitDragging = true;
            orbitLastX = mouseX;
            orbitLastY = mouseY;
            return true;
        }

        if (button == 0 && mouseX > LEFT_W && mouseX < width - RIGHT_W) {

            if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
                Vec3 pointPos = timeline.getPoints().get(selectedIndex).getPos();
                CutsceneGizmo.Axis axis = CutsceneGizmo.pickAxis(mouseX, mouseY, pointPos);
                if (axis != CutsceneGizmo.Axis.NONE) {
                    Minecraft mc = Minecraft.getInstance();
                    Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
                    Vec3 rayDir = CutsceneGizmo.screenToWorldRay(mouseX, mouseY);
                    Vec3 axisDir = CutsceneGizmo.getAxisDir(axis);

                    draggingAxis = axis;
                    dragging = true;
                    dragStartPoint = pointPos;
                    dragStartOffset = CutsceneGizmo.rayLineClosestParam(camPos, rayDir, dragStartPoint, axisDir);
                    return true;
                }
            }

            List<CutscenePoint> points = timeline.getPoints();
            int picked = CutsceneGizmo.pickPoint(mouseX, mouseY, points);
            if (picked >= 0) {
                applyFields();
                selectedIndex = picked;
                CutsceneEditorManager.setSelectedIndex(selectedIndex);
                CutscenePathRenderer.setSelectedIndex(selectedIndex);
                refreshProperties();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {

        if (orbitDragging && button == 1) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                double dx = mouseX - orbitLastX;
                double dy = mouseY - orbitLastY;
                orbitLastX = mouseX;
                orbitLastY = mouseY;
                float newYaw = mc.player.getYRot() + (float) dx * ORBIT_SENSITIVITY;
                float newPitch = Math.max(-90, Math.min(90, mc.player.getXRot() + (float) dy * ORBIT_SENSITIVITY));

                mc.player.setYRot(newYaw);
                mc.player.setXRot(newPitch);
            }
            return true;
        }

        if (dragging && draggingAxis != CutsceneGizmo.Axis.NONE
                && selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
            Minecraft mc = Minecraft.getInstance();
            CutscenePoint p = timeline.getPoints().get(selectedIndex);
            Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
            Vec3 rayDir = CutsceneGizmo.screenToWorldRay(mouseX, mouseY);
            Vec3 axisDir = CutsceneGizmo.getAxisDir(draggingAxis);

            double currentParam = CutsceneGizmo.rayLineClosestParam(camPos, rayDir, dragStartPoint, axisDir);
            double delta = currentParam - dragStartOffset;

            Vec3 newPos = dragStartPoint.add(axisDir.scale(delta));
            p.setX(newPos.x);
            p.setY(newPos.y);
            p.setZ(newPos.z);
            refreshProperties();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (orbitDragging && button == 1) {
            orbitDragging = false;
            return true;
        }
        if (dragging && button == 0) {
            dragging = false;
            draggingAxis = CutsceneGizmo.Axis.NONE;
            CutscenePathRenderer.setHoveredAxis(CutsceneGizmo.Axis.NONE);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (mouseX < LEFT_W) {
            scrollOffset = Math.max(0, scrollOffset - (int) delta);
            int maxScroll = Math.max(0, timeline.getPoints().size() - 5);
            scrollOffset = Math.min(scrollOffset, maxScroll);
            return true;
        }

        if (mouseX > LEFT_W && mouseX < width - RIGHT_W) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                float yaw = mc.player.getYRot();
                float pitch = mc.player.getXRot();
                double yawRad = Math.toRadians(yaw);
                double pitchRad = Math.toRadians(pitch);
                double dx = -Math.sin(yawRad) * Math.cos(pitchRad) * delta * 1.5;
                double dy = -Math.sin(pitchRad) * delta * 1.5;
                double dz = Math.cos(yawRad) * Math.cos(pitchRad) * delta * 1.5;
                mc.player.setPos(mc.player.getX() + dx, mc.player.getY() + dy, mc.player.getZ() + dz);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean isEditing = nameField.isFocused() || xField.isFocused() || yField.isFocused() || zField.isFocused()
                || yawField.isFocused() || pitchField.isFocused() || durationField.isFocused()
                || speedField.isFocused() || delayField.isFocused();

        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;

        if (ctrl && keyCode == GLFW.GLFW_KEY_Z) {
            popUndo();
            return true;
        }

        if (ctrl && keyCode == GLFW.GLFW_KEY_N) {
            addPointAtPlayer();
            return true;
        }

        if (!isEditing && keyCode == GLFW.GLFW_KEY_DELETE) {
            if (selectedIndex >= 0 && selectedIndex < timeline.getPoints().size()) {
                pushUndo();
                timeline.removePoint(selectedIndex);
                if (selectedIndex >= timeline.getPoints().size()) selectedIndex = timeline.getPoints().size() - 1;
                CutsceneEditorManager.setSelectedIndex(selectedIndex);
                refreshProperties();
            }
            return true;
        }

        if (!isEditing && (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_W
                || keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_S || keyCode == GLFW.GLFW_KEY_D
                || keyCode == GLFW.GLFW_KEY_LEFT_SHIFT)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        boolean isEditing = nameField.isFocused() || xField.isFocused() || yField.isFocused() || zField.isFocused()
                || yawField.isFocused() || pitchField.isFocused() || durationField.isFocused()
                || speedField.isFocused() || delayField.isFocused();
        if (!isEditing && (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_W
                || keyCode == GLFW.GLFW_KEY_A || keyCode == GLFW.GLFW_KEY_S || keyCode == GLFW.GLFW_KEY_D
                || keyCode == GLFW.GLFW_KEY_LEFT_SHIFT)) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        applyFields();
        super.onClose();
    }
}
