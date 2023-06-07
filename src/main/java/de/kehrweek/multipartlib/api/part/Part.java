package de.kehrweek.multipartlib.api.part;

import de.kehrweek.multipartlib.api.block.MultipartBlock;
import de.kehrweek.multipartlib.api.item.PartItem;
import de.kehrweek.multipartlib.api.registry.MPRegistry;
import de.kehrweek.multipartlib.api.util.BlockContext;
import de.kehrweek.multipartlib.impl.MultipartInternal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static de.kehrweek.multipartlib.MultipartAPI.LOGGER;

public abstract class Part implements ItemConvertible {

    protected final StateManager<Part, PartState> stateManager;
    private PartState defaultState;
    @Nullable
    private Item cachedItem;
    @Nullable
    private Identifier lootTableId;


    public Part() {
        final StateManager.Builder<Part, PartState> builder = new StateManager.Builder<>(this);
        appendProperties(builder);
        this.stateManager = builder.build(Part::getDefaultState, PartState::new);
        setDefaultState(this.stateManager.getDefaultState());

        final String name = getClass().getSimpleName();
        if (MultipartInternal.isDev() && !name.endsWith("Part")) {
            LOGGER.error("Part classes should end with \"Part\" and {} doesn't", name);
        }

        this.cachedItem = null;
    }


    /**
     * Searches the {@link Registry#ITEM Item registry} for a {@link PartItem},
     * where {@link PartItem#getPart()} matches the given part.
     *
     * @param part The part to search an item for.
     * @return The first fount item, or {@link Items#AIR}, if no item was found.
     */
    private static Item searchItem(Part part) {
        for (Item i : Registry.ITEM) {
            if (i instanceof PartItem partItem && partItem.getPart() == part)
                return partItem;
        }
        return Items.AIR;
    }

    protected void appendProperties(StateManager.Builder<Part, PartState> builder) {

    }

    protected LootContext.Builder buildLootTable(ServerWorld world, BlockPos pos, ItemStack tool) {
        return new LootContext.Builder(world)
                .random(world.random)
                .parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
                .parameter(LootContextParameters.TOOL, tool);
    }

    protected void dropStacks(BlockContext<World> ctx, PartPos pos, PartState state) {
        if (ctx.world() instanceof ServerWorld sw) {
            final List<ItemStack> stacks = getDroppedStacks(ctx.world(sw), pos, state);
            stacks.forEach(is -> Block.dropStack(sw, ctx.pos(), is));
            onStacksDropped(sw, ctx.pos(), pos, state, stacks);
        }
    }

    @ApiStatus.Experimental
    protected List<ItemStack> getDroppedStacks(BlockContext<ServerWorld> ctx, PartPos pos, PartState state) {
        final Identifier lootTableId = getLootTableId();
        if (lootTableId == LootTables.EMPTY) return new ArrayList<>();

        final LootContext.Builder builder = buildLootTable(ctx.world(), ctx.pos(), ItemStack.EMPTY);
        // TODO LootTableContext stuff
//        LootContext ctx = builder.parameter(PART_STATE, state).build(PART);
//        final ServerWorld world = ctx.getWorld();
//        final LootTable table = world.getServer().getLootManager().getTable(lootTableId);
//        return table.generateLoot(ctx);
        return new ArrayList<>();
    }

    protected StateManager<Part, PartState> getStateManager() {
        return stateManager;
    }

    public void onStacksDropped(ServerWorld world, BlockPos blockPos, PartPos partPos, PartState state, List<ItemStack> stacks) {

    }

    @Nullable
    public abstract PartPos getPlacementPos(ItemPlacementContext ctx, BlockState state);

    @Nullable
    public PartState getPlacementState(ItemPlacementContext ctx, BlockState state) {
        return getDefaultState();
    }

    public boolean canPlace(World world, BlockPos blockPos, PartPos partPos, PartState state) {
        return true;
    }

    /**
     * Called to check, if this part can be replaced or not.
     * <p>
     * This method is called on both the logical client and logical server, so take caution
     * when overriding this method.
     *
     * @return Whether this part can be replaced or not.
     */
    public boolean canReplace(BlockContext<World> ctx, PartPos pos, PartState currentState, PartState newState) {
        return false;
    }

    /**
     * Called if this part is replaced.
     *
     * @param ctx          The block context.
     * @param pos          The position, where replacement will take place.
     * @param currentState The current state of this part.
     * @param newState     The new state.
     */
    public void onReplaced(BlockContext<World> ctx, PartPos pos, PartState currentState, PartState newState) {
        onRemoved(ctx, pos, currentState);
    }

    /**
     * Called if this part is removed.
     *
     * @param ctx   The block context.
     * @param pos   The position, where this part was removed.
     * @param state The current state of this part.
     */
    public void onRemoved(BlockContext<World> ctx, PartPos pos, PartState state) {

    }

    /**
     * Called when this part gets clicked on.
     * <p>
     * If the action result is successful on a logical client, then the action will be sent to the logical server for processing.
     *
     * @param ctx       The block context.
     * @param partPos   The position of this part.
     * @param partState The state of this part.
     * @param player    The player who clicked.
     * @param hand      The hand used to click.
     * @param hit       The hit result.
     * @return An action result that specifies if using the block was successful.
     */
    public ActionResult onUse(BlockContext<World> ctx, PartPos partPos, PartState partState, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    /**
     * Called, when this part will be broken of the owning {@link MultipartBlock block}.
     * <p>
     * This method is called on both the logical client and logical server, so take caution when overriding this method.
     *
     * @param ctx    The block context.
     * @param pos    The position of this part.
     * @param state  The state of this part.
     * @param player The player, who broke it off.
     */
    public void onBreak(BlockContext<World> ctx, PartPos pos, PartState state, PlayerEntity player) {
        // TODO custom game event?
        ctx.world().syncWorldEvent(player, WorldEvents.BLOCK_BROKEN, ctx.pos(), Block.getRawIdFromState(ctx.state()));
    }

    /**
     * Called, when this part is broken off.
     * <p>
     * This method is called on both the logical client and logical server, so take caution when overriding this method.
     *
     * @param ctx    The block context.
     * @param pos    The position, where this part was.
     * @param state  The state of this part.
     * @param player The player, who broke it off.
     */
    public void onBroken(BlockContext<World> ctx, PartPos pos, PartState state, PlayerEntity player) {
//        ctx.world().emitGameEvent(GameEvent.BLOCK_DESTROY, ctx.pos(), GameEvent.Emitter.of(player, ctx.state()));
//        Registry.register(Registry.GAME_EVENT, new Identifier(MOD_ID, name), new GameEvent())
    }

    /**
     * Called after {@link #onBroken(BlockContext, PartPos, PartState, PlayerEntity) onBroken(...)} and after
     * {@link ItemStack#postMine(World, BlockState, BlockPos, PlayerEntity) ItemStack.postMine(...)},
     * when the player can harvest the owning {@link MultipartBlock block}.
     * <p>
     * This method is only called on the server.
     *
     * @param ctx    The block context.
     * @param pos    The position, where this part was.
     * @param state  The state of this part.
     * @param player The player, who broke it off.
     * @param stack  The stack used to break the part.
     */
    public void onAfterBreak(BlockContext<World> ctx, PartPos pos, PartState state, PlayerEntity player, ItemStack stack) {
        dropStacks(ctx, pos, state);
    }

    public void onBlockStateReplaced(BlockContext<World> ctx, PartPos pos, PartState state, BlockState newState, boolean moved) {

    }

    public void onBlockNeighborUpdate(BlockContext<World> ctx, PartPos pos, PartState partState, BlockPos sourcePos, Block sourceBlock, boolean notify) {

    }

    public void onBlockGetStateForNeighborUpdate(BlockContext<WorldAccess> ctx, PartPos pos, PartState state, Direction direction, BlockPos neighborPos, BlockState neighborState) {

    }

    public void onRandomDisplayTick(BlockContext<World> ctx, PartPos pos, PartState state, Random random) {

    }

    public void populateCrashReport(CrashReportSection s) {
        s.add("Name", () -> "%s // %s"
                .formatted(MPRegistry.PART.getId(this), getClass().getCanonicalName()));
    }

    @ApiStatus.Experimental
    public Identifier getLootTableId() {
        if (lootTableId == null) {
            final Identifier regId = MPRegistry.PART.getId(this);
            lootTableId = regId != null ? new Identifier(regId.getNamespace(), "parts/" + regId.getPath()) : LootTables.EMPTY;
        }
        return lootTableId;
    }

    public ItemStack getPickStack() {
        return new ItemStack(this);
    }

    /**
     * This should be overridden to avoid
     * unnecessary performance impact.
     *
     * @return This part as an item.
     */
    @Override
    public Item asItem() {
        if (cachedItem == null) {
            cachedItem = searchItem(this);
        }
        return cachedItem;
    }

    public VoxelShape getOutlineShape(BlockContext<BlockView> world, PartPos partPos, PartState partState, ShapeContext ctx) {
        return Block.createCuboidShape(0, 0, 0, 16, 16, 16);
    }

    public VoxelShape getCollisionShape(BlockContext<BlockView> world, PartPos partPos, PartState partState, ShapeContext ctx) {
        return getOutlineShape(world, partPos, partState, ctx);
    }

    public VoxelShape getRaycastShape(BlockContext<BlockView> ctx, PartPos partPos, PartState partState) {
        return getOutlineShape(ctx, partPos, partState, ShapeContext.absent());
    }

    public PartState getDefaultState() {
        return defaultState;
    }

    protected final void setDefaultState(PartState defaultState) {
        this.defaultState = defaultState;
    }

}
