package org.btwr.decaying_spawners;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class DecayingSpawnersMod {

    public DecayingSpawnersMod(IEventBus eventBus) {
        CommonClass.init();
    }

}