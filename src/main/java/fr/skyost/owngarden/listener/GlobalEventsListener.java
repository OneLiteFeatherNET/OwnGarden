package fr.skyost.owngarden.listener;

import fr.skyost.owngarden.OwnGarden;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.List;

/**
 * Global events handled by the plugin.
 */
public record GlobalEventsListener(OwnGarden plugin) implements Listener {

    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST,
        BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    /**
     * Returns the plugin instance.
     *
     * @return The plugin instance.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onStructureGrow(final StructureGrowEvent event) {
        final Location location = event.getLocation();
        final List<String> schematics = plugin.pluginConfig.getSchematics(location.getBlock().getType());
        if (plugin.worldEditOperations.growTree(schematics, location)) {
            /*if (schematics == plugin.pluginConfig.saplingDarkOakSchematics) {
                val current = location.block
                for (blockFace in FACES) {
                    val relative = current.getRelative(blockFace)
                    if (relative.type == Material.DARK_OAK_SAPLING) {
                        relative.type = Material.AIR;
                    }
                }
            }*/ // spruce trees can do that too
            final Block current = location.getBlock();
            for (final BlockFace blockFace : FACES) {
                final Block relative = current.getRelative(blockFace);
                if (relative.getType() == Material.DARK_OAK_SAPLING) {
                    relative.setType(Material.AIR, false);
                }
            }

            //event.getBlocks().clear();
            event.setCancelled(true);
        }
    }
}