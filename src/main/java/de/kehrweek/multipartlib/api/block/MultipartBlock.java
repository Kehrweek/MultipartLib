package de.kehrweek.multipartlib.api.block;

import de.kehrweek.multipartlib.MultipartAPI;
import de.kehrweek.multipartlib.api.item.PartItem;
import de.kehrweek.multipartlib.api.part.Part;
import de.kehrweek.multipartlib.api.part.PartPos;
import de.kehrweek.multipartlib.api.part.PartState;
import de.kehrweek.multipartlib.api.util.BlockContext;
import de.kehrweek.multipartlib.api.util.Pair;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class MultipartBlock extends BlockWithEntity {


    public MultipartBlock(Settings settings) {
        super(settings);
    }

    protected MultipartBlockEntity createMultipartBlockEntity(BlockPos pos, BlockState state) {
        return new MultipartBlockEntity(pos, state);
    }

    protected void onStateReplacedAfterParts(BlockContext<World> block, BlockState newState, boolean moved) {
        if (!block.entity().hasParts()) {
            block.world().removeBlock(block.pos(), false);
        }
    }

    protected BlockState getStateForNeighborUpdateAfterParts(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return state;
    }

    /**
     * Combines all shapes returned by the shapeFunction.
     *
     * @param shapeFunction A function to return the shape of each part.
     * @return The union of all part shapes.
     */
    protected VoxelShape getShape(Map<PartPos, PartState> parts, BiFunction<PartPos, PartState, VoxelShape> shapeFunction) {
        VoxelShape shape = VoxelShapes.empty();
        for (Map.Entry<PartPos, PartState> e : parts.entrySet()) {
            shape = VoxelShapes.union(shape, shapeFunction.apply(e.getKey(), e.getValue()));
        }
        return shape;
    }

    public Optional<MultipartBlockEntity> getEntity(BlockView world, BlockPos pos) {
        return world.getBlockEntity(pos, MultipartAPI.getDefaultBlockEntityType());
    }

    @Nullable
    @Override
    public final BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return createMultipartBlockEntity(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext ctx) {
        final Optional<MultipartBlockEntity> entity = getEntity(ctx.getWorld(), ctx.getBlockPos());
        return entity.isPresent() && ctx.getStack().getItem() instanceof PartItem partItem
               && partItem.canApply(ctx, state, entity.get());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        final MultipartBlockEntity entity = getEntity(world, pos).orElse(null);
        if (entity == null) return;

        final BlockContext<World> bi = new BlockContext<>(world, pos, state, entity);
        entity.getParts().forEach((partPos, partState) -> partState.onBlockStateReplaced(bi, partPos, newState, moved));

        onStateReplacedAfterParts(bi, newState, moved);

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);

        final MultipartBlockEntity entity = getEntity(world, pos).orElse(null);
        if (entity == null) return;

        final BlockContext<World> bi = new BlockContext<>(world, pos, state, entity);
        new HashMap<>(entity.getParts()).forEach((pPos, pState) -> pState.onBlockNeighborUpdate(bi, pPos, sourcePos, sourceBlock, notify));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        final MultipartBlockEntity entity = getEntity(world, pos).orElse(null);
        if (entity == null) return state;

        final BlockContext<WorldAccess> bi = new BlockContext<>(world, pos, state, entity);
        entity.getParts().forEach((partPos, partState) -> partState.onBlockGetStateForNeighborUpdate(bi, partPos, direction, neighborPos, neighborState));

        return getStateForNeighborUpdateAfterParts(state, direction, neighborState, world, pos, neighborPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // get entity -> raycast part -> part.onUse
        return getEntity(world, pos).flatMap(entity -> {
            final BlockContext<World> bi = new BlockContext<>(world, pos, state, entity);
            return entity.raycast(player, 20).map(p -> new Pair<>(p, entity.getPart(p)))
                    .map(p -> p.value().onUse(bi, p.key(), player, hand, hit));
        }).orElse(ActionResult.PASS);
    }

    /**
     * Called, when a {@link Part} will be broken on a {@link MultipartBlock}.
     * <p>
     * This method is called on both the logical client and logical server, so take caution when overriding this method.
     *
     * @param ctx    The block context.
     * @param pos    The position of the part.
     * @param state  The state of the part.
     * @param player The player who broke the part.
     */
    public void onPartBreak(BlockContext<World> ctx, PartPos pos, PartState state, PlayerEntity player) {
        if (ctx.state().isIn(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinBrain.onGuardedBlockInteracted(player, false);
        }
        state.onBreak(ctx, pos, player);
    }

    /**
     * Called, when a {@link Part} is broken on a {@link MultipartBlock}.
     * <p>
     * This method is called on both the logical client and logical server, so take caution when overriding this method.
     *
     * @param ctx    The block context.
     * @param pos    The position of the part.
     * @param state  The state of the part.
     * @param player The player who broke the part.
     */
    public void onPartBroken(BlockContext<World> ctx, PartPos pos, PartState state, PlayerEntity player) {
        state.onBroken(ctx, pos, player);
    }

    /**
     * Called after {@link #onPartBroken(BlockContext, PartPos, PartState, PlayerEntity) onPartBroken(...)} and after
     * {@link ItemStack#postMine(World, BlockState, BlockPos, PlayerEntity) ItemStack.postMine(...)},
     * when the player can harvest this block.
     * <p>
     * This method is only called on the server.
     *
     * @param ctx    The block context.
     * @param pos    The position of the part.
     * @param state  The state of the part.
     * @param player The player who broke the part.
     * @param stack  The stack used to break the part.
     */
    public void onAfterPartBroken(BlockContext<World> ctx, PartPos pos, PartState state, PlayerEntity player, ItemStack stack) {
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005f);
        state.onAfterBreak(ctx, pos, player, stack);
    }

    /**
     * This method will only be called, when this block will be removed because no {@link Part parts}
     * are left in the {@link MultipartBlockEntity entity}.
     *
     * @see Block#onBreak(World, BlockPos, BlockState, PlayerEntity)
     * @see MultipartBlockEntity#getParts()
     */
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (state.isIn(BlockTags.GUARDED_BY_PIGLINS)) {
            PiglinBrain.onGuardedBlockInteracted(player, false);
        }
        world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(player, state));
    }

    /**
     * This method will only be called, when this block is borken because no {@link Part parts}
     * are left in the {@link MultipartBlockEntity entity}.
     *
     * @see Block#onBroken(WorldAccess, BlockPos, BlockState)
     * @see MultipartBlockEntity#getParts()
     */
    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {

    }

    /**
     * This method will only be called, when this block is borken because no {@link Part parts}
     * are left in the {@link MultipartBlockEntity entity}.
     *
     * @see Block#afterBreak(World, PlayerEntity, BlockPos, BlockState, BlockEntity, ItemStack)
     * @see MultipartBlockEntity#getParts()
     */
    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        Block.dropStacks(state, world, pos, blockEntity, player, stack);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        final MultipartBlockEntity entity = getEntity(world, pos).orElseThrow();

        final BlockContext<World> ctx = new BlockContext<>(world, pos, state, entity);
        entity.getParts().forEach((pPos, pState) -> {
            pState.onRandomDisplayTick(ctx, pPos, random);
        });
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final Entity camera = client.getCameraEntity();
        return getEntity(world, pos)
                .flatMap(e -> e.raycast(camera, 20).map(e::getPart))
                .map(PartState::getPickStack)
                .orElse(ItemStack.EMPTY);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        final MultipartBlockEntity entity = getEntity(world, pos).orElse(null);
        if (entity == null) return VoxelShapes.empty();
        final BlockContext<BlockView> bi = new BlockContext<>(world, pos, state, entity);
        return getShape(entity.getParts(), (partPos, ps) -> ps.getOutlineShape(bi, partPos, context));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        final MultipartBlockEntity entity = getEntity(world, pos).orElse(null);
        if (entity == null) return VoxelShapes.empty();
        final BlockContext<BlockView> bi = new BlockContext<>(world, pos, state, entity);
        return getShape(entity.getParts(), (partPos, ps) -> ps.getCollisionShape(bi, partPos, context));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        final MultipartBlockEntity entity = getEntity(world, pos).orElse(null);
        if (entity == null) return VoxelShapes.empty();
        final BlockContext<BlockView> bi = new BlockContext<>(world, pos, state, entity);
        return getShape(entity.getParts(), (partPos, ps) -> ps.getRaycastShape(bi, partPos));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

}
