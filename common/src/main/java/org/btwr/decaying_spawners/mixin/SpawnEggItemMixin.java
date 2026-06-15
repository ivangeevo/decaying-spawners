package org.btwr.decaying_spawners.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import org.btwr.decaying_spawners.util.SpawnerDecayHandler;
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
        SpawnerDecayHandler.resetDecayOnEggUse(context.getLevel(), context.getClickedPos());
    }

}