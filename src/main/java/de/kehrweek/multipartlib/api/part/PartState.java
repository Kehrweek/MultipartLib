package de.kehrweek.multipartlib.api.part;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import de.kehrweek.multipartlib.api.registry.MPRegistry;
import de.kehrweek.multipartlib.api.util.BlockContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class PartState extends State<Part, PartState> implements ItemConvertible {

    public static final Codec<PartState> CODEC = createCodec(MPRegistry.PART.getCodec(), Part::getDefaultState).stable();


    public PartState(Part owner, ImmutableMap<Property<?>, Comparable<?>> entries, MapCodec<PartState> codec) {
        super(owner, entries, codec);
    }


    /**
     * @see Part#onUse(BlockContext, PartPos, PartState, PlayerEntity, Hand, BlockHitResult)
     */
    public ActionResult onUse(BlockContext<World> ctx, PartPos partPos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return getPart().onUse(ctx, partPos, this, player, hand, hit);
    }

    /**
     * @see Part#canPlace(World, BlockPos, PartPos, PartState)
     */
    public boolean canPlace(World world, BlockPos blockPos, PartPos partPos) {
        return getPart().canPlace(world, blockPos, partPos, this);
    }

    /**
     * @see Part#canReplace(BlockContext, PartPos, PartState, PartState)
     */
    public boolean canReplace(BlockContext<World> ctx, PartPos pos, PartState newState) {
        return getPart().canReplace(ctx, pos, this, newState);
    }

    /**
     * @see Part#onReplaced(BlockContext, PartPos, PartState, PartState)
     */
    public void onReplaced(BlockContext<World> ctx, PartPos pos, PartState newState) {
        getPart().onReplaced(ctx, pos, this, newState);
    }

    /**
     * @see Part#onRemoved(BlockContext, PartPos, PartState)
     */
    public void onRemoved(BlockContext<World> ctx, PartPos pos) {
        getPart().onRemoved(ctx, pos, this);
    }

    /**
     * @see Part#onBreak(BlockContext, PartPos, PartState, PlayerEntity)
     */
    public void onBreak(BlockContext<World> ctx, PartPos pos, PlayerEntity player) {
        getPart().onBreak(ctx, pos, this, player);
    }

    /**
     * @see Part#onBroken(BlockContext, PartPos, PartState, PlayerEntity)
     */
    public void onBroken(BlockContext<World> ctx, PartPos pos, PlayerEntity player) {
        getPart().onBroken(ctx, pos, this, player);
    }

    /**
     * @see Part#onAfterBreak(BlockContext, PartPos, PartState, PlayerEntity, ItemStack)
     */
    public void onAfterBreak(BlockContext<World> ctx, PartPos pos, PlayerEntity player, ItemStack stack) {
        getPart().onAfterBreak(ctx, pos, this, player, stack);
    }

    /**
     * @see Part#onBlockStateReplaced(BlockContext, PartPos, PartState, BlockState, boolean)
     */
    public void onBlockStateReplaced(BlockContext<World> ctx, PartPos pos, BlockState newState, boolean moved) {
        getPart().onBlockStateReplaced(ctx, pos, this, newState, moved);
    }

    /**
     * @see Part#onBlockNeighborUpdate(BlockContext, PartPos, PartState, BlockPos, Block, boolean)
     */
    public void onBlockNeighborUpdate(BlockContext<World> ctx, PartPos partPos, BlockPos sourcePos, Block sourceBlock, boolean notify) {
        getPart().onBlockNeighborUpdate(ctx, partPos, this, sourcePos, sourceBlock, notify);
    }

    /**
     * @see Part#onBlockGetStateForNeighborUpdate(BlockContext, PartPos, PartState, Direction, BlockPos, BlockState)
     */
    public void onBlockGetStateForNeighborUpdate(BlockContext<WorldAccess> ctx, PartPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState) {
        getPart().onBlockGetStateForNeighborUpdate(ctx, pos, this, direction, neighborPos, neighborState);
    }

    /**
     * @see Part#onRandomDisplayTick(BlockContext, PartPos, PartState, Random)
     */
    public void onRandomDisplayTick(BlockContext<World> ctx, PartPos pos, Random random) {
        getPart().onRandomDisplayTick(ctx, pos, this, random);
    }

    /**
     * @see Part#getPickStack()
     */
    public ItemStack getPickStack() {
        return getPart().getPickStack();
    }

    /**
     * @see Part#asItem()
     */
    @Override
    public Item asItem() {
        return getPart().asItem();
    }

    /**
     * @see Part#getOutlineShape(BlockContext, PartPos, PartState, ShapeContext)
     */
    public VoxelShape getOutlineShape(BlockContext<BlockView> block, PartPos partPos, ShapeContext ctx) {
        return getPart().getOutlineShape(block, partPos, this, ctx);
    }

    /**
     * @see Part#getCollisionShape(BlockContext, PartPos, PartState, ShapeContext)
     */
    public VoxelShape getCollisionShape(BlockContext<BlockView> block, PartPos partPos, ShapeContext ctx) {
        return getPart().getCollisionShape(block, partPos, this, ctx);
    }

    /**
     * @see Part#getRaycastShape(BlockContext, PartPos, PartState)
     */
    public VoxelShape getRaycastShape(BlockContext<BlockView> block, PartPos partPos) {
        return getPart().getRaycastShape(block, partPos, this);
    }

    /**
     * @return This state owner part.
     */
    public Part getPart() {
        return this.owner;
    }

}
