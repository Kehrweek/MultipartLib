package de.kehrweek.multipartlib.api.registry;

import com.mojang.serialization.Lifecycle;
import de.kehrweek.multipartlib.MultipartAPI;
import de.kehrweek.multipartlib.api.part.Part;
import de.kehrweek.multipartlib.api.part.PartEntityType;
import de.kehrweek.multipartlib.api.part.PartPos;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

/**
 * Contains all registries used for this api.
 * The {@link Registry registries} and {@link RegistryKey keys} exposed here for querying and not for registering
 * new entries. Please call {@link MultipartAPI#registerPart(Identifier, Part) MultipartAPI.register...} for this.
 */
public final class MPRegistry {

    public static final RegistryKey<Registry<Part>> PART_KEY = key("part");
    /**
     * Contains all {@link Part parts}.
     * <p>
     * For registering new parts call: {@link MultipartAPI#registerPart(Identifier, Part)}
     *
     * @see MultipartAPI#registerPart(Identifier, Part)
     */
    public static final SimpleRegistry<Part> PART = simpleBuilder(PART_KEY).buildAndRegister();
    public static final RegistryKey<Registry<PartPos>> PART_POS_KEY = key("part_pos");
    /**
     * Contains all {@link PartPos positions}.
     * <p>
     * For registering new parts call: {@link MultipartAPI#registerPartPos(Identifier, PartPos)}}
     *
     * @see MultipartAPI#registerPartPos(Identifier, PartPos)
     */
    public static final SimpleRegistry<PartPos> PART_POS = simpleBuilder(PART_POS_KEY).buildAndRegister();
    public static final RegistryKey<Registry<PartEntityType<?>>> PART_ENTITY_KEY = key("part_entity_type");
    /**
     * Contains all {@link PartEntityType part entity types}.
     * <p>
     * For registering new parts call: {@link MultipartAPI#registerPartEntityType(Identifier, PartEntityType)}
     *
     * @see MultipartAPI#registerPartEntityType(Identifier, PartEntityType)
     */
    public static final SimpleRegistry<PartEntityType<?>> PART_ENTITY_TYPE = simpleBuilder(PART_ENTITY_KEY).buildAndRegister();


    private MPRegistry() {

    }


    private static <T> RegistryKey<Registry<T>> key(String name) {
        return RegistryKey.ofRegistry(new Identifier(MultipartAPI.MOD_ID, name));
    }

    private static <T> FabricRegistryBuilder<T, SimpleRegistry<T>> simpleBuilder(RegistryKey<Registry<T>> key) {
        return FabricRegistryBuilder.from(new SimpleRegistry<>(key, Lifecycle.stable(), null));
    }

}