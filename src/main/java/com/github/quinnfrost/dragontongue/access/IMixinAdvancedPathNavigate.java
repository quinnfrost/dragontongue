package com.github.quinnfrost.dragontongue.access;

import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathResult;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob;

@Deprecated
public interface IMixinAdvancedPathNavigate {
    PathResult<AbstractPathJob> getPathResult();
    long getPathStartTime();
}
