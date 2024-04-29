package fr.skyost.owngarden.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import fr.skyost.owngarden.OwnGarden;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Represents available WorldEdit operations.
 */
public record WorldEditTreeAPI(OwnGarden plugin) implements TreeAPI {

    private static final Random rnd = new SecureRandom();

    private static final ComponentLogger logger = ComponentLogger.logger(WorldEditTreeAPI.class.getSimpleName());

    /**
     * Tests if each schematic is valid.
     *
     * @return An array containing the invalid schematics.
     */
    public boolean testSchematics() {
        boolean pass = false;
        for (final Map.Entry<Material, List<Path>> en : plugin.getTreeService().getTreeMap().entrySet()) {
            final List<Path> list = new ArrayList<>(en.getValue().size());
            for (final Path end : en.getValue()) {
                final Path schem = plugin.getTreeService().getTreeFolder().resolve(end);
                if (!Files.exists(schem)) {
                    logger.error(Component.text("Schematic not found " + schem, NamedTextColor.RED));
                    pass = true;
                    continue;
                }
                Bukkit.getConsoleSender().sendMessage("Loaded: " + schem);
                list.add(schem);
            }
            en.setValue(list);
        }
        return pass;
    }

    /**
     * Loads a schematic.
     *
     * @param path The schematic file (must be in the schematics directory).
     *
     * @return The WorldEdit clipboard holder.
     *
     * @throws IOException If any I/O exception occurs.
     */

    private ClipboardHolder loadSchematic(final Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Schematic not found : " + path);
        }
        final ClipboardFormat format = ClipboardFormats.findByFile(new File(path.toUri()));
        if (format == null) throw new IOException("Unknown schematic format : " + path);
        return new ClipboardHolder(format.getReader(new BufferedInputStream(Files.newInputStream(path))).read());
    }

    /**
     * Grows a tree at the specified location.
     *
     * @param schematic The schematic file.
     * @param location The location.
     *
     * @return Whether the operation has been a success.
     */

    public GrowState growTree(final Path schematic, final Location location) {
        try (final ClipboardHolder holder = loadSchematic(schematic)) {
            location.getBlock().setType(Material.AIR, false);
            if (plugin.getTreeService().isRandomRotation()) {
                final int degrees = rnd.nextInt(4) * 90;
                if (degrees != 0) {
                    final AffineTransform transform = new AffineTransform();
                    transform.rotateY(degrees);
                    holder.setTransform(transform);
                }
            }
            final Clipboard clip = holder.getClipboards().get(0);
            final BlockVector3 dimensions = clip.getDimensions();
            if (plugin.getTreeService().isCheckHeight() && !checkHeight(dimensions, location)) {
                return GrowState.OUT_OF_BOUNDS;
            }
            clip.setOrigin(BlockVector3.at(dimensions.getBlockX() >> 1, 0, dimensions.getBlockZ() >> 1));
            try (final EditSession session = WorldEdit.getInstance().newEditSession(new BukkitWorld(location.getWorld()))) {
                final Operation operation = holder
                    .createPaste(session)
                    .to(BlockVector3.at(location.getBlockX(),
                            location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(true)
                    .build();
                Operations.completeLegacy(operation);
            }

            return GrowState.SUCCESS;
        } catch (IOException ex) {
            logger.info(Component.text("Unable to load the schematic : " + schematic.toString(), NamedTextColor.RED), ex);
            return GrowState.LOAD_FAIL;
        } catch (MaxChangedBlocksException ex) {
            logger.info(Component.text("Unable to place schematic : " + schematic.toString(), NamedTextColor.RED), ex);
            return GrowState.PLACE_FAIL;
        }
    }

    /**
     * Checks if a there is no floor above the tree.
     *
     * @param dimensions Tree dimensions.
     * @param location Tree location.
     *
     * @return Whether there is a floor above the tree.
     */

    private boolean checkHeight(final BlockVector3 dimensions, final Location location) {
        final World world = location.getWorld();
        try (final EditSession es = WorldEdit.getInstance()
            .newEditSession(BukkitAdapter.adapt(world))) {
            final int blockX = location.getBlockX();
            final int blockY = location.getBlockY();
            final int blockZ = location.getBlockZ();
            for (int x = blockX + dimensions.getBlockX(); x != blockX; x--) {
                for (int y = blockY + dimensions.getBlockY(); y != blockY; y--) {
                    for (int z = blockZ + dimensions.getBlockZ(); z != blockZ; z--) {
                        if (!es.getBlock(BlockVector3.at(x, y, z)).isAir()) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}