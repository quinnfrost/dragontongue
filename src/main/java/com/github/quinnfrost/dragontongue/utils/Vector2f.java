package com.github.quinnfrost.dragontongue.utils;

import com.github.quinnfrost.dragontongue.client.overlay.OverlayCrossHair;

import java.util.Objects;

/**
 * An alternative for net.minecraft.util.math.vector.Vector2f, which is hash correct and has a factory method.
 */
public class Vector2f {
    public static final Vector2f ZERO = new Vector2f(0.0F, 0.0F);
    public static final Vector2f ONE = new Vector2f(1.0F, 1.0F);
    public static final Vector2f UNIT_X = new Vector2f(1.0F, 0.0F);
    public static final Vector2f NEGATIVE_UNIT_X = new Vector2f(-1.0F, 0.0F);
    public static final Vector2f UNIT_Y = new Vector2f(0.0F, 1.0F);
    public static final Vector2f NEGATIVE_UNIT_Y = new Vector2f(0.0F, -1.0F);
    public static final Vector2f MAX = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    public static final Vector2f MIN = new Vector2f(Float.MIN_VALUE, Float.MIN_VALUE);

    public static final Vector2f CR_DAMAGE = new Vector2f(-2, -40);
    public static final Vector2f CR_DISTANCE = new Vector2f(60, 8);
    public final float x;
    public final float y;

    public Vector2f(float xIn, float yIn) {
        this.x = xIn;
        this.y = yIn;
    }

    public static Vector2f of(float xIn, float yIn) {
        return new Vector2f(xIn, yIn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2f vector2f = (Vector2f) o;
        return Float.compare(vector2f.x, x) == 0 && Float.compare(vector2f.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
