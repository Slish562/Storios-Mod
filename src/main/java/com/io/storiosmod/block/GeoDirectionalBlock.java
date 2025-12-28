package com.io.storiosmod.block;

import com.io.storiosmod.registries.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class GeoDirectionalBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public GeoDirectionalBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(net.minecraft.world.item.DyeColor.CYAN)
                .strength(50.0F, 1200.0F) // как у обсидиана: hardness 50, blast resistance 1200
                .requiresCorrectToolForDrops()
                .noOcclusion()
                .dynamicShape()
                .lightLevel(state -> 2)
                .emissiveRendering((state, level, pos) -> true)
        );

        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, false));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockPos pos = context.getClickedPos();
        LevelReader level = context.getLevel();

        BlockPos attachPos = pos.relative(clickedFace.getOpposite());
        BlockState attachState = level.getBlockState(attachPos);

        boolean canAttach = attachState.isFaceSturdy(level, attachPos, clickedFace);

        FluidState fluid = context.getLevel().getFluidState(pos);
        boolean inFluid = !fluid.isEmpty();

        if (canAttach || inFluid) {
            BlockState state = this.defaultBlockState().setValue(FACING, clickedFace);
            return state.setValue(WATERLOGGED, fluid.getType() == Fluids.WATER || fluid.getType() == Fluids.FLOWING_WATER);
        }

        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos attachPos = pos.relative(facing.getOpposite());
        BlockState attachState = level.getBlockState(attachPos);
        return attachState.isFaceSturdy(level, attachPos, facing);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);

        return switch (dir) {
            case UP    -> Block.box(3.0D, 0.0D, 3.0D, 13.0D, 11.0D, 13.0D);
            case DOWN  -> Block.box(3.0D, 5.0D, 3.0D, 13.0D, 16.0D, 13.0D);
            case NORTH -> Block.box(3.0D, 3.0D, 5.0D, 13.0D, 13.0D, 16.0D);
            case SOUTH -> Block.box(3.0D, 3.0D, 0.0D, 13.0D, 13.0D, 11.0D);
            case EAST  -> Block.box(0.0D, 3.0D, 3.0D, 11.0D, 13.0D, 13.0D);
            case WEST  -> Block.box(5.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D);
        };
    }

    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};

        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[1] = Shapes.or(buffer[1], buffer[0]);
            buffer[0] = rotateHorizontal(buffer[0]);
        }

        if (to.getAxis() == Direction.Axis.Y) {
            return buffer[1];
        }

        return buffer[1];
    }

    private static VoxelShape rotateHorizontal(VoxelShape shape) {
        return Block.box(
                16 - shape.max(Direction.Axis.Z), shape.min(Direction.Axis.Y), shape.min(Direction.Axis.X),
                16 - shape.min(Direction.Axis.Z), shape.max(Direction.Axis.Y), shape.max(Direction.Axis.X)
        );
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockRegistry.MYTHRIL_CLUSTER_SMALL_BLOCK_ENTITY.get().create(pos, state);
    }
}