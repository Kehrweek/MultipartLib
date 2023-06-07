package de.kehrweek.multipartlib.impl;

import de.kehrweek.multipartlib.MultipartAPI;
import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;
import de.kehrweek.multipartlib.api.registry.MPRegistry;
import de.kehrweek.multipartlib.api.render.MPBlockEntityRenderer;
import de.kehrweek.multipartlib.api.render.PartRenderDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import static de.kehrweek.multipartlib.MultipartAPI.LOGGER;


// TODO debug command to enable line render
@ApiStatus.Internal
public final class MultipartInternal implements ModInitializer, ClientModInitializer {

    private static BlockEntityType<MultipartBlockEntity> BLOCK_ENTITY_TYPE;
    @Environment(EnvType.CLIENT)
    private static PartRenderDispatcher PART_RENDER_DISPATCHER;


    private static void loadClass(Class<?> clazz) {
        try {
            Class.forName(clazz.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers the given entry in the given registry using the given id
     * in combination with the {@link MultipartAPI#MOD_ID MOD_ID}.
     */
    public static <T, E extends T> E register(Registry<T> registry, String id, E entry) {
        Registry.register(registry, new Identifier(MultipartAPI.MOD_ID, id), entry);
        return entry;
    }

    public static boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static BlockEntityType<MultipartBlockEntity> getBlockEntityType() {
        return BLOCK_ENTITY_TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static PartRenderDispatcher getPartRenderDispatcher() {
        return PART_RENDER_DISPATCHER;
    }

    private void registerBlockEntityTypes() {
        BLOCK_ENTITY_TYPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MultipartAPI.MOD_ID, "multipart"),
                FabricBlockEntityTypeBuilder.create(MultipartBlockEntity::new).build(null));
    }

    @Environment(EnvType.CLIENT)
    private void registerBlockEntityRenderers() {
        BlockEntityRendererFactories.register(BLOCK_ENTITY_TYPE, MPBlockEntityRenderer::new);
    }

    @Environment(EnvType.CLIENT)
    private void registerResourceListeners() {
        PART_RENDER_DISPATCHER = new PartRenderDispatcher();
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(PART_RENDER_DISPATCHER);
    }

    @Override
    public void onInitialize() {
        LOGGER.debug("Common init!");

        // registry
        loadClass(MPRegistry.class);

        // events
        loadClass(MPBlockBreakHandler.class);

        loadClass(DirectionPartPos.class);

        registerBlockEntityTypes();
    }

    @Override
    public void onInitializeClient() {
        LOGGER.debug("Client init!");

        // resources
        registerResourceListeners();

        // renderers
        registerBlockEntityRenderers();
        loadClass(MPBlockOutlineRenderer.class);
    }

}
