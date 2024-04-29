package fr.skyost.owngarden.util;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.OwnGarden;
import fr.skyost.owngarden.data.MaterialSchems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Consumer;

public final class TreeService {

    private static final ComponentLogger logger = ComponentLogger.logger(TreeService.class.getSimpleName());

    private final Map<Material, List<Path>> treeMap = new HashMap<>();

    private static final Random rnd = new SecureRandom();

    private final OwnGarden plugin;

    private boolean checkHeight;
    private boolean randomRotation;
    private String schematicsDirectory = Constants.TREES;

    public TreeService(final OwnGarden plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the schematics list which corresponds to the specified material (sapling / log).
     *
     * @param mat The sapling material.
     * @param match Consumer of the schmatic path.
     */

    public void ifMatMatch(final Material mat, final Consumer<? super Path> match) {
        final List<Path> files = treeMap.get(mat);
        if (files == null || files.isEmpty()) return;
        match.accept(files.get(rnd.nextInt(files.size())));
    }

    /**
     * Loads the main config. Fills the tree material map
     */

    public void loadConfigs() {
        plugin.reloadConfig();
        treeMap.clear();
        final FileConfiguration config = plugin.getConfig();
        checkHeight = config.getBoolean("checkHeight", false);
        randomRotation = config.getBoolean("randomRotation", true);
        schematicsDirectory = config.getString("schematicsDirectory", Constants.TREES);

        @SuppressWarnings("unchecked")
        final List<MaterialSchems> list = config.getObject(Constants.TREES, List.class, List.of());
        list.forEach(ms -> treeMap.put(ms.material(), ms.schematics()));
    }

    /**
     * Looks and loads all schematics. Creates folders that dont exist.
     */

    public void loadSchematics() {
        final Path root = getTreeFolder();
        if (Files.notExists(root)) {
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                logger.error(Component.text("Could not create dir...", NamedTextColor.RED), e);
            }

            /* EXTRACTING DEFAULT SCHEMATICS IF NEEDED : */
            logger.info(Component.text("Extracting default schematics...", NamedTextColor.GOLD));
            ZipUtils.extractZip(plugin.getResource("schematics.zip"), getTreeFolder());
            logger.info(Component.text("Done !", NamedTextColor.GOLD));
            loadConfigs();
        }

        /* TESTING SCHEMATICS : */
        if (plugin.getOperations().testSchematics()) {
            logger.info(Component.text("There are some invalid schematics.\nPlease fix them and restart your server.", NamedTextColor.RED));
        } else {
            logger.info(Component.text("Done, no error.", NamedTextColor.GOLD));
        }
    }

    /**
     * Returns the schematics list which corresponds to the specified material (sapling / log).
     *
     * @return Folder with tree schematics.
     */

    public Path getTreeFolder() {
        return Path.of(plugin.getDataFolder() + File.separator + schematicsDirectory);
    }

    public void listTrees(final CommandSender sender) {
        for (final Map.Entry<Material, List<Path>> en : treeMap.entrySet()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<bold><yellow>- " + en.getKey().name() + " : <reset>"
                    + Joiner.on('\n').join(en.getValue().stream().map(Path::toString).toList())));
        }
    }

    public String getSchematicsDirectory() {
        return schematicsDirectory;
    }

    public Map<Material, List<Path>> getTreeMap() {
        return Collections.unmodifiableMap(treeMap);
    }

    public boolean isCheckHeight() {
        return checkHeight;
    }

    public boolean isRandomRotation() {
        return randomRotation;
    }
}
