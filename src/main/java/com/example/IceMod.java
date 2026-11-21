package com.example.icemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
            new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .copyComponents(new ItemStack(Items.MACE).getComponents()));

    public static final Item ICE_SPEAR = register("ice_spear",
            IceWeaponItem.IceSpear::new,
            new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .copyComponents(new ItemStack(Items.NETHERITE_SPEAR).getComponents()));

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
    }
                                                 }
  
