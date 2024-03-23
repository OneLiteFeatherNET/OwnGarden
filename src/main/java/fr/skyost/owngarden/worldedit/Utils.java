package fr.skyost.owngarden.worldedit;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.OwnGarden;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

public interface Utils {

    Random rnd = new SecureRandom();

    /**
     * Accepted WorldEdit versions.
     */
    String[] WORLDEDIT_VERSIONS = {"7.2", "7.3"};

    /**
     * Returns whether the current WorldEdit version should be accepted.
     *
     * @return Whether the current WorldEdit version should be accepted.
     */

    static boolean checkWorldEditVersion(final OwnGarden plugin) {
        final Plugin we = Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (we == null) {
            plugin.log(NamedTextColor.RED, "WorldEdit must be installed on your server !");
            return false;
        }
        final String version = we.getPluginMeta().getVersion();
        for (final String prefix : WORLDEDIT_VERSIONS) {
            if (version.startsWith(prefix)) {
                return true;
            }
        }
        plugin.log(NamedTextColor.RED, "Incorrect WorldEdit version. Current accepted ones are : "
            + Joiner.on(", ").join(WorldEditUtils.WORLDEDIT_VERSIONS) + ".");
        return false;
    }

    /**
     * Tests if each schematic is valid.
     *
     * @return An array containing the invalid schematics.
     */

    String[] testSchematics();

    /**
     * Grows a tree at the specified location.
     *
     * @param schematics The schematics list.
     * @param location The location.
     *
     * @return Whether the operation has been a success.
     */

    boolean growTree(final List<String> schematics, final Location location);


}
