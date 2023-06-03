package de.kehrweek.multipartlib.api.part;

import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;

public abstract class PartWithEntity extends Part {

    public abstract PartEntity createPartEntity(MultipartBlockEntity entity, PartPos pos);

    public boolean hasPartEntity(PartState state) {
        return true;
    }

}
