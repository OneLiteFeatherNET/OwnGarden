package fr.skyost.owngarden.worldedit;

import org.bukkit.Location;

import java.nio.file.Path;

public class DefaultTreeAPI implements TreeAPI {

    @Override
    public boolean testSchematics() {
        return false;
    }

    @Override
    public GrowState growTree(final Path schematic, final Location location) {
        return GrowState.UNKNOWN;
    }
}
