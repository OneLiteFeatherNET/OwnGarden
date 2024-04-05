package fr.skyost.owngarden;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.command.OwnGardenCommand;
import fr.skyost.owngarden.data.MaterialSchems;
import fr.skyost.owngarden.listener.GlobalEventsListener;
import fr.skyost.owngarden.util.TreeService;
import fr.skyost.owngarden.worldedit.DefaultTreeAPI;
import fr.skyost.owngarden.worldedit.TreeAPI;
import fr.skyost.owngarden.worldedit.WorldEditTreeAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The OwnGarden plugin class.
 */

public class OwnGarden extends JavaPlugin {

    /*
     * The plugin config.
     */
//    public PluginConfig pluginConfig = null;

    /**
     * The WorldEdit operations.
     */
    private TreeAPI operations = null;
    private TreeService treeService = null;

    private static final ComponentLogger logger = ComponentLogger.logger(OwnGarden.class.getSimpleName());

    @Override
    public void onLoad() {
        super.onLoad();
        ConfigurationSerialization.registerClass(MaterialSchems.class);
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        /* WORLDEDIT HOOK : */
        if (TreeAPI.checkWorldEditVersion(this)) {
            operations = new WorldEditTreeAPI(this);
        } else {
            operations = new DefaultTreeAPI();
        }

        treeService = new TreeService(this);

        /* CONFIGURATION : */
        logger.info(Component.text("Loading the configuration...", NamedTextColor.GOLD));

        treeService.loadConfigs();

        logger.info(Component.text("Configuration loaded !", NamedTextColor.GOLD));
        logger.info(Component.text("Testing schematics...", NamedTextColor.GOLD));

        treeService.loadSchematics();

        /* REGISTERING EVENTS : */
        Bukkit.getPluginManager().registerEvents(new GlobalEventsListener(this), this);

        /* REGISTERING COMMANDS : */
        getCommand("owngarden").setExecutor(new OwnGardenCommand(this));
        logger.info(Component.text("Enabled " + getName() + " v" + getPluginMeta().getVersion()
            + " by " + Joiner.on(' ').join(getPluginMeta().getAuthors()) + "!"));
    }

    public TreeAPI getOperations() {
        return operations;
    }

    public TreeService getTreeService() {
        return treeService;
    }
}