package fr.skyost.owngarden.data;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record MaterialSchems(Material material, List<Path> schematics) implements ConfigurationSerializable {

    private static final String MAT = "material";
    private static final String SCHEMS = "schematics";

    @Override
    public @NotNull Map<String, Object> serialize() {
        final Map<String, Object> serials = new HashMap<>();
        serials.put(MAT, material.name());
        serials.put(SCHEMS, schematics.stream()
            .map(Path::toString).toList());
        return serials;
    }

    @SuppressWarnings("unchecked")
    public static MaterialSchems deserialize(Map<String, Object> args) {
        return new MaterialSchems(Material.matchMaterial((String) args.get(MAT)),
            ((List<String>) args.get(SCHEMS)).stream().map(Path::of).toList());
    }
}
