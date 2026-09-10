package org.vmstudio.essentials.core.server;

import org.jetbrains.annotations.Nullable;


public final class BowReleaseTension {

    private static final ThreadLocal<Float> ACTIVE = new ThreadLocal<>();

    private BowReleaseTension() {}

    public static void open(float tension) {
        ACTIVE.set(tension);
    }

    public static @Nullable Float get() {
        return ACTIVE.get();
    }

    public static void close() {
        ACTIVE.remove();
    }
}
