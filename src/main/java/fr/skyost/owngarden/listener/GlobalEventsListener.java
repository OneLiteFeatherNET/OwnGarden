package fr.skyost.owngarden.listener;

import fr.skyost.owngarden.OwnGarden;
import fr.skyost.owngarden.worldedit.TreeAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * Global events handled by the plugin.
 */
public record GlobalEventsListener(OwnGarden plugin) implements Listener {

    private static final BlockFace[] FACES = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SELF,
        BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onStructureGrow(final StructureGrowEvent event) {
        final Location location = event.getLocation();
        plugin.getTreeService().ifMatMatch(location.getBlock().getType(), schem -> {
            final Block current = location.getBlock();
            final Material type = current.getType();
            if (plugin.getOperations().growTree(schem, location) == TreeAPI.GrowState.SUCCESS) {
                event.setCancelled(true);
                for (final BlockFace blockFace : FACES) {
                    final Block relative = current.getRelative(blockFace);
                    if (relative.getType() == type) {
                        relative.setType(Material.AIR, false);
                    }
                }
            }
        });
    }
}