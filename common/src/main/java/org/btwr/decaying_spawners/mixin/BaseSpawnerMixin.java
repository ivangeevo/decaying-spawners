package org.btwr.decaying_spawners.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.btwr.decaying_spawners.util.DecayableSpawner;
import org.btwr.decaying_spawners.util.SpawnerDecayConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin implements DecayableSpawner {

    @Unique private int mobSpawnCount = 0;
    @Unique private int spawnerTickCount = 0;
    @Unique private boolean decayed = false;

    @Override
    public boolean decayingSpawners$isDecayed() {
        return this.decayed;
    }

    @Override
    public void decayingSpawners$resetDecay() {
        this.decayed = false;
        this.mobSpawnCount = 0;
        this.spawnerTickCount = 0;
    }

    // Runs after a successful spawn batch
    @Inject(method = "serverTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;levelEvent(ILnet/minecraft/core/BlockPos;I)V"
            )
    )
    private void onMobSpawned(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
        this.mobSpawnCount++;
        this.spawnerTickCount++;
        decayingSpawners$checkDecay(serverLevel, pos);
    }

    // Tick-based decay
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void onServerTickStart(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
        if (this.decayed) {
            ci.cancel();
            return;
        }

        if (SpawnerDecayConfig.maxSpawnerTicks > 0) {
            this.spawnerTickCount++;

            if (this.spawnerTickCount >= SpawnerDecayConfig.maxSpawnerTicks) {
                decayingSpawners$decay(serverLevel, pos);
                ci.cancel();
            }
        }
    }

    @Unique
    private void decayingSpawners$checkDecay(ServerLevel level, BlockPos pos) {
        if (this.decayed) return;
        boolean mobCapHit = SpawnerDecayConfig.maxMobSpawnCount > 0
                && this.mobSpawnCount >= SpawnerDecayConfig.maxMobSpawnCount;
        if (mobCapHit) {
            decayingSpawners$decay(level, pos);
        }
    }

    @Unique
    private void decayingSpawners$decay(Level level, BlockPos pos) {
        this.decayed = true;

        // Trigger a block update so the block entity/renderer gets notified
        BlockState state = level.getBlockState(pos);
        //level.playSound(null, pos, SoundEvents.CONDUIT_DEACTIVATE, SoundSource.BLOCKS, 1.5F, 0.4F);
        level.sendBlockUpdated(pos, state, state, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
    }

    // Persist to NBT
    @Inject(method = "save", at = @At("TAIL"))
    private void saveDecayData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putInt("MobSpawnCount", this.mobSpawnCount);
        cir.getReturnValue().putInt("SpawnerTickCount", this.spawnerTickCount);
        cir.getReturnValue().putBoolean("Decayed", this.decayed);
    }

    // Load from NBT
    @Inject(method = "load", at = @At("TAIL"))
    private void loadDecayData(Level level, BlockPos pos, CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("MobSpawnCount")) {
            this.mobSpawnCount = tag.getInt("MobSpawnCount");
        }
        if (tag.contains("SpawnerTickCount")) {
            this.spawnerTickCount = tag.getInt("SpawnerTickCount");
        }
        if (tag.contains("Decayed")) {
            this.decayed = tag.getBoolean("Decayed");
        }
    }

}