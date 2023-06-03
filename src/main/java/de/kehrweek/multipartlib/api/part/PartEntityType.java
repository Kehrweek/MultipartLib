package de.kehrweek.multipartlib.api.part;

import com.google.common.collect.ImmutableSet;
import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;

import java.util.Set;

public class PartEntityType<T extends PartEntity> {

    @FunctionalInterface
    public interface Factory<T extends PartEntity> {

        T create(MultipartBlockEntity entity, PartPos pos);

    }

    private final Factory<T> factory;
    private final Set<Part> parts;
    private final Class<T> type;


    public PartEntityType(Class<T> type, Factory<T> factory, Part... parts) {
        this.type = type;
        this.factory = factory;
        this.parts = ImmutableSet.copyOf(parts);

        if (this.parts.isEmpty())
            throw new IllegalArgumentException("Parts should not be empty!");
    }


    /**
     * @return Whether this part entity is compatible with the given part.
     */
    public boolean supports(Part part) {
        return this.parts.contains(part);
    }

    /**
     * @see #supports(Part)
     */
    public boolean supports(PartState state) {
        return supports(state.getPart());
    }

    /**
     * @param entity The entity to check.
     * @return Whether this type can be used with the given entity.
     */
    public boolean supports(PartEntity entity) {
        return type.isAssignableFrom(entity.getClass());
    }

    public T instantiate(MultipartBlockEntity entity, PartPos pos) {
        return factory.create(entity, pos);
    }

    public Class<T> getType() {
        return type;
    }

}
