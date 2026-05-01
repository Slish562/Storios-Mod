package com.io.storiosmod.client;

import com.io.storiosmod.block.entity.RadiusTriggerBlockEntity;
import com.io.storiosmod.network.RadiusTriggerResetPacket;
import com.io.storiosmod.network.RadiusTriggerSavePacket;
import com.io.storiosmod.block.entity.RadiusTriggerBlockEntity.TriggerMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class RadiusTriggerScreen extends Screen {
    private final RadiusTriggerBlockEntity blockEntity;
    private EditBox radiusField;
    private TriggerMode currentMode;
    private final List<ActionEntry> actionEntries = new ArrayList<>();
    private int scrollOffset = 0;

    private static final int PANEL_W = 320;
    private static final int ROW_H = 20;
    private static final int BTN_H = 20;
    private static final int MARGIN = 10;
    private static final int HEADER_H = 24;

    public static class ActionEntry {
        public String command;

        public ActionEntry(String command) {
            this.command = command;
        }
    }

    public RadiusTriggerScreen(RadiusTriggerBlockEntity blockEntity) {
        super(Component.literal("Radius Trigger Editor"));
        this.blockEntity = blockEntity;
        this.currentMode = blockEntity.getTriggerMode();
        for (String cmd : blockEntity.getCommands()) {
            actionEntries.add(new ActionEntry(cmd));
        }
    }

    @Override
    protected void init() {
        int left = (width - PANEL_W) / 2;
        int top = 20;

        radiusField = new EditBox(font, left + 60, top + HEADER_H + 4, 80, 16, Component.literal("Radius"));
        radiusField.setValue(String.valueOf(blockEntity.getRadius()));
        addRenderableWidget(radiusField);

        addRenderableWidget(Button.builder(
                Component.literal("Mode: " + (currentMode == TriggerMode.ONCE ? "\u00A7aOnce" : "\u00A7eEvery Entry")),
                btn -> {
                    currentMode = currentMode == TriggerMode.ONCE ? TriggerMode.EVERY_ENTRY : TriggerMode.ONCE;
                    btn.setMessage(Component
                            .literal("Mode: "
                                    + (currentMode == TriggerMode.ONCE ? "\u00A7aOnce" : "\u00A7eEvery Entry")));
                }).bounds(left + 150, top + HEADER_H + 2, 160, BTN_H).build());

        int row2Y = top + HEADER_H + 4 + ROW_H + 4;

        addRenderableWidget(Button.builder(Component.literal("\u00A7a+ Add Command"), btn -> {
            actionEntries.add(new ActionEntry(""));
            rebuildScreen();
        }).bounds(left, row2Y, 110, BTN_H).build());

        addRenderableWidget(Button.builder(Component.literal("\u00A7e\u21BB Reset Triggered"), btn -> {
            net.neoforged.neoforge.network.PacketDistributor
                    .sendToServer(new RadiusTriggerResetPacket(blockEntity.getBlockPos()));
        }).bounds(left + 114, row2Y, 120, BTN_H).build());

        int bottomY = height - BTN_H - MARGIN;
        int bw = PANEL_W / 2;
        addRenderableWidget(Button.builder(Component.literal("\u00A7a\uD83D\uDCBE Save"), btn -> {
            save();
            onClose();
        })
                .bounds(left, bottomY, bw - 2, BTN_H).build());

        addRenderableWidget(Button.builder(Component.literal("\u00A7c\u2716 Close"), btn -> onClose())
                .bounds(left + bw + 2, bottomY, bw - 2, BTN_H).build());
    }

    private void rebuildScreen() {
        String currentRadius = radiusField != null ? radiusField.getValue()
                : String.valueOf(blockEntity.getRadius());
        clearWidgets();
        init();
        radiusField.setValue(currentRadius);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        int left = (width - PANEL_W) / 2;
        int top = 20;

        g.fill(left - 4, top - 4, left + PANEL_W + 4, height - 4, 0xE0101020);
        g.fill(left - 4, top - 4, left + PANEL_W + 4, top + HEADER_H, 0xFF1a1a3a);
        g.drawCenteredString(font, "\u00A7b\u00A7l\u29BF \u00A7rRadius Trigger \u00A77[" +
                blockEntity.getBlockPos().getX() + ", " +
                blockEntity.getBlockPos().getY() + ", " +
                blockEntity.getBlockPos().getZ() + "]", width / 2, top + 4, 0xFFCCDDFF);

        g.drawString(font, "\u00A79Radius:", left + 4, top + HEADER_H + 8, 0xFFCCCCCC);
        int actionsStartY = top + HEADER_H + 4 + ROW_H + 4 + BTN_H + 8;
        g.drawString(font, "\u00A7e\u00A7lCommands \u00A77(" + actionEntries.size() + ")", left + 4, actionsStartY,
                0xFFCCDDFF);

        int listY = actionsStartY + 14;
        int maxVisible = (height - BTN_H - MARGIN - 4 - listY) / (ROW_H + 2);
        for (int i = scrollOffset; i < actionEntries.size() && i < scrollOffset + maxVisible; i++) {
            int rowY = listY + (i - scrollOffset) * (ROW_H + 2);
            ActionEntry entry = actionEntries.get(i);
            g.fill(left, rowY, left + PANEL_W, rowY + ROW_H, 0xFF1e1e38);
            String val = entry.command;
            if (val.length() > 38)
                val = val.substring(0, 35) + "...";
            g.drawString(font, "\u00A7f" + val, left + 4, rowY + 6, 0xFFCCCCCC);
            int btnX = left + PANEL_W - 66;
            if (mouseX >= btnX && mouseX <= btnX + 30 && mouseY >= rowY && mouseY < rowY + ROW_H)
                g.fill(btnX, rowY, btnX + 30, rowY + ROW_H, 0x40FFFF00);
            g.drawString(font, "\u00A7e\u270E", btnX + 8, rowY + 6, 0xFFFFFFFF);
            int delX = left + PANEL_W - 30;
            if (mouseX >= delX && mouseX <= delX + 26 && mouseY >= rowY && mouseY < rowY + ROW_H)
                g.fill(delX, rowY, delX + 26, rowY + ROW_H, 0x40FF0000);
            g.drawString(font, "\u00A7c\u2716", delX + 8, rowY + 6, 0xFFFFFFFF);
        }
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = (width - PANEL_W) / 2;
            int top = 20;
            int actionsStartY = top + HEADER_H + 4 + ROW_H + 4 + BTN_H + 8;
            int listY = actionsStartY + 14;
            int maxVisible = (height - BTN_H - MARGIN - 4 - listY) / (ROW_H + 2);
            for (int i = scrollOffset; i < actionEntries.size() && i < scrollOffset + maxVisible; i++) {
                int rowY = listY + (i - scrollOffset) * (ROW_H + 2);
                ActionEntry entry = actionEntries.get(i);
                int editX = left + PANEL_W - 66;
                if (mouseX >= editX && mouseX <= editX + 30 && mouseY >= rowY && mouseY < rowY + ROW_H) {
                    minecraft.setScreen(new ActionEditScreen(this, entry));
                    return true;
                }
                int delX = left + PANEL_W - 30;
                if (mouseX >= delX && mouseX <= delX + 26 && mouseY >= rowY && mouseY < rowY + ROW_H) {
                    actionEntries.remove(i);
                    rebuildScreen();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void save() {
        int r;
        try {
            r = (int) Double.parseDouble(radiusField.getValue());
        } catch (Exception e) {
            r = blockEntity.getRadius();
        }
        List<String> commands = new ArrayList<>();
        for (ActionEntry e : actionEntries) {
            if (!e.command.trim().isEmpty())
                commands.add(e.command.trim());
        }
        net.neoforged.neoforge.network.PacketDistributor
                .sendToServer(new RadiusTriggerSavePacket(blockEntity.getBlockPos(), r, currentMode, commands));
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(g);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class ActionEditScreen extends Screen {
        private final RadiusTriggerScreen parent;
        private final ActionEntry entry;
        private EditBox valueField;
        private net.minecraft.client.gui.components.CommandSuggestions commandSuggestions;

        ActionEditScreen(RadiusTriggerScreen parent, ActionEntry entry) {
            super(Component.literal("Edit Command"));
            this.parent = parent;
            this.entry = entry;
        }

        @Override
        protected void init() {
            int cx = width / 2;
            int cy = height / 2;
            valueField = new EditBox(font, cx - 150, cy - 10, 300, 20, Component.literal("Command"));
            valueField.setMaxLength(256);
            String initValue = entry.command;
            if (!initValue.startsWith("/"))
                initValue = "/" + initValue;
            valueField.setValue(initValue);
            addRenderableWidget(valueField);
            setInitialFocus(valueField);
            commandSuggestions = new net.minecraft.client.gui.components.CommandSuggestions(minecraft, this, valueField,
                    font, true, true, 0, 7, false, Integer.MIN_VALUE);
            commandSuggestions.setAllowSuggestions(true);
            commandSuggestions.updateCommandInfo();
            valueField.setResponder(s -> {
                if (commandSuggestions != null) {
                    commandSuggestions.setAllowSuggestions(true);
                    commandSuggestions.updateCommandInfo();
                }
            });
            addRenderableWidget(Button.builder(Component.literal("\u00A7a\u2714 OK"), btn -> {
                String cmd = valueField.getValue();
                if (cmd.startsWith("/"))
                    cmd = cmd.substring(1);
                entry.command = cmd;
                minecraft.setScreen(parent);
            }).bounds(cx - 52, cy + 20, 50, BTN_H).build());
            addRenderableWidget(Button.builder(Component.literal("\u00A7c\u2716"), btn -> {
                minecraft.setScreen(parent);
            }).bounds(cx + 2, cy + 20, 50, BTN_H).build());
        }

        @Override
        public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            super.renderBackground(g, mouseX, mouseY, partialTick);
            int cx = width / 2;
            int cy = height / 2;
            g.fill(cx - 160, cy - 40, cx + 160, cy + 50, 0xE0101020);
            g.drawCenteredString(font, "\u00A7e\u00A7lEdit command \u00A77(e.g. say hello)", cx, cy - 32, 0xFFCCDDFF);
            super.render(g, mouseX, mouseY, partialTick);
            if (commandSuggestions != null)
                commandSuggestions.render(g, mouseX, mouseY);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
