package com.io.storiosmod.block.entity;

import com.io.storiosmod.registries.BlockRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class RadiusTriggerBlockEntity extends BlockEntity {

    public enum TriggerMode { ONCE, EVERY_ENTRY }

    private int radius = 5;
    private TriggerMode triggerMode = TriggerMode.ONCE;
    private final List<String> commands = new ArrayList<>();
    private final Set<UUID> triggeredPlayers = new HashSet<>();
    private final Set<UUID> playersInRange = new HashSet<>();

    public RadiusTriggerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockRegistry.RADIUS_TRIGGER_BLOCK_ENTITY.get(), pos, state);
    }

    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = Math.max(1, Math.min(100, radius)); }
    public TriggerMode getTriggerMode() { return triggerMode; }
    public void setTriggerMode(TriggerMode mode) { this.triggerMode = mode; }
    public List<String> getCommands() { return commands; }

    public void setCommands(List<String> newCommands) {
        commands.clear();
        commands.addAll(newCommands);
        setChanged();
    }

    public void resetTriggered() {
        triggeredPlayers.clear();
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadiusTriggerBlockEntity be) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) return;
        if (level.getGameTime() % 10 != 0) return;

        Vec3 center = Vec3.atCenterOf(pos);
        double r = be.radius;
        AABB box = new AABB(center.subtract(r, r, r), center.add(r, r, r));
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class, box);

        Set<UUID> currentlyInRange = new HashSet<>();
        for (ServerPlayer player : nearbyPlayers) {
            if (player.position().distanceTo(center) <= r) {
                currentlyInRange.add(player.getUUID());
            }
        }

        for (ServerPlayer player : nearbyPlayers) {
            UUID uuid = player.getUUID();
            if (!currentlyInRange.contains(uuid)) continue;

            boolean shouldTrigger = false;
            if (be.triggerMode == TriggerMode.ONCE) {
                if (!be.triggeredPlayers.contains(uuid)) {
                    be.triggeredPlayers.add(uuid);
                    be.setChanged();
                    shouldTrigger = true;
                }
            } else {
                if (!be.playersInRange.contains(uuid)) {
                    shouldTrigger = true;
                }
            }

            if (shouldTrigger) {
                be.executeCommands(serverLevel, player);
            }
        }

        Set<UUID> left = new HashSet<>(be.playersInRange);
        left.removeAll(currentlyInRange);
        be.playersInRange.clear();
        be.playersInRange.addAll(currentlyInRange);
    }

    private void executeCommands(ServerLevel level, ServerPlayer player) {
        for (String command : commands) {
            String cmd = command.startsWith("/") ? command.substring(1) : command;
            cmd = cmd.replace("@p", player.getName().getString());
            CommandSourceStack source = level.getServer().createCommandSourceStack()
                    .withPosition(Vec3.atCenterOf(worldPosition))
                    .withPermission(4);
            level.getServer().getCommands().performPrefixedCommand(source, cmd);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Radius", radius);
        tag.putString("TriggerMode", triggerMode.name());

        ListTag commandList = new ListTag();
        for (String command : commands) {
            commandList.add(StringTag.valueOf(command));
        }
        tag.put("Commands", commandList);

        ListTag triggered = new ListTag();
        for (UUID uuid : triggeredPlayers) {
            triggered.add(StringTag.valueOf(uuid.toString()));
        }
        tag.put("TriggeredPlayers", triggered);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        radius = tag.getInt("Radius");
        if (radius <= 0) radius = 5;

        String mode = tag.getString("TriggerMode");
        try { triggerMode = TriggerMode.valueOf(mode); }
        catch (Exception e) { triggerMode = TriggerMode.ONCE; }

        commands.clear();
        if (tag.contains("Commands", Tag.TAG_LIST)) {
            ListTag commandList = tag.getList("Commands", Tag.TAG_STRING);
            for (int i = 0; i < commandList.size(); i++) {
                commands.add(commandList.getString(i));
            }
        } else if (tag.contains("Actions", Tag.TAG_LIST)) {

            ListTag actionList = tag.getList("Actions", Tag.TAG_COMPOUND);
            for (int i = 0; i < actionList.size(); i++) {
                CompoundTag at = actionList.getCompound(i);
                if (at.contains("Value")) {
                    String val = at.getString("Value");
                    if (at.getString("Type").equals("CUTSCENE")) {
                        commands.add("cutscene play " + val + " @p");
                    } else if (at.getString("Type").equals("MESSAGE")) {
                        commands.add("tellraw @p {\"text\":\"" + val.replace("\"", "\\\"") + "\"}");
                    } else {
                        commands.add(val);
                    }
                }
            }
        }

        triggeredPlayers.clear();
        ListTag triggered = tag.getList("TriggeredPlayers", Tag.TAG_STRING);
        for (int i = 0; i < triggered.size(); i++) {
            try { triggeredPlayers.add(UUID.fromString(triggered.getString(i))); }
            catch (Exception ignored) {}
        }
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

