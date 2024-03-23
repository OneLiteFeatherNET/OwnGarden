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
import com.sk89q.worldedit.util.io.Closer;
import fr.skyost.owngarden.OwnGarden;
import fr.skyost.owngarden.config.PluginConfig;
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
        final PluginConfig config = plugin.pluginConfig;
        final ArrayList<File> schematics = new ArrayList<>();
        schematics.addAll(OwnGarden.saplingOakSchematics);
        schematics.addAll(OwnGarden.saplingSpruceSchematics);
        schematics.addAll(OwnGarden.saplingBirchSchematics);
        schematics.addAll(OwnGarden.saplingJungleSchematics);
        schematics.addAll(OwnGarden.saplingAcaciaSchematics);
        schematics.addAll(OwnGarden.saplingDarkOakSchematics);
        schematics.addAll(OwnGarden.mushroomBrownSchematics);
        schematics.addAll(OwnGarden.mushroomRedSchematics);
        final boolean removeWorldEditMetaData = plugin.pluginConfig.schematicsRemoveWorldEditMetaData;
        final ArrayList<File> invalidSchematics = new ArrayList<>();
        for (final File schematic : schematics) {
            try {
                loadSchematic(schematic);
                if (!removeWorldEditMetaData) {
                    continue;
                }
                final ClipboardFormat format = ClipboardFormats.findByFile(schematic);
                if (format == null) continue;
//                removeWorldEditMetaData(format, schematic);
                Bukkit.getConsoleSender().sendMessage("loaded: " + schematic.getCanonicalPath());
            } catch (IOException ex) {
                ex.printStackTrace();
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
            plugin.log(NamedTextColor.RED, "Schematic not found : " + file.getName());
        }
        final ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) throw new IllegalArgumentException("Unknown schematic format.");
        final Closer closer = Closer.create();
        try {
            final FileInputStream fileInputStream = closer.register(new FileInputStream(file));
            final BufferedInputStream bufferedInputStream = closer.register(new BufferedInputStream(fileInputStream));
            final ClipboardReader reader = closer.register(format.getReader(bufferedInputStream));
            return new ClipboardHolder(reader.read());
        } catch (IOException e) {
            closer.rethrow(e);
        } finally {
            closer.close();
        }
        return null;
    }

    /**
     * Grows a tree at the specified location.
     *
     * @param schematics The schematics list.
     * @param location The location.
     *
     * @return Whether the operation has been a success.
     */

    public boolean growTree(final List<File> schematics, final Location location) {
        if (schematics == null || schematics.isEmpty()) {
            return false;
        }
        final File file = schematics.get(rnd.nextInt(schematics.size()));
        try {
            location.getBlock().setType(Material.AIR, false);
            final ClipboardHolder holder = loadSchematic(file);
            if (plugin.pluginConfig.schematicsRandomRotation) {
                final int degrees = rnd.nextInt(4) * 90;
                if (degrees != 0) {
                    final AffineTransform transform = new AffineTransform();
                    transform.rotateY(degrees);
                    holder.setTransform(transform);
                }
            }
            final Clipboard clip = holder.getClipboards().get(0);
            final BlockVector3 dimensions = clip.getDimensions();
            if (plugin.pluginConfig.schematicsCheckHeight && !checkHeight(dimensions, location)) {
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
            plugin.log(NamedTextColor.RED, "Unable to load the schematic : " + file.getName() + ".");
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
        final EditSession es = WorldEdit.getInstance()
            .newEditSession(BukkitAdapter.adapt(world));
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
        es.close();
        return true;
    }

    /**
     * Removes all WorldEdit metadata (if needed).
     *
     * @param format The clipboard format.
     * @param file The file.
     *
     */
    //write() breaks the GZIP file format, and this is not needed since origin gets reset anyway later
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