package de.kehrweek.multipartlib.api.util;

import de.kehrweek.multipartlib.api.block.MultipartBlock;
import de.kehrweek.multipartlib.api.block.MultipartBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;

import java.util.function.Function;

public record BlockContext<T extends HeightLimitView>(T world, BlockPos pos, BlockState state, MultipartBlockEntity entity) {

    /**
     * @throws IllegalArgumentException If the given state is not for a {@link MultipartBlock}.
     */
    public BlockContext(T world, BlockPos pos, BlockState state, MultipartBlockEntity entity) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.entity = entity;

        if (!(state.getBlock() instanceof MultipartBlock))
            throw new IllegalArgumentException("The given BlockState is not for a MultipartBlock!");
    }


    public <U extends HeightLimitView> BlockContext<U> map(Function<T, U> function) {
        return new BlockContext<>(function.apply(world), pos, state, entity);
    }

    public <U extends HeightLimitView> BlockContext<U> world(U world) {
        return new BlockContext<>(world, pos, state, entity);
    }

    public MultipartBlock block() {
        return (MultipartBlock) state.getBlock();
    }

}
