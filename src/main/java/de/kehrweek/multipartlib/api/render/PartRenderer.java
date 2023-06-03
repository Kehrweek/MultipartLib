package de.kehrweek.multipartlib.api.render;

import de.kehrweek.multipartlib.api.part.PartEntity;
import de.kehrweek.multipartlib.api.part.PartPos;
import de.kehrweek.multipartlib.api.part.PartState;
import de.kehrweek.multipartlib.api.util.BlockContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public interface PartRenderer {

    void render(BlockContext<BlockRenderView> ctx, PartPos pos, PartState state, @Nullable PartEntity entity,
            MatrixStack matrices, float tickDelta, VertexConsumerProvider vcp, int light, int overlay);


    default int getRenderDistance() {
        return 64;
    }

    default boolean isInRenderDistance(Vec3d pos, Vec3d cameraPos) {
        return pos.isInRange(cameraPos, getRenderDistance());
    }

}
