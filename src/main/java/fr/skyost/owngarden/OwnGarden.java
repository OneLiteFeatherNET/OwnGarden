package fr.skyost.owngarden;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.command.OwnGardenCommand;
import fr.skyost.owngarden.config.PluginConfig;
import fr.skyost.owngarden.listener.GlobalEventsListener;
import fr.skyost.owngarden.worldedit.DefaultUtils;
import fr.skyost.owngarden.worldedit.Utils;
import fr.skyost.owngarden.worldedit.WorldEditUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The OwnGarden plugin class.
 */

public class OwnGarden extends JavaPlugin {
    /**
     * The plugin config.
     */
    public PluginConfig pluginConfig = null;

    /**
     * The WorldEdit operations.
     */
    public Utils operations = null;

    public static final HashMap<String, List<File>> treeTypes = new HashMap<>();

    public final ComponentLogger logger = ComponentLogger.logger(getName());

    @Override
    public void onEnable() {
        /* WORLDEDIT HOOK : */
        if (Utils.checkWorldEditVersion(this)) {
            operations = new WorldEditUtils(this);
        } else operations = new DefaultUtils();

        /* CONFIGURATION : */
        logger.info(Component.text("Loading the configuration...", NamedTextColor.GOLD));

        this.pluginConfig = new PluginConfig(getDataFolder());
        try {
            pluginConfig.load();
            /*if (pluginConfig.enableUpdater) {
                new Skyupdater(this, 103296, getFile(), true, true);
            }*/ // off for now, no updates D:
            /*if (pluginConfig.enableMetrics) {
                new MetricsLite(this);
            }*/
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        logger.info(Component.text("Configuration loaded !", NamedTextColor.GOLD));
        logger.info(Component.text("Testing schematics...", NamedTextColor.GOLD));
        loadSchematics();

        /* TESTING SCHEMATICS : */
        final File[] invalidSchematics = operations.testSchematics();
        if (invalidSchematics.length != 0) {
            logger.info(Component.text("There are some invalid schematics :", NamedTextColor.RED));
            for (final File invalidSchematic : invalidSchematics) {
                logger.info(Component.text(invalidSchematic.getName(), NamedTextColor.RED));
                for (final List<File> treeList : treeTypes.values()) {
                    treeList.remove(invalidSchematic);
                }
            }
            logger.info(Component.text("They will not be used. Please fix them and restart your server.", NamedTextColor.RED));
        } else {
            logger.info(Component.text("Done, no error.", NamedTextColor.GOLD));
        }

        /* REGISTERING EVENTS : */
        Bukkit.getPluginManager().registerEvents(new GlobalEventsListener(this), this);

        /* REGISTERING COMMANDS : */
        getCommand("owngarden").setExecutor(new OwnGardenCommand(this));
        logger.info(Component.text("Enabled " + getName() + " v" + getPluginMeta().getVersion()
            + " by " + Joiner.on(' ').join(getPluginMeta().getAuthors()) + "!"));
    }

    /**
     * Extracts the samples to the specified directory.
     *
     * @param schematicsDirectory The schematics directory.
     */
    private void extractSamples(final File schematicsDirectory) {
        ZipUtil.unpack(getFile(), schematicsDirectory, name -> {
            return name.startsWith("schematics/") && name.length() > "schematics/".length()
                ? name.substring("schematics/".length()) : null;
        });
    }

    /**
     * Returns the schematics list which corresponds to the specified material (sapling / log).
     *
     * @param material The material.
     *
     * @return The corresponding list.
     */

    public List<File> getSchematics(final Material material) {
        return switch (material) { // Oak
            case OAK_SAPLING, OAK_LOG -> treeTypes.get(pluginConfig.saplingOakDir); // Oak
            case SPRUCE_SAPLING, SPRUCE_LOG -> treeTypes.get(pluginConfig.saplingSpruceDir); // Spruce
            case BIRCH_SAPLING, BIRCH_LOG -> treeTypes.get(pluginConfig.saplingBirchDir); // Birch
            case JUNGLE_SAPLING, JUNGLE_LOG -> treeTypes.get(pluginConfig.saplingJungleDir); // Jungle
            case ACACIA_SAPLING, ACACIA_LOG -> treeTypes.get(pluginConfig.saplingAcaciaDir); // Acacia
            case DARK_OAK_SAPLING, DARK_OAK_LOG -> treeTypes.get(pluginConfig.saplingDarkOakDir); // Dark Oak
            case CHERRY_SAPLING, CHERRY_LOG -> treeTypes.get(pluginConfig.saplingCherryDir); // Cherry Blossom
            case AZALEA, FLOWERING_AZALEA -> treeTypes.get(pluginConfig.saplingAzaleaDir); // Azalea Wood
            case RED_MUSHROOM, MUSHROOM_STEM -> treeTypes.get(pluginConfig.mushroomRedDir); // Red mushroom
            case BROWN_MUSHROOM -> treeTypes.get(pluginConfig.mushroomBrownDir); // Brown mushroom
            case CRIMSON_FUNGUS, CRIMSON_STEM -> treeTypes.get(pluginConfig.mushroomCrimsonDir); // Crimson mushroom
            case WARPED_FUNGUS, WARPED_STEM -> treeTypes.get(pluginConfig.mushroomWarpedDir); // Warped mushroom
            default -> List.of();
        };
    }

    /**
     * Looks and loads all schematics. Creates folders that dont exist.
     */

    public void loadSchematics() {
        final File root = new File(pluginConfig.schematicsDirectory);
        if (!root.isDirectory()) {
            root.mkdirs();
        }

        /* EXTRACTING DEFAULT SCHEMATICS IF NEEDED : */
        if (root.list().length == 0) {
            logger.info(Component.text("Extracting samples schematics...", NamedTextColor.GOLD));
            extractSamples(root);
            logger.info(Component.text("Done !", NamedTextColor.GOLD));
        }

        for (final Map.Entry<String, List<File>> en : treeTypes.entrySet()) {
            final File dir = new File(root.getAbsolutePath() + File.separator + en.getKey());
            if (!dir.isDirectory()) dir.mkdirs();
            en.getValue().addAll(Arrays.asList(dir.listFiles()));
        }
    }
}