package org.btwr.decaying_spawners.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.btwr.decaying_spawners.Constants;
import org.btwr.decaying_spawners.config.ModConfig;
import org.btwr.decaying_spawners.mixin.BaseSpawnerAccessor;

public class SpawnerDecayHandler {

    private SpawnerDecayHandler() {}

    // --- BaseSpawnerMixin logic ---

    public static boolean isExcluded(BaseSpawner spawner) {
        if (ModConfig.excludedSpawners.isEmpty()) return false;

        SpawnData spawnData = ((BaseSpawnerAccessor) spawner).getNextSpawnData();
        if (spawnData == null) return false;

        String entityId = spawnData.getEntityToSpawn().getString("id");
        if (entityId.isEmpty()) return false;

        return ModConfig.excludedSpawners.contains(entityId);
    }

    public static void onMobSpawned(BaseSpawner spawner, DecayTracker tracker, ServerLevel level, BlockPos pos) {
        if (isExcluded(spawner)) return;
        tracker.incrementMobCount();
        checkMobDecay(tracker, level, pos);
    }

    public static boolean onTickStart(BaseSpawner spawner, DecayTracker tracker, ServerLevel level, BlockPos pos) {
        if (tracker.isDecayed()) return true; // signal: cancel tick
        if (isExcluded(spawner)) return false;

        if (ModConfig.maxSpawnerTicks > 0) {
            tracker.incrementTickCount();
            if (tracker.getTickCount() >= ModConfig.maxSpawnerTicks) {
                decay(tracker, level, pos);
                return true; // signal: cancel tick
            }
        }
        return false;
    }

    public static void checkMobDecay(DecayTracker tracker, ServerLevel level, BlockPos pos) {
        Constants.LOG.info(
                "Checking decay: count={}, max={}",
                tracker.getMobCount(),
                ModConfig.maxMobSpawnCount
        );
        if (tracker.isDecayed()) return;
        if (ModConfig.maxMobSpawnCount > 0 && tracker.getMobCount() >= ModConfig.maxMobSpawnCount) {
            decay(tracker, level, pos);
        }
    }

    public static void decay(DecayTracker tracker, Level level, BlockPos pos) {
        tracker.setDecayed(true);
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
    }

    public static void saveDecayData(DecayTracker tracker, CompoundTag tag) {
        tag.putInt("MobSpawnCount", tracker.getMobCount());
        tag.putInt("SpawnerTickCount", tracker.getTickCount());
        tag.putBoolean("Decayed", tracker.isDecayed());
    }

    public static void loadDecayData(DecayTracker tracker, CompoundTag tag) {
        if (tag.contains("MobSpawnCount")) tracker.setMobCount(tag.getInt("MobSpawnCount"));
        if (tag.contains("SpawnerTickCount")) tracker.setTickCount(tag.getInt("SpawnerTickCount"));
        if (tag.contains("Decayed")) tracker.setDecayed(tag.getBoolean("Decayed"));
    }

    // --- SpawnEggItemMixin logic ---

    public static void resetDecayOnEggUse(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawnerBE) {
            BaseSpawner spawner = spawnerBE.getSpawner();
            if (spawner instanceof DecayableSpawner ds && ds.decayingSpawners$isDecayed()) {
                ds.decayingSpawners$resetDecay();
                spawnerBE.setChanged();
            }
        }
    }

    // --- SpawnerBlockEntityMixin logic ---

    public static void addDecayToUpdateTag(SpawnerBlockEntity be, CompoundTag tag) {
        BaseSpawner spawner = be.getSpawner();
        if (spawner instanceof DecayableSpawner ds) {
            tag.putBoolean("Decayed", ds.decayingSpawners$isDecayed());
        }
    }
}

