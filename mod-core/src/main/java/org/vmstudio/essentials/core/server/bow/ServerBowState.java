package org.vmstudio.essentials.core.server.bow;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerBowState {

    private static final ConcurrentHashMap<UUID, Float> TENSIONS = new ConcurrentHashMap<>();

    private static final ThreadLocal<LivingEntity> RELEASING_ENTITY = new ThreadLocal<>();

    private ServerBowState() {}

    public static void setTension(ServerPlayer player, float tension) {
        TENSIONS.put(player.getUUID(), Math.max(0f, Math.min(1f, tension)));
    }

    public static float consumeTension(UUID uuid) {
        Float v = TENSIONS.remove(uuid);
        return v == null ? -1f : v;
    }

    public static float peekTension(UUID uuid) {
        Float v = TENSIONS.get(uuid);
        return v == null ? -1f : v;
    }

    public static void clear(UUID uuid) {
        TENSIONS.remove(uuid);
    }

    public static void beginRelease(LivingEntity entity) {
        RELEASING_ENTITY.set(entity);
    }

    public static void endRelease() {
        RELEASING_ENTITY.remove();
    }

    public static LivingEntity currentReleaser() {
        return RELEASING_ENTITY.get();
    }
}