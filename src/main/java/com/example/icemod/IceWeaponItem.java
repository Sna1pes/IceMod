package com.example.icemod;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IceWeaponItem {

    private static final int COOLDOWN_TICKS = 20 * 45; // 45 секунд кулдауна
    private static final int EFFECT_TICKS = 20 * 3;    // 3 секунды на эффекты
    private static final int FROZEN_TICKS = 100;       // 5 секунд заморозки

    // --- ЛЕДЯНАЯ БУЛАВА ---
    // Наследует ванильную MaceItem: удар с высоты и чары булавы работают как в ванилле.
    // Характеристики и вид — как у ванильной булавы (компоненты Items.MACE).
    public static class IceMace extends MaceItem {
        public IceMace() {
            super(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .copyComponents(new ItemStack(Items.MACE).getComponents()));
        }

        // Ульта при любом ударе (обычном или с высоты)
        @Override
        public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker instanceof PlayerEntity player
                    && !player.getWorld().isClient
                    && !player.getItemCooldownManager().isCoolingDown(this)) {
                activateUltimate(player.getWorld(), player, target, true, this);
            }
            return super.postHit(stack, target, attacker);
        }

        // Ульта на ПКМ: ближайшая цель в радиусе 6 блоков
        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
            ItemStack stack = user.getStackInHand(hand);

            if (user.getItemCooldownManager().isCoolingDown(this)) {
                return TypedActionResult.fail(stack);
            }

            if (!world.isClient) {
                LivingEntity nearest = findNearestTarget(world, user);
                if (nearest != null) {
                    activateUltimate(world, user, nearest, true, this);
                    return TypedActionResult.success(stack);
                }
            }

            return TypedActionResult.pass(stack);
        }
    }

    // --- ЛЕДЯНОЕ КОПЬЁ ---
    // Вид и характеристики НЕЗЕРИТОВОГО копья (компоненты Items.NETHERITE_SPEAR):
    // укол по ЛКМ и удар с разбега по удержанию ПКМ работают как у ванильного копья.
    // ПКМ не переопределяем — она занята зарядом разбега.
    public static class IceSpear extends Item {
        public IceSpear() {
            super(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .copyComponents(new ItemStack(Items.NETHERITE_SPEAR).getComponents()));
        }

        // Ульта при любом ударе (укол ЛКМ или удар с разбега)
        @Override
        public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker instanceof PlayerEntity player
                    && !player.getWorld().isClient
                    && !player.getItemCooldownManager().isCoolingDown(this)) {
                activateUltimate(player.getWorld(), player, target, false, this);
            }
            return super.postHit(stack, target, attacker);
        }
    }

    private static LivingEntity findNearestTarget(World world, PlayerEntity user) {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : world.getEntitiesByClass(
                LivingEntity.class,
                user.getBoundingBox().expand(6.0),
                entity -> entity != user && entity.isAlive() && !entity.isSpectator())) {
            double d = user.squaredDistanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    // Общая ульта: цель замирает на 5 секунд, замедление и слепота на 3 секунды,
    // у булавы под целью появляется лёд 3x3
    static void activateUltimate(World world, PlayerEntity user, LivingEntity target, boolean spawnIce, Item item) {
        user.getItemCooldownManager().set(item, COOLDOWN_TICKS);

        target.setVelocity(0, 0, 0);
        target.velocityModified = true;
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
                    }
                }
            }
        }

        world.playSound(null, target.getBlockPos(), SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1.0f, 0.8f);
    }
  }
          
