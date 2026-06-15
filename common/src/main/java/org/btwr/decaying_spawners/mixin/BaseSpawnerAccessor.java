package org.btwr.decaying_spawners.mixin;

import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {
    @Nullable @Accessor("nextSpawnData") SpawnData getNextSpawnData();
}