package de.kehrweek.multipartlib.api.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;

@FunctionalInterface
public interface PartRendererFactory {

    record Context(PartRenderDispatcher partRenderDispatcher, BlockRenderManager blockRenderManager,
            EntityModelLoader entityModelLoader, ItemRenderer itemRenderer, TextRenderer textRenderer) {

        public static Context fromClient(PartRenderDispatcher partRenderDispatcher) {
            final MinecraftClient client = MinecraftClient.getInstance();
            return new Context(partRenderDispatcher, client.getBlockRenderManager(), client.getEntityModelLoader(), client.getItemRenderer(), client.textRenderer);
        }

        public ModelPart getLayerModelPart(EntityModelLayer layer) {
            return entityModelLoader.getModelPart(layer);
        }

    }

    PartRenderer create(Context ctx);

}
