package de.kehrweek.multipartlib;

import de.kehrweek.multipartlib.api.block.MultipartBlock;
import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;
import de.kehrweek.multipartlib.api.part.Part;
import de.kehrweek.multipartlib.api.part.PartEntityType;
import de.kehrweek.multipartlib.api.part.PartPos;
import de.kehrweek.multipartlib.api.registry.MPRegistry;
import de.kehrweek.multipartlib.api.render.MPPartRenderers;
import de.kehrweek.multipartlib.api.render.PartRenderDispatcher;
import de.kehrweek.multipartlib.api.render.PartRenderer;
import de.kehrweek.multipartlib.api.render.PartRendererFactory;
import de.kehrweek.multipartlib.impl.MultipartInternal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class MultipartAPI {

    public static final String MOD_ID = "multipart";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    private MultipartAPI() {

    }


    private static void checkIdentifier(Identifier id) {
        if (!MultipartInternal.isDev()) return;

        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
            LOGGER.warn("Detected id with default namespace: {}.", id);
        if (id.getNamespace().equals(MOD_ID))
            LOGGER.warn("Detected id with {} namespace: {}. Please use your own mod_id for registering content.", MOD_ID, id);
    }

    /**
     * Registers the given part to the {@link MPRegistry#PART} registry.
     *
     * @param id The id of the part.
     * @return The registered part.
     */
    public static <T extends Part> T registerPart(Identifier id, T part) {
        checkIdentifier(id);
        return Registry.register(MPRegistry.PART, id, part);
    }

    /**
     * Registers the given type to the {@link MPRegistry#PART_ENTITY_TYPE} registry.
     *
     * @param id The id of the part.
     * @return The registered part.
     */
    public static <T extends PartEntityType<?>> T registerPartEntityType(Identifier id, T type) {
        checkIdentifier(id);
        return Registry.register(MPRegistry.PART_ENTITY_TYPE, id, type);
    }

    /**
     * Registers the given pos to the {@link MPRegistry#PART_POS} registry.
     *
     * @param id The id of the pos.
     * @return The registered pos.
     */
    public static <T extends PartPos> T registerPartPos(Identifier id, T pos) {
        checkIdentifier(id);
        return Registry.register(MPRegistry.PART_POS, id, pos);
    }

    /**
     * @return The default type used, when creating a {@link MultipartBlock}.
     */
    public static BlockEntityType<MultipartBlockEntity> getDefaultBlockEntityType() {
        return MultipartInternal.getBlockEntityType();
    }

    /**
     * Registers the given renderer to be used when rendering the given
     * part using the {@link PartRenderDispatcher}.
     *
     * @param part    The part for which the renderer should apply.
     * @param factory A factory for the renderer.
     */
    @Environment(EnvType.CLIENT)
    public static void registerPartRenderer(Part part, PartRendererFactory factory) {
        MPPartRenderers.register(part, factory);
    }

    /**
     * @return The {@link PartRenderDispatcher} used to render Parts with their respective {@link PartRenderer}.
     */
    @Environment(EnvType.CLIENT)
    public static PartRenderDispatcher getPartRenderDispatcher() {
        return MultipartInternal.getPartRenderDispatcher();
    }

}