package com.example.icemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class IceMod implements ModInitializer {
    public static final String MOD_ID = "icemod";

    public static final Item ICE_MACE = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "ice_mace"),
            new IceWeaponItem.IceMace()
    );

    public static final Item ICE_SPEAR = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "ice_spear"),
            new IceWeaponItem.IceSpear()
    );

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ICE_MACE);
            entries.add(ICE_SPEAR);
        });
    }
}
