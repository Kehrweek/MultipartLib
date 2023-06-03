package de.kehrweek.multipartlib.api.render;

import de.kehrweek.multipartlib.MultipartAPI;
import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;
import de.kehrweek.multipartlib.api.part.PartEntity;
import de.kehrweek.multipartlib.api.part.PartPos;
import de.kehrweek.multipartlib.api.part.PartState;
import de.kehrweek.multipartlib.api.part.PartWithEntity;
import de.kehrweek.multipartlib.api.registry.MPRegistry;
import de.kehrweek.multipartlib.api.util.BlockContext;
import de.kehrweek.multipartlib.impl.MultipartInternal;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MPBlockEntityRenderer<T extends MultipartBlockEntity> implements BlockEntityRenderer<T> {


    public MPBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {

    }


    private static MutableText literal(Object o) {
        return Text.literal(String.valueOf(o));
    }

    protected void renderText(List<Text> lines, int lineOff, Direction direction, MatrixStack matrices) {
        final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        matrices.multiply(direction.getRotationQuaternion());
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(90));
        matrices.translate(-0.5, -0.5, -0.5);
        matrices.translate(0.03, 0.03 + (lineOff * 0.08), -0.001);

        for (Text line : lines) {
            matrices.push();
            matrices.scale(0.006f, 0.006f, 0.006f);
            textRenderer.draw(matrices, line, 0, 0, 0xE0E0E0);
            matrices.pop();
            matrices.translate(0, 0.08, 0);
        }

        matrices.pop();
    }

    protected void renderText(Text text, int line, Direction textDirection, MatrixStack matrices) {
        renderText(List.of(text), line, textDirection, matrices);
    }

    protected void renderDebug(MultipartBlockEntity entity, float boxScale, Direction textDirection, MatrixStack matrices, VertexConsumerProvider vcp) {
        if (!MultipartInternal.isDev()) return;

        final VertexConsumer vc = vcp.getBuffer(RenderLayer.getLines());

        // draw the box
        matrices.push();
        final float boxOff = (1 - boxScale) / 2;
        matrices.translate(boxOff, boxOff, boxOff);
        matrices.scale(boxScale, boxScale, boxScale);
        WorldRenderer.drawBox(matrices, vc, 0, 0, 0, 1, 1, 1,
                0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
        matrices.pop();

        // draw part info
        final Block block = entity.getWorld().getBlockState(entity.getPos()).getBlock();
        final Entity camera = MinecraftClient.getInstance().getCameraEntity();
        final PartPos hitPos = entity.raycast(camera, 20).orElse(null);
        final Identifier hitPart = hitPos != null ? MPRegistry.PART.getId(entity.getPart(hitPos).getPart()) : null;
        final PartEntity hitEntity = hitPos != null ? entity.getPartEntity(hitPos) : null;
        final Identifier hitEntityId = hitEntity != null ? MPRegistry.PART_ENTITY_TYPE.getId(hitEntity.getType()) : null;
        final Map<PartPos, PartState> parts = entity.getParts();
        final Map<PartPos, PartEntity> partEntities = entity.getPartEntities();

        final List<Text> lines = new ArrayList<>();
        // block
        final MutableText blockText = literal(Registry.BLOCK.getId(block)).formatted(Formatting.WHITE);
        lines.add(blockText);
        // parts / partEntities - size
        final int partsSize = parts.size();
        final int partEntitiesSize = partEntities.size();
        final int expectedPartEntities = (int) parts.values().stream().filter(ps -> ps.getPart() instanceof PartWithEntity).count();
        final MutableText sizeText = literal(partsSize).formatted(partsSize != 0 ? Formatting.WHITE : Formatting.YELLOW);
        final MutableText partEntitiesText = literal(partEntitiesSize).formatted(partEntitiesSize == expectedPartEntities ? Formatting.WHITE : Formatting.RED);
        lines.add(literal("Parts: ").append(sizeText).append(literal(" Entities: ").append(partEntitiesText)));
        // target pos
        final MutableText posText = literal(hitPos).formatted(hitPos != null ? Formatting.GREEN : Formatting.RED);
        lines.add(literal("TPos: ").append(posText));
        // target part
        final MutableText partText = literal(hitPart).formatted(hitPart != null ? Formatting.GREEN : Formatting.RED);
        lines.add(literal("TP: ").append(partText));
        // target entity
        final MutableText entityText = literal(hitEntityId).formatted(hitEntityId != null ? Formatting.GREEN : Formatting.GRAY);
        lines.add(literal("TE: ").append(entityText));

        renderText(lines, 0, textDirection, matrices);
    }

    protected void renderDebug(MultipartBlockEntity entity, MatrixStack matrices, VertexConsumerProvider vcp) {
        renderDebug(entity, 1, Direction.SOUTH, matrices, vcp);
    }

    protected void renderParts(BlockContext<BlockRenderView> ctx, MatrixStack matrices, float tickDelta, VertexConsumerProvider vcp, int light, int overlay) {
        final PartRenderDispatcher partRenderDispatcher = MultipartAPI.getPartRenderDispatcher();

        final MultipartBlockEntity entity = ctx.entity();
        for (Map.Entry<PartPos, PartState> e : entity.getParts().entrySet()) {
            final PartPos pos = e.getKey();

            partRenderDispatcher.render(ctx, pos, e.getValue(), matrices, tickDelta, vcp, light, overlay);
        }
    }

    @Override
    public void render(MultipartBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay) {
        final BlockContext<BlockRenderView> blockCtx = new BlockContext<>(entity.getWorld(), entity.getPos(), entity.getCachedState(), entity);
        renderParts(blockCtx, matrices, tickDelta, vcp, light, overlay);

//        renderDebug(entity, matrices, vcp);
    }

}
