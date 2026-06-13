package org.btwr.decaying_spawners.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.btwr.decaying_spawners.util.DecayableSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnerBlockEntity.class)
public abstract class SpawnerBlockEntityMixin {

    @Inject(method = "getUpdateTag", at = @At("TAIL"))
    private void addDecayToUpdateTag(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        SpawnerBlockEntity self = (SpawnerBlockEntity) (Object) this;
        BaseSpawner spawner = self.getSpawner();

        if (spawner instanceof DecayableSpawner ds) {
            cir.getReturnValue().putBoolean("Decayed", ds.decayingSpawners$isDecayed());
        }
    }
}
