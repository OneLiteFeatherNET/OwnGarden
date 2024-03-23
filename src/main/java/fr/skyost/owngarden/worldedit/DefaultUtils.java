package fr.skyost.owngarden.worldedit;

import org.bukkit.Location;

import java.io.File;
import java.util.List;

public class DefaultUtils implements Utils{

    @Override
    public File[] testSchematics() {
        return new File[0];
    }

    @Override
    public boolean growTree(final List<File> schematics, final Location location) {
        return false;
    }
}
