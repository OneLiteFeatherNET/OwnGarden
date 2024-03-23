package fr.skyost.owngarden.worldedit;

import org.bukkit.Location;

import java.util.List;

public class DefaultUtils implements Utils{

    @Override
    public String[] testSchematics() {
        return new String[0];
    }

    @Override
    public boolean growTree(final List<String> schematics, final Location location) {
        return false;
    }
}
