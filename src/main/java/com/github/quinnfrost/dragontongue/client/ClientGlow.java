package com.github.quinnfrost.dragontongue.client;

import com.github.quinnfrost.dragontongue.DragonTongue;
import com.github.quinnfrost.dragontongue.utils.util;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ClientGlow {
    public static Map<Integer, Integer> glowList;
    public static World world = Minecraft.getInstance().world;

    static {
        glowList = new HashMap<>();
    }

    public static void setGlowing(Entity entity, int timeInTick) {
        if (entity != null) {
            world = entity.world;
            glowList.put(entity.getEntityId(), timeInTick);
            entity.setGlowing(true);
        }
    }

    public static void tickGlowing() {
        glowList.values().removeIf(integer -> integer < 0);
        if (!glowList.isEmpty()) {
            glowList.replaceAll((entityID, time) -> {
                if (time > 0) {
                    return time - 1;
                } else if (world.getEntityByID(entityID) != null) {
                    world.getEntityByID(entityID).setGlowing(false);
                } else {
                    DragonTongue.LOGGER.warn("No world in scope");
                }
                return 0;
            });
        }
    }

    public static void glowSurroundTamed(LivingEntity centralEntity, int timeInTicks, double radius, @Nullable Predicate<? super Entity> excludeEntity) {
        if (excludeEntity == null) {
            excludeEntity = (Predicate<Entity>) notExclude -> true;
        }
        List<Entity> entities = centralEntity.world.getEntitiesInAABBexcluding(centralEntity,
                (new AxisAlignedBB(centralEntity.getPosX(), centralEntity.getPosY(), centralEntity.getPosZ(),
                        centralEntity.getPosX() + 1.0d, centralEntity.getPosY() + 1.0d, centralEntity.getPosZ() + 1.0d)
                        .grow(radius)),
                ((Predicate<Entity>) entityGet -> !entityGet.isSpectator()
                        && entityGet.canBeCollidedWith()
                        && (entityGet instanceof LivingEntity)
                        && util.isOwner((LivingEntity) entityGet, centralEntity))
                        .and(excludeEntity));
        for (Entity entity :
                entities) {
            setGlowing(entity, timeInTicks);
        }
    }
}
