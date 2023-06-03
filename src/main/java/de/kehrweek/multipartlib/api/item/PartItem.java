package de.kehrweek.multipartlib.api.item;

import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;
import de.kehrweek.multipartlib.api.part.Part;
import de.kehrweek.multipartlib.api.part.PartPos;
import de.kehrweek.multipartlib.api.part.PartState;
import de.kehrweek.multipartlib.api.util.BlockContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PartItem extends BlockItem {

    private final Part part;


    public PartItem(Part part, Block block, Settings settings) {
        super(block, settings);
        this.part = part;
    }

    @Override
    protected boolean place(ItemPlacementContext ctx, BlockState newState) {
        final World world = ctx.getWorld();
        final BlockPos pos = ctx.getBlockPos();

        // get part pos and state
        final PartPos placePos = getPart().getPlacementPos(ctx, newState);
        final PartState placeState = getPart().getPlacementState(ctx, newState);
        if (placePos == null || placeState == null
            || !getPart().canPlace(world, pos, placePos, placeState)) return false;

        // check if existing part can be replaced
        if (world.getBlockEntity(pos) instanceof MultipartBlockEntity mbe) {
            final BlockContext<World> blockCtx = new BlockContext<>(world, pos, newState, mbe);
            final PartState oldPart = mbe.getPart(placePos);
            if (oldPart != null && !oldPart.canReplace(blockCtx, placePos, placeState)) return false;
        }

        // part can be placed -> set/update block
        world.setBlockState(pos, newState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        if (!(world.getBlockEntity(pos) instanceof MultipartBlockEntity entity)) return false;

        // update entity
        entity.setPart(placePos, placeState);

        return true;
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        return super.postPlacement(pos, world, player, stack, state);
    }

    @Override
    public String getTranslationKey() {
        return getOrCreateTranslationKey();
    }

    public boolean canApply(ItemPlacementContext ctx, BlockState state, MultipartBlockEntity entity) {
        return state.isOf(getBlock());
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return super.useOnBlock(context);
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        return super.place(context);
    }

    public Part getPart() {
        return part;
    }

}
