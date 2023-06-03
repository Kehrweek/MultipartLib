package de.kehrweek.multipartlib.api.part;

import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;
import de.kehrweek.multipartlib.api.registry.MPRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static de.kehrweek.multipartlib.MultipartAPI.LOGGER;

public abstract class PartEntity {

    private final PartEntityType<?> type;
    private final MultipartBlockEntity entity;
    private final PartPos partPos;


    public PartEntity(PartEntityType<?> type, MultipartBlockEntity entity, PartPos pos) {
        if (!type.supports(this))
            throw new IllegalArgumentException("The given type is incompatible with this PartEntity");
        this.type = type;
        this.entity = entity;
        this.partPos = pos;
    }


    @Nullable
    public static PartEntity createFromNbt(MultipartBlockEntity entity, PartPos pos, NbtCompound nbt) {
        final String idString = nbt.getString("id");
        final Identifier id = Identifier.tryParse(idString);
        if (id == null) {
            LOGGER.error("PartEntity has invalid type: {}", idString);
            return null;
        }
        return MPRegistry.PART_ENTITY_TYPE.getOrEmpty(id).map(type -> {
            try {
                return type.instantiate(entity, pos);
            } catch (Throwable t) {
                LOGGER.error("Failed to create PartEntity {}", idString, t);
                return null;
            }
        }).map(pe -> {
            try {
                pe.readNbt(nbt);
                return pe;
            } catch (Throwable t) {
                LOGGER.error("Failed to load data for PartEntity {}", idString, t);
                return null;
            }
        }).orElseGet(() -> {
            LOGGER.warn("Skipping PartEntity with id {}", idString);
            return null;
        });
    }

    private void writeIdentifyingData(NbtCompound nbt) {
        final Identifier id = MPRegistry.PART_ENTITY_TYPE.getId(type);
        if (id == null) throw new IllegalStateException("Part missing registered Type id. This is a Bug!");
        nbt.putString("id", id.toString());
    }

    protected void writeNbt(NbtCompound nbt) {

    }

    protected void markDirty() {
        entity.markDirty();
    }

    public void readNbt(NbtCompound nbt) {

    }

    public NbtCompound createNbt() {
        final NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    public NbtCompound createNbtWithIdentifyingData() {
        final NbtCompound nbt = createNbt();
        writeIdentifyingData(nbt);
        return nbt;
    }

    /**
     * Called, when this entity is removed from the {@link #getEntity() entity}.
     */
    public void onRemoved() {

    }

    /**
     * Called, when a new entity is placed at the current position.
     * <p>
     * Calls {@link #onRemoved()} by default.
     *
     * @param newEntity The entity that replaced this one.
     */
    public void onReplaced(PartEntity newEntity) {
        onRemoved();
    }

    public PartEntityType<?> getType() {
        return type;
    }

    public MultipartBlockEntity getEntity() {
        return entity;
    }

    @Nullable
    public World getWorld() {
        return entity.getWorld();
    }

    public BlockPos getBlockPos() {
        return entity.getPos();
    }

    public PartPos getPartPos() {
        return partPos;
    }

}
