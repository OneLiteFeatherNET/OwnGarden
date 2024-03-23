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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static final List<File> saplingOakSchematics = new ArrayList<>();
    public static final List<File> saplingSpruceSchematics = new ArrayList<>();
    public static final List<File> saplingBirchSchematics = new ArrayList<>();
    public static final List<File> saplingJungleSchematics = new ArrayList<>();
    public static final List<File> saplingAcaciaSchematics = new ArrayList<>();
    public static final List<File> saplingDarkOakSchematics = new ArrayList<>();
    public static final List<File> mushroomRedSchematics = new ArrayList<>();
    public static final List<File> mushroomBrownSchematics = new ArrayList<>();

    @Override
    public void onEnable() {
        /* WORLDEDIT HOOK : */
        if (Utils.checkWorldEditVersion(this)) {
            operations = new WorldEditUtils(this);
        } else operations = new DefaultUtils();

        /* CONFIGURATION : */
        log(NamedTextColor.GOLD, "Loading the configuration...");

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
        log(NamedTextColor.GOLD, "Configuration loaded !");

        /* EXTRACTING DEFAULT SCHEMATICS IF NEEDED : */
        /*final File shematicsDirectory = new File(pluginConfig.schematicsDirectory);
        if (!shematicsDirectory.exists() || !shematicsDirectory.isDirectory()) {
            shematicsDirectory.mkdirs();
        }
        if (shematicsDirectory.list().length != 0) {
            log(NamedTextColor.GOLD, "Extracting samples schematics...");
            extractSamples(shematicsDirectory);
            log(NamedTextColor.GOLD, "Done !");
        }*/

        /* TESTING SCHEMATICS : */
        log(NamedTextColor.GOLD, "Testing schematics...");
        loadSchematics();
        final File[] invalidSchematics = operations.testSchematics();
        if (invalidSchematics.length != 0) {
            log(NamedTextColor.RED, "There are some invalid schematics :");
            for (final File invalidSchematic : invalidSchematics) {
                log(NamedTextColor.RED, invalidSchematic.getName());
                saplingOakSchematics.remove(invalidSchematic);
                saplingSpruceSchematics.remove(invalidSchematic);
                saplingBirchSchematics.remove(invalidSchematic);
                saplingJungleSchematics.remove(invalidSchematic);
                saplingAcaciaSchematics.remove(invalidSchematic);
                saplingDarkOakSchematics.remove(invalidSchematic);
                mushroomBrownSchematics.remove(invalidSchematic);
                mushroomRedSchematics.remove(invalidSchematic);
            }
            log(NamedTextColor.RED, "They are not going to be used by the plugin. Please fix them and restart your server.");
        } else {
            log(NamedTextColor.GOLD, "Done, no error.");
        }

        /* REGISTERING EVENTS : */
        Bukkit.getPluginManager().registerEvents(new GlobalEventsListener(this), this);

        /* REGISTERING COMMANDS : */
        getCommand("owngarden").setExecutor(new OwnGardenCommand(this));
        log(null, "Enabled " + getName() + " v" + getPluginMeta().getVersion()
            + " by " + Joiner.on(' ').join(getPluginMeta().getAuthors()) + "!");
    }

    /*
     * Extracts the samples to the specified directory.
     *
     * @param schematicsDirectory The schematics directory.
     */
    /*private void extractSamples(final File schematicsDirectory) {
        ZipUtil.unpack(getFile(), schematicsDirectory, name -> {
            return name.startsWith("schematics/") && name.length() > "schematics/".length()
                ? name.substring("schematics/".length()) : null;
        });
    }*/

    /**
     * Logs a message to the console.
     *
     * @param color The color (after the [plugin-name]).
     * @param message The message.
     */
    public void log(final NamedTextColor color, final String message) {
        Bukkit.getConsoleSender().sendMessage(Component.text("[" + getName() + "]").append(Component.text(message, color)));
    }

    /**
     * Returns the schematics list which corresponds to the specified material (sapling / log).
     *
     * @param material The material.
     *
     * @return The corresponding list.
     */

    public static List<File> getSchematics(final Material material) {
        return switch (material) { // Oak
            case OAK_SAPLING, OAK_LOG -> saplingOakSchematics; // Spruce
            case SPRUCE_SAPLING, SPRUCE_LOG -> saplingSpruceSchematics; // Birch
            case BIRCH_SAPLING, BIRCH_LOG -> saplingBirchSchematics; // Jungle
            case JUNGLE_SAPLING, JUNGLE_LOG -> saplingJungleSchematics; // Acacia
            case ACACIA_SAPLING, ACACIA_LOG -> saplingAcaciaSchematics; // Dark Oak
            case DARK_OAK_SAPLING, DARK_OAK_LOG -> saplingDarkOakSchematics; // Red mushroom
            case RED_MUSHROOM, MUSHROOM_STEM -> mushroomRedSchematics; // Red mushroom
            case BROWN_MUSHROOM -> mushroomBrownSchematics; // Brown mushroom
            default -> List.of();
        };
    }


    public void loadSchematics() {
        final File root = new File(getDataFolder().getAbsolutePath() + "/schematics");
        if (!root.isDirectory()) {
            root.mkdirs();
        }

        File dir = new File(root.getAbsolutePath() + File.separator + pluginConfig.saplingOakDir);
        if (!dir.isDirectory()) dir.mkdirs();
        saplingOakSchematics.addAll(Arrays.asList(dir.listFiles()));

        dir = new File(root.getAbsolutePath() + File.separator + pluginConfig.saplingAcaciaDir);
        if (!dir.isDirectory()) dir.mkdirs();
        saplingAcaciaSchematics.addAll(Arrays.asList(dir.listFiles()));

        dir = new File(root.getAbsolutePath() + File.separator + pluginConfig.saplingBirchDir);
        if (!dir.isDirectory()) dir.mkdirs();
        saplingBirchSchematics.addAll(Arrays.asList(dir.listFiles()));

        dir = new File(root.getAbsolutePath() + File.separator + pluginConfig.saplingDarkOakDir);
        if (!dir.isDirectory()) dir.mkdirs();
        saplingDarkOakSchematics.addAll(Arrays.asList(dir.listFiles()));

        dir = new File(root.getAbsolutePath() + File.separator + pluginConfig.saplingJungleDir);
        if (!dir.isDirectory()) dir.mkdirs();
        saplingJungleSchematics.addAll(Arrays.asList(dir.listFiles()));

        dir = new File(root.getAbsolutePath() + File.separator + pluginConfig.saplingSpruceDir);
        if (!dir.isDirectory()) dir.mkdirs();
        saplingSpruceSchematics.addAll(Arrays.asList(dir.listFiles()));

        dir = new File(root.getAbsolutePath() + File.separator + pluginConfig.mushroomBrownDir);
        if (!dir.isDirectory()) dir.mkdirs();
        mushroomBrownSchematics.addAll(Arrays.asList(dir.listFiles()));

        dir = new File(root.getAbsolutePath() + File.separator + pluginConfig.mushroomRedDir);
        if (!dir.isDirectory()) dir.mkdirs();
        mushroomRedSchematics.addAll(Arrays.asList(dir.listFiles()));
    }
}