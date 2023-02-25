package com.github.quinnfrost.dragontongue.iceandfire.pathfinding.raycoms.pathjobs;

public interface ICustomSizeNavigator {

    boolean isSmallerThanBlock();
    float getXZNavSize();
    int getYNavSize();
}
