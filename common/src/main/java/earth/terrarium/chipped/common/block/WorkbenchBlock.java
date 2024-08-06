package earth.terrarium.chipped.common.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class WorkbenchBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<WorkbenchModelType> MODEL_TYPE = EnumProperty.create("model", WorkbenchModelType.class);
    protected static final VoxelShape WORKBENCH_NORTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WORKBENCH_EAST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WORKBENCH_WEST_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WORKBENCH_SOUTH_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private final ContainerFactory factory;
    private final LazyLoadedValue<Component> containerName;

    public WorkbenchBlock(ContainerFactory factory, Properties properties) {
        super(properties);
        this.factory = factory;
        containerName = new LazyLoadedValue<>(() -> new TranslatableComponent("container.chipped." + Registry.BLOCK.getKey(WorkbenchBlock.this).getPath()));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(MODEL_TYPE, WorkbenchModelType.MAIN));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand handIn, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(state.getMenuProvider(level, pos));
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public MenuProvider getMenuProvider(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        return new SimpleMenuProvider(
            (id, inventory, player) -> factory.create(id, inventory, ContainerLevelAccess.create(level, pos)),
            containerName.get()
        );
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            WorkbenchModelType workbenchModel = state.getValue(MODEL_TYPE);
            if (workbenchModel == WorkbenchModelType.MAIN) {
                BlockPos otherpos = pos.relative(state.getValue(FACING).getClockWise());
                BlockState otherstate = level.getBlockState(otherpos);
                if (otherstate.getBlock() == this) {
                    level.setBlock(otherpos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, otherpos, Block.getId(otherstate));
                }
            }
            if (workbenchModel == WorkbenchModelType.SIDE) {
                BlockPos otherpos = pos.relative(state.getValue(FACING).getCounterClockWise());
                BlockState otherstate = level.getBlockState(otherpos);
                if (otherstate.getBlock() == this) {
                    level.setBlock(otherpos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, otherpos, Block.getId(otherstate));
                }
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockPos blockpos = pos.relative(state.getValue(FACING).getClockWise());
            level.setBlock(blockpos, state.setValue(MODEL_TYPE, WorkbenchModelType.SIDE), Block.UPDATE_ALL);
            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        switch (state.getValue(FACING)) {
            case NORTH:
                return WORKBENCH_NORTH_SHAPE;
            case SOUTH:
                return WORKBENCH_SOUTH_SHAPE;
            case WEST:
                return WORKBENCH_WEST_SHAPE;
            default:
                return WORKBENCH_EAST_SHAPE;
        }
    }

    @Deprecated
    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MODEL_TYPE);
    }

    @Deprecated
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos otherpos = pos.relative(state.getValue(FACING).getClockWise());
        return level.getBlockState(otherpos).getMaterial().isReplaceable();
    }

    @Deprecated
    @Environment(EnvType.CLIENT)
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1;
    }

    public enum WorkbenchModelType implements StringRepresentable {
        MAIN, SIDE;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String toString() {
            return getSerializedName();
        }
    }

    @FunctionalInterface
    public interface ContainerFactory {
        AbstractContainerMenu create(int windowId, Inventory inventory, ContainerLevelAccess position);
    }
}