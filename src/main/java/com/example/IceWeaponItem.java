package com.example.icemod;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public class IceWeaponItem extends Item {
    private final boolean spawnIce;
    private static final int COOLDOWN_TICKS = 45 * 20; // 45 секунд
    private static final int EFFECT_TICKS = 3 * 20;    // 3 секунды

    public IceWeaponItem(Settings settings, boolean spawnIce) {
        super(settings);
        this.spawnIce = spawnIce;
    }

    // Активация на ПКМ: ищет ближайшую цель в радиусе 6 блоков
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient) {
            Box searchBox = user.getBoundingBox().expand(6.0);
            List<LivingEntity> targets = world.getEntitiesByClass(
                    LivingEntity.class,
                    searchBox,
                    entity -> entity != user && entity.isAlive() && !entity.isSpectator()
            );

            if (!targets.isEmpty()) {
                targets.sort(Comparator.comparingDouble(user::squaredDistanceTo));
                LivingEntity target = targets.get(0);

                activateUltimate(world, user, target);
                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.pass(stack);
    }

    // Альтернатива: удар по врагу на Shift, если ульта не в КД
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player && player.isSneaking()) {
            World world = player.getWorld();
            if (!world.isClient && !player.getItemCooldownManager().isCoolingDown(this)) {
                activateUltimate(world, player, target);
            }
        }
        return super.postHit(stack, target, attacker);
    }

    private void activateUltimate(World world, PlayerEntity user, LivingEntity target) {
        user.getItemCooldownManager().set(this, COOLDOWN_TICKS);

        target.setVelocity(0, 0, 0);
        target.velocityModified = true;
        target.setFrozenTicks(200);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, EFFECT_TICKS, 255, false, false, true));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, EFFECT_TICKS, 250, false, false, false));

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, EFFECT_TICKS, 0, false, false, true));

        if (this.spawnIce) {
            BlockPos centerPos = target.getBlockPos().down();

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos icePos = centerPos.add(x, 0, z);
                    if (world.getBlockState(icePos).getHardness(world, icePos) >= 0) {
                        world.setBlockState(icePos, Blocks.ICE.getDefaultState());
                    }
                }
            }
        }

        world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 0.8f);
    }
          }
  
