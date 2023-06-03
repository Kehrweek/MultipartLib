package de.kehrweek.multipartlib.impl;

import de.kehrweek.multipartlib.api.block.MultipartBlock;
import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;
import de.kehrweek.multipartlib.api.part.PartPos;
import de.kehrweek.multipartlib.api.part.PartState;
import de.kehrweek.multipartlib.api.util.BlockContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext.BlockOutlineContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;
import java.util.Optional;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public final class MPBlockOutlineRenderer {

    static {
        WorldRenderEvents.BLOCK_OUTLINE.register(MPBlockOutlineRenderer::renderMultipartBlockOutline);
    }

    private MPBlockOutlineRenderer() {

    }


    private static boolean renderMultipartBlockOutline(WorldRenderContext renderCtx, BlockOutlineContext blockCtx) {
        final VertexConsumerProvider vcp = Objects.requireNonNull(renderCtx.consumers());
        final ClientWorld world = renderCtx.world();
        final BlockPos pos = blockCtx.blockPos();
        final BlockState state = blockCtx.blockState();

        // get block and entity
        if (!(state.getBlock() instanceof MultipartBlock mpb)) return true;
        final MultipartBlockEntity entity = mpb.getEntity(world, pos).orElse(null);
        if (entity == null) return true;

        final Optional<PartPos> hit = entity.raycast(blockCtx.entity(), 20);
        if (hit.isPresent()) {
            final PartPos partPos = hit.get();
            final PartState partState = Objects.requireNonNull(entity.getPart(partPos));
            final BlockContext<BlockView> bi = new BlockContext<>(world, pos, state, entity);
            final Vec3d cameraPos = new Vec3d(blockCtx.cameraX(), blockCtx.cameraY(), blockCtx.cameraZ());
            renderPartOutline(bi, partPos, partState, blockCtx.entity(), cameraPos, renderCtx.matrixStack(), vcp);

            return false;
        }
        return true;
    }

    private static void renderPartOutline(BlockContext<BlockView> block, PartPos partPos, PartState state, Entity entity,
            Vec3d cameraPos, MatrixStack matrices, VertexConsumerProvider vcp) {
        final BlockPos blockPos = block.pos();
        final VoxelShape shape = state.getOutlineShape(block, partPos, ShapeContext.of(entity));
        WorldRenderer.drawCuboidShapeOutline(
                matrices,
                vcp.getBuffer(RenderLayer.getLines()),
                shape,
                (double) blockPos.getX() - cameraPos.getX(),
                (double) blockPos.getY() - cameraPos.getY(),
                (double) blockPos.getZ() - cameraPos.getZ(),
                0.0f, 0.0f, 0.0f, 0.4f);
    }

}