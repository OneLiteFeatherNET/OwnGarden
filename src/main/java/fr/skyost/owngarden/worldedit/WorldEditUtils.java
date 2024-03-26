package fr.skyost.owngarden.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import fr.skyost.owngarden.OwnGarden;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents available WorldEdit operations.
 */
public record WorldEditUtils(OwnGarden plugin) implements Utils {

    /**
     * Tests if each schematic is valid.
     *
     * @return An array containing the invalid schematics.
     */
    public File[] testSchematics() {
        final ArrayList<File> schematics = new ArrayList<>();
        for (final List<File> list : plugin.treeMap.values()) {
            schematics.addAll(list);
        }
//        final boolean removeMetaData = plugin.pluginConfig.removeMetaData;
        final ArrayList<File> invalidSchematics = new ArrayList<>();
        for (final File schematic : schematics) {
            try {
                loadSchematic(schematic);
                Bukkit.getConsoleSender().sendMessage("loaded: " + schematic.getCanonicalPath());
                /*if (!removeMetaData) {
                    continue;
                }
                final ClipboardFormat format = ClipboardFormats.findByFile(schematic);
                if (format == null) continue;
                removeWorldEditMetaData(format, schematic);*/
            } catch (IOException ex) {
                plugin.logger.info(Component.text(ex.getMessage(), NamedTextColor.RED));
                invalidSchematics.add(schematic);
            }
        }
        return invalidSchematics.toArray(new File[0]);
    }

    /**
     * Returns the file associated with the given schematic.
     *
     * @param schematic The schematic.
     *
     * @return The file.
     */
    private File getFile(final String schematic) {
        return new File(plugin.pluginConfig.schematicsDirectory, schematic);
    }

    /**
     * Loads a schematic.
     *
     * @param file The schematic file (must be in the schematics directory).
     *
     * @return The WorldEdit clipboard holder.
     *
     * @throws IOException If any I/O exception occurs.
     */

    private ClipboardHolder loadSchematic(final File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Schematic not found : " + file.getPath());
        }
        final ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) throw new IOException("Unknown schematic format.");
        final FileInputStream fileInputStream = new FileInputStream(file);
        final BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        final ClipboardReader reader = format.getReader(bufferedInputStream);
        return new ClipboardHolder(reader.read());
    }

    /**
     * Grows a tree at the specified location.
     *
     * @param schematic The schematic file.
     * @param location The location.
     *
     * @return Whether the operation has been a success.
     */

    public boolean growTree(final File schematic, final Location location) {
        try {
            location.getBlock().setType(Material.AIR, false);
            final ClipboardHolder holder = loadSchematic(schematic);
            if (plugin.pluginConfig.randomRotation) {
                final int degrees = plugin.rnd.nextInt(4) * 90;
                if (degrees != 0) {
                    final AffineTransform transform = new AffineTransform();
                    transform.rotateY(degrees);
                    holder.setTransform(transform);
                }
            }
            final Clipboard clip = holder.getClipboards().get(0);
            final BlockVector3 dimensions = clip.getDimensions();
            if (plugin.pluginConfig.checkHeight && !checkHeight(dimensions, location)) {
                return false;
            }
            clip.setOrigin(BlockVector3.at(dimensions.getBlockX() >> 1, 0, dimensions.getBlockZ() >> 1));
            final EditSession session = WorldEdit.getInstance().newEditSession(new BukkitWorld(location.getWorld()));
            final Operation operation = holder
                .createPaste(session)
                .to(BlockVector3.at(location.getBlockX(),
                    location.getBlockY(), location.getBlockZ()))
                .ignoreAirBlocks(true)
                .build();
            Operations.completeLegacy(operation);
            session.close();

            return true;
        } catch (IOException | MaxChangedBlocksException ex) {
            plugin.logger.info(Component.text("Unable to load the schematic : " + schematic.getName(), NamedTextColor.RED));
            ex.printStackTrace();
        }
        return false;
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

    /**
     * Removes all WorldEdit metadata (if needed).
     *
     * @param format The clipboard format.
     * @param file The file.
     *
     */
    //write() breaks the GZIP file format, and this is not needed since origin gets reset later anyway
    private void removeWorldEditMetaData(final ClipboardFormat format, final File file) {
        /*final CompoundBinaryTag root = BinaryTagIO.reader().read(new FileInputStream(file), BinaryTagIO.Compression.GZIP);
        CompoundBinaryTag target = root;
        final boolean isSponge = Objects.equals(format.getPrimaryFileExtension(),
            BuiltInClipboardFormat.SPONGE_SCHEMATIC.getPrimaryFileExtension());
        if (isSponge) {
            target = target.getCompound("Metadata", CompoundBinaryTag.empty());
        }
        target.remove("WEOriginX");
        target.remove("WEOriginY");
        target.remove("WEOriginZ");
        target.remove("WEOffsetX");
        target.remove("WEOffsetY");
        target.remove("WEOffsetZ");
        if (isSponge) {
            root.put("Metadata", target);
            root.put("Offset", IntArrayBinaryTag.intArrayBinaryTag(new int[] {0, 0, 0}));
        }

        BinaryTagIO.writer().write(root, new FileOutputStream(file), BinaryTagIO.Compression.GZIP);*/
    }
}