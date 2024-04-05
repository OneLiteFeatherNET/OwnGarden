package fr.skyost.owngarden.worldedit;

import fr.skyost.owngarden.OwnGarden;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;

public interface TreeAPI {

    /*
     * Accepted WorldEdit versions.
     */
//    String[] FAWE_VERSIONS = {"2.9.1", "2.9.2"};

    /**
     * Returns whether the current WorldEdit version should be accepted.
     *
     * @return Whether the current WorldEdit version should be accepted.
     */

    static boolean checkWorldEditVersion(final OwnGarden plugin) {
        final Plugin we = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");

        if (we == null) {
            ComponentLogger.logger(TreeAPI.class.getSimpleName())
                .info(Component.text("FAWE must be installed on your server !", NamedTextColor.RED));
            return false;
        }
        return true;
    }

    /**
     * Tests if each schematic is valid.
     *
     * @return An array containing the invalid schematics.
     */

    boolean testSchematics();

    /**
     * Grows a tree at the specified location.
     *
     * @param schematic The schematic file.
     * @param location The location.
     *
     * @return Whether the operation has been a success.
     */

    GrowState growTree(final Path schematic, final Location location);

    enum GrowState {
        SUCCESS, LOAD_FAIL, PLACE_FAIL, UNKNOWN, OUT_OF_BOUNDS,
    }
}
