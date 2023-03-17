package com.github.quinnfrost.dragontongue.client;

import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ClientGlow {
    public static Map<Integer, Integer> glowList;
    public static Level world = Minecraft.getInstance().level;

    static {
        glowList = new HashMap<>();
    }

    public static void setGlowing(Entity entity, int timeInTick) {
        if (entity != null) {
            world = entity.level;
            glowList.put(entity.getId(), timeInTick);
            entity.setSharedFlag(6, true);
        }
    }

    public static void tickGlowing() {
        glowList.values().removeIf(integer -> integer < 0);
        if (!glowList.isEmpty()) {
            glowList.replaceAll((entityID, time) -> {
                if (time > 0) {
                    return time - 1;
                } else if (world.getEntity(entityID) != null) {
                    world.getEntity(entityID).setSharedFlag(6, false);
                }
                return 0;
            });
        }
    }

    public static void glowSurroundTamed(LivingEntity centralEntity, int timeInTicks, double radius, @Nullable Predicate<? super Entity> excludeEntity) {
        if (excludeEntity == null) {
            excludeEntity = (Predicate<Entity>) notExclude -> true;
        }
        List<Entity> entities = centralEntity.level.getEntities(centralEntity,
                (new AABB(centralEntity.getX(), centralEntity.getY(), centralEntity.getZ(),
                        centralEntity.getX() + 1.0d, centralEntity.getY() + 1.0d, centralEntity.getZ() + 1.0d)
                        .inflate(radius)),
                ((Predicate<Entity>) entityGet -> !entityGet.isSpectator()
                        && entityGet.isPickable()
                        && (entityGet instanceof LivingEntity)
                        && util.isOwner((LivingEntity) entityGet, centralEntity))
                        .and(excludeEntity));
        for (Entity entity :
                entities) {
            setGlowing(entity, timeInTicks);
        }
    }
}
