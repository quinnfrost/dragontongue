package com.github.quinnfrost.dragontongue.proxy;

import com.github.quinnfrost.dragontongue.DragonTongue;

public interface IProxy {
    public void commonInit();

    public void clientInit();

    public default void test() {
        DragonTongue.LOGGER.info("Running test on");
    }

}
