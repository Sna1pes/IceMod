package com.example.icemod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class IceMod implements ModInitializer {
    public static final String MOD_ID = "icemod";

    // Ледяная булава: высокий урон, медленная атака, спавнит лёд
    public static final Item ICE_MACE = registerItem("ice_mace",
            new IceWeaponItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .attributeModifiers(createWeaponAttributes(9.0, -3.2)),
                    true
            ));

    // Ледяное копьё: средний урон, быстрее, без льда
    public static final Item ICE_SPEAR = registerItem("ice_spear",
            new IceWeaponItem(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
                    .attributeModifiers(createWeaponAttributes(7.0, -2.4)),
                    false
            ));

    private static AttributeModifiersComponent createWeaponAttributes(double attackDamage, double attackSpeed) {
        return AttributeModifiersComponent.builder()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID, attackDamage, EntityAttributeModifier.Operation.ADD_VALUE),
                        net.minecraft.component.type.AttributeModifierSlot.MAINHAND)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(Item.BASE_ATTACK_SPEED_MODIFIER_ID, attackSpeed, EntityAttributeModifier.Operation.ADD_VALUE),
                        net.minecraft.component.type.AttributeModifierSlot.MAINHAND)
                .build();
    }

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(MOD_ID, name), item);
    }

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(ICE_MACE);
            entries.add(ICE_SPEAR);
        });
    }
                              }
