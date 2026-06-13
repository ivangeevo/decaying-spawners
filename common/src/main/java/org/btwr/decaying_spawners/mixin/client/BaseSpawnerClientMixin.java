package org.btwr.decaying_spawners.mixin.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.btwr.decaying_spawners.util.DecayableSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerClientMixin {

    @Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
    private void skipParticlesIfDecayed(Level level, BlockPos pos, CallbackInfo ci) {
        BaseSpawner self = (BaseSpawner) (Object) this;
        if (self instanceof DecayableSpawner ds && ds.decayingSpawners$isDecayed()) {
            ci.cancel();
        }
    }
}
