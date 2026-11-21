package com.example.icemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.Component;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.function.Function;

public class IceMod implements ModInitializer {
    public static final String MOD_ID = "icemod";

    public static final Item ICE_MACE = register("ice_mace",
            IceWeaponItem.IceMace::new,
            copyComponentsFrom(new ItemStack(Items.MACE),
                    new Item.Settings()
                            .maxCount(1)
                            .rarity(Rarity.EPIC)));

    public static final Item ICE_SPEAR = register("ice_spear",
            IceWeaponItem.IceSpear::new,
            copyComponentsFrom(new ItemStack(Items.NETHERITE_SPEAR),
                    new Item.Settings()
                            .maxCount(1)
                            .rarity(Rarity.EPIC)));

    // Копирует ВСЕ компоненты ванильного предмета в настройки нового:
    // урон, прочность, зачаровываемость, механики укола и удара с разбега
    private static Item.Settings copyComponentsFrom(ItemStack source, Item.Settings settings) {
        for (Component<?> component : source.getComponents()) {
            putComponent(settings, component);
        }
        return settings;
    }

    private static <T> void putComponent(Item.Settings settings, Component<T> component) {
        settings.component(component.type(), component.value());
    }

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
        Item item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ICE_MACE);
            entries.add(ICE_SPEAR);
        });

        // Тикер, который убирает лёд по таймеру (через 15 секунд после появления)
        ServerTickEvents.END_SERVER_TICK.register(IceRemovalScheduler::tick);
    }
          }
