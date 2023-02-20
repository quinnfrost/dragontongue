package com.github.quinnfrost.dragontongue.mixin.iceandfire;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.AbstractAdvancedPathNavigate;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathResult;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;
import com.github.quinnfrost.dragontongue.access.IMixinAdvancedPathNavigate;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(AdvancedPathNavigate.class)
public abstract class MixinAdvancedPathNavigate extends AbstractAdvancedPathNavigate implements IMixinAdvancedPathNavigate {
    @Shadow(remap = false) @Nullable private PathResult<AbstractPathJob> pathResult;

    @Shadow(remap = false) private long pathStartTime;

    public MixinAdvancedPathNavigate(MobEntity entityLiving, World worldIn) {
        super(entityLiving, worldIn);
    }

    @Override
    public PathResult<AbstractPathJob> getPathResult() {
        return pathResult;
    }

    @Override
    public long getPathStartTime() {
        return pathStartTime;
    }
}
