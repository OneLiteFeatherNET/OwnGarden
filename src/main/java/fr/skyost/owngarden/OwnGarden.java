package fr.skyost.owngarden;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.command.OwnGardenCommand;
import fr.skyost.owngarden.data.MaterialSchems;
import fr.skyost.owngarden.listener.GlobalEventsListener;
import fr.skyost.owngarden.util.ZipUtils;
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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * The OwnGarden plugin class.
 */

public class OwnGarden extends JavaPlugin {

    public static final String TREES = "trees";

    public static final MiniMessage mini = MiniMessage.builder()
        .tags(TagResolver.builder().resolver(TagResolver.standard())
            .build()).build();

    /*
     * The plugin config.
     */
//    public PluginConfig pluginConfig = null;
    public boolean checkHeight;
    public boolean randomRotation;

    public String schematicsDirectory = TREES;

    /**
     * The WorldEdit operations.
     */
    public Utils operations = null;

    public final Random rnd = new SecureRandom();

    public final Map<Material, List<Path>> treeMap = new EnumMap<>(Material.class);

    public final ComponentLogger logger = ComponentLogger.logger(getName());

    @Override
    public void onLoad() {
        super.onLoad();
        ConfigurationSerialization.registerClass(MaterialSchems.class);
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        /* WORLDEDIT HOOK : */
        if (Utils.checkWorldEditVersion(this)) {
            operations = new WorldEditUtils(this);
        } else operations = new DefaultUtils();

        /* CONFIGURATION : */
        logger.info(Component.text("Loading the configuration...", NamedTextColor.GOLD));

        loadConfigs();

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
     * Returns the schematics list which corresponds to the specified material (sapling / log).
     *
     * @return Folder with tree schematics.
     */

    public Path treeFolder() {
        return Path.of(getDataFolder() + File.separator + schematicsDirectory);
    }

    /**
     * Returns the schematics list which corresponds to the specified material (sapling / log).
     *
     * @param mat The sapling material.
     * @param match Consumer of the schmatic path.
     */

    public void ifMatMatch(final Material mat, final Consumer<Path> match) {
        final List<Path> files = treeMap.get(mat);
        if (files == null || files.isEmpty()) return;
        match.accept(files.get(rnd.nextInt(files.size())));
    }

    /**
     * Loads the main config. Fills the tree material map
     */

    public void loadConfigs() {
        reloadConfig();
        treeMap.clear();
        final FileConfiguration config = getConfig();
        checkHeight = config.getBoolean("checkHeight", false);
        randomRotation = config.getBoolean("randomRotation", true);
        schematicsDirectory = config.getString("schematicsDirectory", TREES);

        @SuppressWarnings("unchecked")
        final List<MaterialSchems> list = config.getObject(TREES, List.class, List.of());
        list.forEach(ms -> treeMap.put(ms.material(), ms.schematics()));
//        logger.info(Component.text(treeMap.toString()));
    }

    /**
     * Looks and loads all schematics. Creates folders that dont exist.
     */

    public void loadSchematics() {
        final Path root = treeFolder();
        if (Files.notExists(root)) {
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                logger.error(Component.text("Could not create dir...", NamedTextColor.RED), e);
            }

            /* EXTRACTING DEFAULT SCHEMATICS IF NEEDED : */
            logger.info(Component.text("Extracting default schematics...", NamedTextColor.GOLD));
            ZipUtils.extractZip(getResource("schematics.zip"), Path.of(getDataFolder() + File.separator + schematicsDirectory));
            logger.info(Component.text("Done !", NamedTextColor.GOLD));
            loadConfigs();
        }

        /* TESTING SCHEMATICS : */
        if (operations.testSchematics()) {
            logger.info(Component.text("There are some invalid schematics.\nPlease fix them and restart your server.", NamedTextColor.RED));
        } else {
            logger.info(Component.text("Done, no error.", NamedTextColor.GOLD));
        }
    }
}