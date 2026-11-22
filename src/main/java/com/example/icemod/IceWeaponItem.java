package com.example.icemod;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IceWeaponItem {

    private static final int COOLDOWN_TICKS = 20 * 30;   // 30 секунд кулдауна ульты
    private static final int EFFECT_TICKS = 20 * 3;      // 3 секунды на эффекты
    private static final int FROZEN_TICKS = 100;         // 5 секунд заморозки
    private static final int ICE_REMOVE_TICKS = 20 * 15; // лёд исчезает через 15 секунд

    // --- ЛЕДЯНАЯ БУЛАВА ---
    // Наследует ванильную MaceItem: удар с высоты и чары булавы работают как в ванилле.
    // Ульта срабатывает ТОЛЬКО при ударе — цель всегда та, в кого попал.
    public static class IceMace extends MaceItem {
        public IceMace(Item.Settings settings) {
            super(settings);
        }

        @Override
        public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker instanceof PlayerEntity player
                    && !player.getEntityWorld().isClient()
                    && !player.getItemCooldownManager().isCoolingDown(stack)) {
                activateUltimate(player.getEntityWorld(), player, target, true, stack);
            }
            super.postHit(stack, target, attacker);
        }
    }

    // --- ЛЕДЯНОЕ КОПЬЁ ---
    // Ульты нет: механика копья живёт в компонентах (см. IceMod.java).
    public static class IceSpear extends Item {
        public IceSpear(Item.Settings settings) {
            super(settings);
        }
    }

    // Ульта по удару: цель замирает на 5 секунд, замедление и слепота на 3 секунды,
    // под целью появляется лёд 3x3 и исчезает через 15 секунд
    static void activateUltimate(World world, PlayerEntity user, LivingEntity target, boolean spawnIce, ItemStack stack) {
        user.getItemCooldownManager().set(stack, COOLDOWN_TICKS);

        target.setVelocity(0, 0, 0);
        target.setFrozenTicks(FROZEN_TICKS);
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, EFFECT_TICKS, 255, false, false, true));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, EFFECT_TICKS, 0, false, false, true));

        if (spawnIce) {
            BlockPos centerPos = target.getBlockPos().down();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos icePos = centerPos.add(x, 0, z);
                    if (world.getBlockState(icePos).getHardness(world, icePos) >= 0) {
                        world.setBlockState(icePos, Blocks.ICE.getDefaultState());
                        IceRemovalScheduler.schedule(world, icePos, ICE_REMOVE_TICKS);
                    }
                }
            }
        }

        world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 0.8f);
    }
              }
