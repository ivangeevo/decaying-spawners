package org.btwr.decaying_spawners.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.btwr.decaying_spawners.util.DecayableSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {

    @Inject(
            method = "useOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Spawner;setEntityId(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/util/RandomSource;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void resetDecayOnEggUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if (level.isClientSide) return;

        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawnerBE) {
            BaseSpawner spawner = spawnerBE.getSpawner();
            if (spawner instanceof DecayableSpawner ds && ds.decayingSpawners$isDecayed()) {
                ds.decayingSpawners$resetDecay();
                spawnerBE.setChanged();
                //level.playSound(null, pos, SoundEvents.CONDUIT_ACTIVATE, SoundSource.BLOCKS, 1.2f, 0.6f);
            }
        }
    }

}
