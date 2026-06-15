package org.btwr.decaying_spawners;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.btwr.decaying_spawners.config.ModConfig;

public class DecayingSpawnersMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        CommonClass.init();
        ModConfig.init(FabricLoader.getInstance().getConfigDir());

        // Reload config every time a world is loaded
        ServerLifecycleEvents.SERVER_STARTING.register(server -> ModConfig.load());
    }
}
