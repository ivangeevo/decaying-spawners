package org.btwr.decaying_spawners;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.btwr.decaying_spawners.config.ModConfig;

@Mod(Constants.MOD_ID)
public class DecayingSpawnersMod {

    public DecayingSpawnersMod(IEventBus eventBus) {
        CommonClass.init();
        ModConfig.init(FMLPaths.CONFIGDIR.get());

        // Reload config every time a world is loaded
        NeoForge.EVENT_BUS.addListener((ServerStartingEvent e) -> ModConfig.load());
    }

}