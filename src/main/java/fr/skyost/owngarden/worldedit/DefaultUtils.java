package fr.skyost.owngarden.worldedit;

import org.bukkit.Location;

import java.io.File;

public class DefaultUtils implements Utils{

    @Override
    public File[] testSchematics() {
        return new File[0];
    }

    @Override
    public boolean growTree(final File schematic, final Location location) {
        return false;
    }
}
