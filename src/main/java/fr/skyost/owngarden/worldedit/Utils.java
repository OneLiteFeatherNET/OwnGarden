package fr.skyost.owngarden.worldedit;

import fr.skyost.owngarden.OwnGarden;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

public interface Utils {

    Random rnd = new SecureRandom();

    /**
     * Accepted WorldEdit versions.
     */
    String[] FAWE_VERSIONS = {"2.9.1", "2.9.2"};

    /**
     * Returns whether the current WorldEdit version should be accepted.
     *
     * @return Whether the current WorldEdit version should be accepted.
     */

    static boolean checkWorldEditVersion(final OwnGarden plugin) {
        final Plugin we = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
        if (we == null) {
            plugin.logger.info(Component.text("FAWE must be installed on your server !", NamedTextColor.RED));
            return false;
        }
        return true;

        /*final String version = we.getPluginMeta().getVersion();
        for (final String prefix : FAWE_VERSIONS) {
            if (version.startsWith(prefix)) {
                return true;
            }
        }
        plugin.log(NamedTextColor.RED, "Incorrect WorldEdit version " + version + ". Current accepted ones are : "
            + Joiner.on(", ").join(WorldEditUtils.FAWE_VERSIONS) + ".");
        return false;*/ // FAWE methods are (probably) stable with versions
    }

    /**
     * Tests if each schematic is valid.
     *
     * @return An array containing the invalid schematics.
     */

    File[] testSchematics();

    /**
     * Grows a tree at the specified location.
     *
     * @param schematics The schematics list.
     * @param location The location.
     *
     * @return Whether the operation has been a success.
     */

    boolean growTree(final List<File> schematics, final Location location);


}
