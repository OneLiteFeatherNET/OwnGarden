package fr.skyost.owngarden.worldedit;

import org.bukkit.Location;

import java.nio.file.Path;

public class DefaultUtils implements Utils{

    @Override
    public boolean testSchematics() {
        return false;
    }

    @Override
    public boolean growTree(final Path schematic, final Location location) {
        return false;
    }
}
