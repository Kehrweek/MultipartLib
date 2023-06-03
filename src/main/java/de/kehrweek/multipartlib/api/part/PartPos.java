package de.kehrweek.multipartlib.api.part;

import com.mojang.serialization.Codec;
import de.kehrweek.multipartlib.api.registry.MPRegistry;

public abstract class PartPos {

    public static final Codec<PartPos> CODEC = MPRegistry.PART_POS.getCodec();


    @Override
    public String toString() {
        return String.valueOf(MPRegistry.PART_POS.getId(this));
    }

}
