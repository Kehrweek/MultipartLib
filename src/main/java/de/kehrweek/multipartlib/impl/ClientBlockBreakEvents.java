package de.kehrweek.multipartlib.impl;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ClientBlockBreakEvents {

    @FunctionalInterface
    interface Before {

        boolean beforeBlockBreak(World world, BlockPos pos, BlockState state, ClientPlayerEntity player);

    }

    Event<Before> BEFORE = EventFactory.createArrayBacked(Before.class, listeners -> (w, p, s, e) -> {
        for (Before l : listeners) {
            if (!l.beforeBlockBreak(w, p, s, e)) return false;
        }
        return true;
    });

}
