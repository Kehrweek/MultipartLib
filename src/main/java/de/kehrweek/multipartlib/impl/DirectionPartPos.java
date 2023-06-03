package de.kehrweek.multipartlib.impl;

import de.kehrweek.multipartlib.api.part.PartPos;
import de.kehrweek.multipartlib.api.registry.MPRegistry;
import net.minecraft.util.math.Direction;

public final class DirectionPartPos extends PartPos {

    public static final DirectionPartPos UP;
    public static final DirectionPartPos DOWN;
    public static final DirectionPartPos NORTH;
    public static final DirectionPartPos SOUTH;
    public static final DirectionPartPos EAST;
    public static final DirectionPartPos WEST;

    static {
        UP = MultipartInternal.register(MPRegistry.PART_POS, "up", new DirectionPartPos(Direction.UP));
        DOWN = MultipartInternal.register(MPRegistry.PART_POS, "down", new DirectionPartPos(Direction.DOWN));
        NORTH = MultipartInternal.register(MPRegistry.PART_POS, "north", new DirectionPartPos(Direction.NORTH));
        SOUTH = MultipartInternal.register(MPRegistry.PART_POS, "south", new DirectionPartPos(Direction.SOUTH));
        EAST = MultipartInternal.register(MPRegistry.PART_POS, "east", new DirectionPartPos(Direction.EAST));
        WEST = MultipartInternal.register(MPRegistry.PART_POS, "west", new DirectionPartPos(Direction.WEST));
    }

    private final Direction direction;

    private DirectionPartPos(Direction direction) {
        this.direction = direction;
    }


    public static DirectionPartPos from(Direction dir) {
        return switch (dir) {
            case UP -> UP;
            case DOWN -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
        };
    }

    public Direction toDirection() {
        return direction;
    }

    public float getPitch() {
        return (float) switch (direction) {
            case UP -> 0;
            case NORTH, SOUTH, EAST, WEST -> Math.PI / 2;
            case DOWN -> Math.PI;
        };
    }

    public float getYaw() {
        return (float) switch (direction) {
            case UP, DOWN, SOUTH -> 0;
            case WEST -> -(Math.PI / 2);
            case NORTH -> Math.PI;
            case EAST -> Math.PI / 2;
        };
    }

}
