package fr.skyost.owngarden.worldedit;

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
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

import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Represents available WorldEdit operations.
 */
public record WorldEditOperations(OwnGarden plugin) {

    private static final Random rnd = new SecureRandom();
    /**
     * Accepted WorldEdit versions.
     */
    public static final String[] WORLDEDIT_VERSIONS = {"7.2", "7.3"};
    /**
     * Returns whether the current WorldEdit version should be accepted.
     *
     * @return Whether the current WorldEdit version should be accepted.
     */
    public boolean checkWorldEditVersion() {
        final String version = Bukkit.getPluginManager().getPlugin("WorldEdit").getPluginMeta().getVersion();
        for (final String prefix : WORLDEDIT_VERSIONS) {
            if (version.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests if each schematic is valid.
     *
     * @return An array containing the invalid schematics.
     */
    public String[] testSchematics() {
        final PluginConfig config = plugin.pluginConfig;
        final ArrayList<String> schematics = new ArrayList<>();
        schematics.addAll(config.saplingOakSchematics);
        schematics.addAll(config.saplingSpruceSchematics);
        schematics.addAll(config.saplingBirchSchematics);
        schematics.addAll(config.saplingJungleSchematics);
        schematics.addAll(config.saplingAcaciaSchematics);
        schematics.addAll(config.saplingDarkOakSchematics);
        final boolean removeWorldEditMetaData = plugin.pluginConfig.schematicsRemoveWorldEditMetaData;
        final ArrayList<String> invalidSchematics = new ArrayList<>();
        for (final String schematic : schematics) {
            try {
                loadSchematic(schematic);
                if (!removeWorldEditMetaData) {
                    continue;
                }
                final File file = getFile(schematic);
                final ClipboardFormat format = ClipboardFormats.findByFile(file);
                if (format == null) continue;
                removeWorldEditMetaData(format, file);
            } catch (IOException ex) {
                ex.printStackTrace();
                invalidSchematics.add(schematic);
            }
        }
        return invalidSchematics.toArray(new String[0]);
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
     * @param schematic The schematic file (must be in the schematics directory).
     *
     * @return The WorldEdit clipboard holder.
     *
     * @throws IOException If any I/O exception occurs.
     */

    public ClipboardHolder loadSchematic(final String schematic) throws IOException {
        final File file = getFile(schematic);
        if (!file.exists()) {
            throw new FileNotFoundException("Schematic not found : $schematic.");
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
    public boolean growTree(final List<String> schematics, final Location location) {
        if (schematics == null || schematics.isEmpty()) {
            return false;
        }
        final String file = schematics.get(rnd.nextInt(schematics.size()));
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
            final BlockVector3 dimensions = holder.getClipboard().getDimensions();
            if (plugin.pluginConfig.schematicsCheckHeight && !checkHeight(dimensions, location)) {
                return false;
            }
            holder.getClipboard().setOrigin(BlockVector3.at(dimensions.x() >> 1, 0, dimensions.z() >> 1));
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
            plugin.log(NamedTextColor.RED, "Unable to load the schematic : " + file + ".");
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
        final int blockX = location.getBlockX();
        final int blockY = location.getBlockY();
        final int blockZ = location.getBlockZ();
        for (int x = blockX + dimensions.x(); x != blockX; x--) {
            for (int y = blockY + dimensions.y(); y != blockY; y--) {
                for (int z = blockZ + dimensions.z(); z != blockZ; z--) {
                    if (!world.getBlockAt(x, y, z).getType().isAir()) {
                        return false;
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
     * @throws IOException If any I/O exception occurs.
     */
    @SuppressWarnings("deprecation")
    private void removeWorldEditMetaData(final ClipboardFormat format, final File file) throws IOException {
        final NBTInputStream input = new NBTInputStream(new GZIPInputStream(new FileInputStream(file)));
        CompoundTag root = (CompoundTag) input.readNamedTag().getTag();
        CompoundTag target = root;
        final boolean isSponge = Objects.equals(format.getPrimaryFileExtension(),
            BuiltInClipboardFormat.SPONGE_SCHEMATIC.getPrimaryFileExtension());
        if (isSponge) {
            target = (CompoundTag) target.getValue().getOrDefault("Metadata", new CompoundTag(new HashMap<>()));
        }
        final Map<String, Tag<?, ?>> value = target.getValue();
        value.remove("WEOriginX");
        value.remove("WEOriginY");
        value.remove("WEOriginZ");
        value.remove("WEOffsetX");
        value.remove("WEOffsetY");
        value.remove("WEOffsetZ");
        target = target.setValue(value);
        if (isSponge) {
            final Map<String, Tag<?, ?>> rootValue = root.getValue();
            rootValue.put("Metadata", target);
            rootValue.put("Offset", new IntArrayTag(new int[] {0, 0, 0}));
            root = root.setValue(rootValue);
        } else {
            root = target;
        }
        final NBTOutputStream output = new NBTOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
        output.writeNamedTag("Schematic", root);
        output.close();
    }
}