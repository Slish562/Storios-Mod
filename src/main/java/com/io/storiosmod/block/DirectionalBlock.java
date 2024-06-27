package com.io.storiosmod.block;

import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DirectionalBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
    public static final VoxelShape BOX1 = Block.box(5, 0, 5, 9, 7, 9);
    public static final VoxelShape BOX2 = Block.box(5.7, 7, 5.7, 8.3, 9.6, 8.3);
    public static final VoxelShape BOX3 = Block.box(6, 3.5, -0.5, 8, 5.5, 3.5);
    public static final VoxelShape BOX4 = Block.box(4.6, -1.9, 6, 7.4, 3.3, 8.8);
    public static final VoxelShape BOX5 = Block.box(2.9, 6.6, 5.6, 5.6, 11.7, 8.1);
    public static final VoxelShape BOX6 = Block.box(6, 3.4, 6.3, 8, 9.4, 8.3);
    public static final VoxelShape BOX7 = Block.box(5.3, 3, 7, 6.3, 4, 8);
    public static final VoxelShape BOX8 = Block.box(3.4, 11, 6, 4.9, 12.5, 7.5);
    public static final VoxelShape BOX9 = Block.box(6, 0.6, 5.7, 8, 4, 7.7);
    public static final VoxelShape BOX10 = Block.box(6.1, 9, 6.3, 7.7, 10.4, 7.8);
    public static final VoxelShape BOX11 = Block.box(7, 3.3, 6.1, 7.7, 4.3, 6.8);
    public static final VoxelShape BOX12 = Block.box(6.6, 0, 4.1, 7.3, 1, 4.8);
    public static final VoxelShape BOX13 = Block.box(6.6, 9, 7.1, 7.3, 10, 7.8);

    public static final VoxelShape SHAPE = Shapes.or(BOX1, BOX2, BOX3, BOX4, BOX5, BOX6, BOX7, BOX8, BOX9, BOX10, BOX11, BOX12, BOX13);

    public DirectionalBlock() {
        super(Properties.copy(Blocks.AMETHYST_BLOCK).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));

    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        //return Shapes.or(LEG_NE,LEG_NW,LEG_SE,LEG_SW,TABLE_TOP);
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }
}
