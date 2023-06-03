package de.kehrweek.multipartlib.api.render;

import com.google.common.collect.ImmutableMap;
import de.kehrweek.multipartlib.api.part.Part;
import de.kehrweek.multipartlib.api.part.PartEntity;
import de.kehrweek.multipartlib.api.part.PartPos;
import de.kehrweek.multipartlib.api.part.PartState;
import de.kehrweek.multipartlib.api.registry.MPRegistry;
import de.kehrweek.multipartlib.api.render.PartRendererFactory.Context;
import de.kehrweek.multipartlib.api.util.BlockContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static de.kehrweek.multipartlib.MultipartAPI.LOGGER;
import static de.kehrweek.multipartlib.MultipartAPI.MOD_ID;

public class PartRenderDispatcher implements SimpleSynchronousResourceReloadListener {

    private static final Identifier IDENTIFIER = new Identifier(MOD_ID, "part_render_dispatcher");
    private Map<Part, PartRenderer> renderers = ImmutableMap.of();
    @Nullable
    private Camera camera;


    public PartRenderDispatcher() {
        WorldRenderEvents.START.register(ctx -> this.configure(ctx.camera()));
    }


    private static void runReported(Part part, Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            final CrashReport cr = CrashReport.create(t, "Rendering Part");
            part.populateCrashReport(cr.addElement("Part Details"));
            throw new CrashException(cr);
        }
    }

    private void configure(Camera camera) {
        this.camera = camera;
    }

    public void render(BlockContext<BlockRenderView> blockCtx, PartPos pos, PartState state, @Nullable PartEntity entity,
            MatrixStack matrices, float tickDelta, VertexConsumerProvider vcp, int light, int overlay) {
        final Part part = state.getPart();
        final PartRenderer renderer = get(part);

        if (renderer == null || camera == null || !renderer.isInRenderDistance(Vec3d.ofCenter(blockCtx.pos()), camera.getPos())) return;

        runReported(part, () -> renderer.render(blockCtx, pos, state, entity, matrices, tickDelta, vcp, light, overlay));
    }

    public void render(BlockContext<BlockRenderView> blockCtx, PartPos pos, PartState state, MatrixStack matrices,
            float tickDelta, VertexConsumerProvider vcp, int light, int overlay) {
        render(blockCtx, pos, state, blockCtx.entity().getPartEntity(pos), matrices, tickDelta, vcp, light, overlay);
    }

    @Override
    public void reload(ResourceManager manager) {
        final Context ctx = Context.fromClient(this);
        this.renderers = MPPartRenderers.reload(ctx);

        // check missing parts
        for (Part p : MPRegistry.PART) {
            if (!renderers.containsKey(p))
                LOGGER.warn("Missing partRenderFactory for Part {}. The Part may not be rendered correctly.", p);
        }
    }

    @Nullable
    public PartRenderer get(Part part) {
        return renderers.get(part);
    }

    @Override
    public Identifier getFabricId() {
        return IDENTIFIER;
    }

}
