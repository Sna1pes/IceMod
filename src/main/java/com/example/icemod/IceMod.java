package com.example.icemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class IceMod implements ModInitializer {

    public static final String MOD_ID = "icemod";

    // --- ЛЕДЯНАЯ БУЛАВА ---
    public static final Item ICE_MACE = register("ice_mace",
            IceWeaponItem.IceMace::new,
            new Item.Settings()
                    .maxDamage(500)   // прочность как у ванильной булавы
    );

    // --- ЛЕДЯНОЕ КОПЬЁ ---
    // Механика копья в 1.21.11 живёт в компонентах (piercing_weapon, kinetic_weapon
    // и т. д.), поэтому копируем их с ванильного трезубца один в один.
    public static final Item ICE_SPEAR = register("ice_spear",
            IceWeaponItem.IceSpear::new,
            spearSettings()
    );

    // Копируем все компоненты трезубца в настройки копья.
    // Так поведение будет 1:1 как у незеритового копья, даже если в будущих
    // версиях что-то в этих компонентах поменяется.
    @SuppressWarnings("unchecked")
    private static Item.Settings spearSettings() {
        ItemStack trident = new ItemStack(Items.TRIDENT);
        Item.Settings settings = new Item.Settings()
                .maxDamage(250)   // прочность трезубца
                .maxCount(1)
                .fireproof();     // трезубец не горит — копьё тоже
        trident.getComponents().forEach((type, value) ->
                settings.component((ComponentType<Object>) type, value));
        return settings;
    }

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
        return Registry.register(Registries.ITEM, key, factory.apply(settings.registryKey(key)));
    }

    @Override
    public void onInitialize() {
        // Предметы в творческой вкладке «Бой»
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ICE_MACE);
            entries.add(ICE_SPEAR);
        });

        // Тикер, который убирает лёд по таймеру
        ServerTickEvents.END_SERVER_TICK.register(IceRemovalScheduler::tick);
    }
}
