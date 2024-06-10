package me.itzjustsamu.playerskills.util.path;

import me.hsgamer.hscore.config.Config;
import me.hsgamer.hscore.config.PathString;
import me.hsgamer.hscore.config.path.AdvancedConfigPath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class IntegerMapConfigPath extends AdvancedConfigPath<Map<String, Object>, Map<Integer, Integer>> {
    public IntegerMapConfigPath(@NotNull PathString path, @Nullable Map<Integer, Integer> def) {
        super(path, def);
    }

    @Override
    public @Nullable Map<String, Object> getFromConfig(@NotNull Config config) {
        return PathString.toPathMap(config.getNormalizedValues(getPath(), false));
    }

    @Override
    public @Nullable Map<Integer, Integer> convert(@NotNull Map<String, Object> rawValue) {
        Map<Integer, Integer> map = new HashMap<>();
        rawValue.forEach((k, v) -> {
            int ki;
            int vi;
            try {
                ki = Integer.parseInt(k);
            } catch (Exception e) {
                return;
            }
            try {
                vi = Integer.parseInt(String.valueOf(v));
            } catch (Exception e) {
                return;
            }
            map.put(ki, vi);
        });
        return map;
    }

    @Override
    public @Nullable Map<String, Object> convertToRaw(@NotNull Map<Integer, Integer> value) {
        Map<String, Object> map = new HashMap<>();
        value.forEach((k, v) -> map.put(String.valueOf(k), v));
        return map;
    }
}
