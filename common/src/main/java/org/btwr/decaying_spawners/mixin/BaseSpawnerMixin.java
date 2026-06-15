package org.btwr.decaying_spawners.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.btwr.decaying_spawners.util.DecayTracker;
import org.btwr.decaying_spawners.util.DecayableSpawner;
import org.btwr.decaying_spawners.util.SpawnerDecayHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin implements DecayableSpawner, DecayTracker {

    @Unique private int mobSpawnCount = 0;
    @Unique private int spawnerTickCount = 0;
    @Unique private boolean decayed = false;

    // DecayableSpawner
    @Override public boolean decayingSpawners$isDecayed() { return decayed; }
    @Override public void decayingSpawners$resetDecay() { reset(); }

    // DecayTracker
    @Override public boolean isDecayed() { return decayed; }
    @Override public void setDecayed(boolean v) { decayed = v; }
    @Override public int getMobCount() { return mobSpawnCount; }
    @Override public void setMobCount(int v) { mobSpawnCount = v; }
    @Override public void incrementMobCount() { mobSpawnCount++; }
    @Override public int getTickCount() { return spawnerTickCount; }
    @Override public void setTickCount(int v) { spawnerTickCount = v; }
    @Override public void incrementTickCount() { spawnerTickCount++; }
    @Override public void reset() { decayed = false; mobSpawnCount = 0; spawnerTickCount = 0; }

    @Inject(method = "serverTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"))
    private void onMobSpawned(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
        SpawnerDecayHandler.onMobSpawned((BaseSpawner)(Object)this, this, serverLevel, pos);
    }

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void onServerTickStart(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
        if (SpawnerDecayHandler.onTickStart((BaseSpawner)(Object)this, this, serverLevel, pos)) {
            ci.cancel();
        }
    }

    @Inject(method = "save", at = @At("TAIL"))
    private void saveDecayData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        SpawnerDecayHandler.saveDecayData(this, cir.getReturnValue());
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void loadDecayData(Level level, BlockPos pos, CompoundTag tag, CallbackInfo ci) {
        SpawnerDecayHandler.loadDecayData(this, tag);
    }
}