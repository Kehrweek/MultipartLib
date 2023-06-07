package de.kehrweek.multipartlib.api.block;

import com.mojang.datafixers.util.Pair;
import de.kehrweek.multipartlib.MultipartAPI;
import de.kehrweek.multipartlib.api.part.*;
import de.kehrweek.multipartlib.api.registry.MPRegistry;
import de.kehrweek.multipartlib.api.util.BlockContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

public class MultipartBlockEntity extends BlockEntity {

    private final Map<PartPos, PartState> parts;
    private final Map<PartPos, PartEntity> partEntities;


    public MultipartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.parts = new HashMap<>();
        this.partEntities = new HashMap<>();
    }

    public MultipartBlockEntity(BlockPos pos, BlockState state) {
        this(MultipartAPI.getDefaultBlockEntityType(), pos, state);
    }


    protected final void markForUpdate() {
        if (world instanceof ServerWorld sw)
            sw.getChunkManager().markForUpdate(getPos());
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        // parts
        final NbtCompound partsNbt = new NbtCompound();
        getParts().forEach((pos, state) -> {
            final Identifier id = MPRegistry.PART_POS.getId(pos);
            if (id == null) return;
            final NbtElement stateNbt = PartState.CODEC.encodeStart(NbtOps.INSTANCE, state).result().orElseThrow();
            partsNbt.put(id.toString(), stateNbt);
        });
        nbt.put("parts", partsNbt);

        // part entities
        final NbtCompound entitiesNbt = new NbtCompound();
        getPartEntities().forEach((pos, entity) -> {
            final Identifier id = MPRegistry.PART_POS.getId(pos);
            if (id == null) return;
            entitiesNbt.put(id.toString(), entity.createNbtWithIdentifyingData());
        });
        nbt.put("entities", entitiesNbt);
    }

    protected Optional<PartPos> raycast(Entity entity, double distance, BiFunction<PartPos, PartState, VoxelShape> shapeFunction) {
        final Map<PartPos, PartState> parts = getParts();
        if (parts.isEmpty()) return Optional.empty();

        final Vec3d start = entity.getEyePos();
        final Vec3d rot = entity.getRotationVector();
        // no need to normalize rot (it's ~1)
        final Vec3d end = start.add(rot.multiply(distance));

        PartPos p = null;
        double sqDist = Double.POSITIVE_INFINITY;
        for (Map.Entry<PartPos, PartState> e : parts.entrySet()) {
            final VoxelShape shape = shapeFunction.apply(e.getKey(), e.getValue());
            final BlockHitResult hit = shape.raycast(start, end, pos);
            if (hit == null || hit.getType() == HitResult.Type.MISS) continue;

            final double hitSqDist = hit.getPos().squaredDistanceTo(start);
            if (hitSqDist < sqDist) {
                sqDist = hitSqDist;
                p = e.getKey();
            }
        }
        return Optional.ofNullable(p);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        // parts
        parts.clear();
        final NbtCompound partsNbt = nbt.getCompound("parts");
        for (String key : partsNbt.getKeys()) {
            final Identifier id = Identifier.tryParse(key);
            if (id == null) continue;
            final PartPos pos = MPRegistry.PART_POS.get(id);
            if (pos == null) continue;
            PartState.CODEC.decode(NbtOps.INSTANCE, partsNbt.getCompound(key)).result()
                    .map(Pair::getFirst)
                    .ifPresent(state -> parts.put(pos, state));
        }

        // part entities
        partEntities.clear();
        final NbtCompound entitiesNbt = nbt.getCompound("entities");
        for (String key : entitiesNbt.getKeys()) {
            final Identifier id = Identifier.tryParse(key);
            if (id == null) continue;
            final PartPos pos = MPRegistry.PART_POS.get(id);
            if (pos == null) continue;
            // TODO just read?
            final PartEntity entity = PartEntity.createFromNbt(this, pos, entitiesNbt.getCompound(key));
            if (entity != null) partEntities.put(pos, entity);
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbtWithIdentifyingData();
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public Optional<PartPos> raycast(Entity entity, double distance) {
        final BlockContext<BlockView> bi = new BlockContext<>(getWorld(), getPos(), getCachedState(), this);
        return raycast(entity, distance, (pos, state) -> state.getRaycastShape(bi, pos));
    }

    public Optional<PartPos> raycastOutline(Entity entity, double distance) {
        final BlockContext<BlockView> bi = new BlockContext<>(getWorld(), getPos(), getCachedState(), this);
        final ShapeContext ctx = ShapeContext.of(entity);
        return raycast(entity, distance, (pos, state) -> state.getOutlineShape(bi, pos, ctx));
    }

    public Optional<PartPos> raycastCollision(Entity entity, double distance) {
        final BlockContext<BlockView> bi = new BlockContext<>(getWorld(), getPos(), getCachedState(), this);
        final ShapeContext ctx = ShapeContext.of(entity);
        return raycast(entity, distance, (pos, state) -> state.getCollisionShape(bi, pos, ctx));
    }

    public Map<PartPos, PartState> getParts() {
        return Collections.unmodifiableMap(parts);
    }

    public boolean hasParts() {
        return !parts.isEmpty();
    }

    public boolean hasPart(PartPos pos) {
        return parts.containsKey(pos);
    }

    @Nullable
    public PartState getPart(PartPos pos) {
        return this.parts.get(pos);
    }

    public void setPart(PartPos pos, PartState state) {
        final PartState oldState = this.parts.put(pos, state);
        if (oldState != null) {
            final BlockContext<World> bc = new BlockContext<>(getWorld(), getPos(), getCachedState(), this);
            oldState.onReplaced(bc, pos, state);
        }

        final PartEntity pe = getPartEntity(pos);
        // do we need a part entity?
        if (state.getPart() instanceof PartWithEntity pwe
            && pwe.hasPartEntity(state)) {
            // no current entity || we have to replace the current entity
            if (pe == null || !pe.getType().supports(state)) {
                final PartEntity entity = pwe.createPartEntity(this, pos);
                setPartEntity(pos, entity);
            }
        } else if (pe != null) removePartEntity(pos);

        markForUpdate();
    }

    public boolean removePart(PartPos pos) {
        final PartState state = this.parts.remove(pos);

        // notify part
        if (state != null) {
            final BlockContext<World> bc = new BlockContext<>(getWorld(), getPos(), getCachedState(), this);
            state.onRemoved(bc, pos);
        }

        // remove entity
        removePartEntity(pos);

        if (state != null) {
            markForUpdate();
            return true;
        }
        return false;
    }

    public Map<PartPos, PartEntity> getPartEntities() {
        return Collections.unmodifiableMap(partEntities);
    }

    @Nullable
    public PartEntity getPartEntity(PartPos pos) {
        return this.partEntities.get(pos);
    }

    @Nullable
    public <T extends PartEntity> T getPartEntity(PartPos pos, PartEntityType<T> type) {
        final PartEntity entity = getPartEntity(pos);
        if (entity == null || !type.supports(entity)) return null;
        // it is checked:      here ^
        //noinspection unchecked
        return (T) entity;
    }

    public void setPartEntity(PartPos pos, PartEntity entity) {
        Objects.requireNonNull(entity);
        final PartEntity oldEntity = this.partEntities.put(pos, entity);
        if (oldEntity != null) oldEntity.onReplaced(entity);
        markForUpdate();
    }

    public boolean removePartEntity(PartPos pos) {
        final PartEntity entity = partEntities.remove(pos);
        if (entity != null) {
            entity.onRemoved();
            markForUpdate();
            return true;
        }
        return false;
    }

}
