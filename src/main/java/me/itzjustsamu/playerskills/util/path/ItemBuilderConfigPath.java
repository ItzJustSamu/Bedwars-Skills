package me.itzjustsamu.playerskills.util.path;

import me.hsgamer.hscore.bukkit.item.BukkitItemBuilder;
import me.hsgamer.hscore.bukkit.item.modifier.AmountModifier;
import me.hsgamer.hscore.bukkit.item.modifier.ItemFlagModifier;
import me.hsgamer.hscore.bukkit.item.modifier.LoreModifier;
import me.hsgamer.hscore.bukkit.item.modifier.NameModifier;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.config.Config;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.path.AdvancedConfigPath;
import me.hsgamer.hscore.minecraft.item.ItemBuilder;
import me.hsgamer.hscore.minecraft.item.ItemModifier;
import me.itzjustsamu.playerskills.util.CommonStringReplacer;
import me.itzjustsamu.playerskills.util.modifier.XMaterialModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ItemBuilderConfigPath extends AdvancedConfigPath<Map<String, Object>, ItemBuilder<ItemStack>> {
    public ItemBuilderConfigPath(@NotNull PathString path, @Nullable ItemBuilder<ItemStack> def) {
        super(path, def != null ? addDefault(def) : null);
    }

    public static ItemBuilder<ItemStack> addDefault(ItemBuilder<ItemStack> builder) {
        return builder
                .addItemModifier(new ItemFlagModifier().setFlag(ItemFlag.values()))
                .addStringReplacer(CommonStringReplacer.COLORIZE)
                .addStringReplacer(CommonStringReplacer.PLAYER_PROPERTIES);
    }

    @Override
    public @Nullable Map<String, Object> getFromConfig(@NotNull Config config) {
        return Optional.of(config.getNormalizedValues(getPath(), false))
                .filter(map -> !map.isEmpty())
                .map(PathString::toPathMap)
                .orElse(null);
    }

    @Override
    public @Nullable ItemBuilder<ItemStack> convert(@NotNull Map<String, Object> rawValue) {
        ItemBuilder<ItemStack> itemBuilder = new BukkitItemBuilder();
        XMaterialModifier materialModifier = new XMaterialModifier();
        if (rawValue.containsKey("material")) {
            materialModifier.loadFromObject(rawValue.get("material"));
        } else if (rawValue.containsKey("type")) {
            materialModifier.loadFromObject(rawValue.get("type"));
        } else if (rawValue.containsKey("item")) {
            materialModifier.loadFromObject(rawValue.get("item"));
        }
        if (materialModifier.toObject() != null) {
            itemBuilder.addItemModifier(materialModifier);
        }
        if (rawValue.containsKey("amount")) {
            itemBuilder.addItemModifier(new AmountModifier().setAmount(String.valueOf(rawValue.get("amount"))));
        }
        if (rawValue.containsKey("name")) {
            itemBuilder.addItemModifier(new NameModifier().setName(String.valueOf(rawValue.get("name"))));
        }
        if (rawValue.containsKey("lore")) {
            itemBuilder.addItemModifier(new LoreModifier().setLore(CollectionUtils.createStringListFromObject(rawValue.get("lore"), false)));
        }
        return addDefault(itemBuilder);
    }

    @Override
    public @Nullable Map<String, Object> convertToRaw(@NotNull ItemBuilder<ItemStack> value) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (ItemModifier<ItemStack> modifier : value.getItemModifiers()) {
            if (modifier instanceof XMaterialModifier) {
                map.put("material", modifier.toObject());
            } else if (modifier instanceof AmountModifier) {
                map.put("amount", modifier.toObject());
            } else if (modifier instanceof NameModifier) {
                map.put("name", modifier.toObject());
            } else if (modifier instanceof LoreModifier) {
                map.put("lore", modifier.toObject());
            }
        }
        return map;
    }
}
