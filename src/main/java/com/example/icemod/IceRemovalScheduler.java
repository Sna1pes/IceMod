package com.example.icemod;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class IceRemovalScheduler {

    private IceRemovalScheduler() {}

    // Ключ = измерение + позиция, значение = тик, когда лёд надо убрать
    private record IceKey(RegistryKey<World> dimension, BlockPos pos) {}

    private static final Map<IceKey, Long> ICE_TO_REMOVE = new HashMap<>();

    public static void schedule(World world, BlockPos pos, int delayTicks) {
        ICE_TO_REMOVE.put(
                new IceKey(world.getRegistryKey(), pos.toImmutable()),
                world.getTime() + delayTicks
        );
    }

    public static void tick(MinecraftServer server) {
        if (ICE_TO_REMOVE.isEmpty()) return;

        long now = server.overworld().getTime();
        Iterator<Map.Entry<IceKey, Long>> it = ICE_TO_REMOVE.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<IceKey, Long> entry = it.next();
            if (now < entry.getValue()) continue;
            it.remove();

            ServerWorld world = server.getWorld(entry.getKey().dimension());
            if (world == null) continue;

            BlockPos pos = entry.getKey().pos();
            // Убираем только если игрок не заменил блок на что-то своё
            if (world.getBlockState(pos).isOf(Blocks.ICE)) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            }
        }
    }
          }
          
