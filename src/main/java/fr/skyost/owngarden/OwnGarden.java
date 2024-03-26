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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.zeroturnaround.zip.ZipUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    public final MiniMessage mini = MiniMessage.builder()
        .tags(TagResolver.builder().resolver(TagResolver.standard())
            .build()).build();

    public final Random rnd = new SecureRandom();

    public final Map<Material, List<File>> treeMap = new EnumMap<>(Material.class);

    public final ComponentLogger logger = ComponentLogger.logger(getName());

    @Override
    public void onEnable() {
        /* WORLDEDIT HOOK : */
        if (Utils.checkWorldEditVersion(this)) {
            operations = new WorldEditUtils(this);
        } else operations = new DefaultUtils();

        /* CONFIGURATION : */
        logger.info(Component.text("Loading the configuration...", NamedTextColor.GOLD));


        this.pluginConfig = new PluginConfig(this);
        /*try {
            pluginConfig.load();
            *//*if (pluginConfig.enableUpdater) {
                new Skyupdater(this, 103296, getFile(), true, true);
            }*//* // off for now, no updates D:
            *//*if (pluginConfig.enableMetrics) {
                new MetricsLite(this);
            }*//*
        } catch (InvalidConfigurationException e) {
            throw new IllegalArgumentException(e);
        }*/

        logger.info(Component.text("Configuration loaded !", NamedTextColor.GOLD));
        logger.info(Component.text("Testing schematics...", NamedTextColor.GOLD));
        loadSchematics();

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
        ZipUtil.unpack(getFile(), schematicsDirectory, name ->
            name.startsWith("schematics/") && name.length() > "schematics/".length()
            ? name.substring("schematics/".length()) : null);
    }

    /**
     * Returns the schematics list which corresponds to the specified material (sapling / log).
     *
     * @param material The material.
     *
     * @return The corresponding list.
     */

    public @Nullable File getSchematic(final Material material) {
        final List<File> files = treeMap.get(material);
        return files == null || files.isEmpty() ? null
            : files.get(rnd.nextInt(files.size()));
    }

    /**
     * Looks and loads all schematics. Creates folders that dont exist.
     */

    public void loadSchematics() {
        final File root = new File(getDataFolder()
            + File.separator + pluginConfig.schematicsDirectory);
        if (!root.isDirectory()) {
            root.mkdirs();
            for (final PluginConfig.DefaultTreeType type : PluginConfig.DefaultTreeType.values()) {
                new File(root.getAbsolutePath() + File.separator + type.name().toLowerCase()).mkdir();
            }

            /* EXTRACTING DEFAULT SCHEMATICS IF NEEDED : */
            logger.info(Component.text("Extracting default schematics...", NamedTextColor.GOLD));
            extractSamples(root);
            logger.info(Component.text("Done !", NamedTextColor.GOLD));
            pluginConfig.reloadConfig();
        }

        /* TESTING SCHEMATICS : */
        final File[] invalidSchematics = operations.testSchematics();
        if (invalidSchematics.length != 0) {
            logger.info(Component.text("There are some invalid schematics.", NamedTextColor.RED));
            for (final File invalidSchematic : invalidSchematics) {
                for (final List<File> treeList : treeMap.values()) {
                    treeList.remove(invalidSchematic);
                }
            }
            logger.info(Component.text("They will not be used. Please fix them and restart your server.", NamedTextColor.RED));
        } else {
            logger.info(Component.text("Done, no error.", NamedTextColor.GOLD));
        }
    }
}