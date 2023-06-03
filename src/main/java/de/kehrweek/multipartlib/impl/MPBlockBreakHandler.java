package de.kehrweek.multipartlib.impl;

import de.kehrweek.multipartlib.api.block.MultipartBlock;
import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;
import de.kehrweek.multipartlib.api.part.PartPos;
import de.kehrweek.multipartlib.api.part.PartState;
import de.kehrweek.multipartlib.api.util.BlockContext;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@ApiStatus.Internal
public final class MPBlockBreakHandler {

    static {
        ClientBlockBreakEvents.BEFORE.register(MPBlockBreakHandler::handleClientBlockBreak);
        PlayerBlockBreakEvents.BEFORE.register(MPBlockBreakHandler::handleServerBlockBreak);
    }

    private MPBlockBreakHandler() {

    }

    private static boolean handleClientMPBlockBreak(World world, MultipartBlock block, BlockPos pos, BlockState state, MultipartBlockEntity entity, ClientPlayerEntity player) {
        //noinspection DuplicatedCode
        final Optional<PartPos> hit = entity.raycast(player, 20);
        if (hit.isEmpty()) return true;

        final PartPos partPos = hit.get();
        final PartState partState = Objects.requireNonNull(entity.getPart(partPos));
        final BlockContext<World> ctx = new BlockContext<>(world, pos, state, entity);

        block.onPartBreak(ctx, partPos, partState, player);
        entity.removePart(partPos);
        block.onPartBroken(ctx, partPos, partState, player);

        return !entity.hasParts();
    }

    private static boolean handleClientBlockBreak(World world, BlockPos pos, BlockState state, ClientPlayerEntity player) {
        if (state.getBlock() instanceof MultipartBlock mb && world.getBlockEntity(pos) instanceof MultipartBlockEntity mpe)
            return handleClientMPBlockBreak(world, mb, pos, state, mpe, player);
        return true;
    }

    private static boolean handleServerMPBlockBreak(World world, MultipartBlock block, BlockPos pos, BlockState state, MultipartBlockEntity entity, PlayerEntity player) {
        //noinspection DuplicatedCode
        final Optional<PartPos> hit = entity.raycast(player, 20);
        if (hit.isEmpty()) return true;

        final PartPos partPos = hit.get();
        final PartState partState = Objects.requireNonNull(entity.getPart(partPos));
        final BlockContext<World> ctx = new BlockContext<>(world, pos, state, entity);

        block.onPartBreak(ctx, partPos, partState, player);
        entity.removePart(partPos);
        block.onPartBroken(ctx, partPos, partState, player);

        if (!player.isCreative()) {
            final ItemStack stack = player.getMainHandStack();
            final ItemStack stackCopy = stack.copy();
            final boolean canHarvest = player.canHarvest(state);
            stack.postMine(world, state, pos, player);
            if (canHarvest) {
                block.onAfterPartBroken(ctx, partPos, partState, player, stackCopy);
            }
        }

        return !entity.hasParts();
    }

    private static boolean handleServerBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity entity) {
        if (state.getBlock() instanceof MultipartBlock mb && entity instanceof MultipartBlockEntity mpe)
            return handleServerMPBlockBreak(world, mb, pos, state, mpe, player);
        return true;
    }

}