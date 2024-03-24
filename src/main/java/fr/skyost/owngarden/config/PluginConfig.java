package fr.skyost.owngarden.config;

import fr.skyost.owngarden.OwnGarden;
import fr.skyost.owngarden.util.Skyoconfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * The plugin configuration.
 */

public class PluginConfig extends Skyoconfig {

	@ConfigOptions(name = "enable.updater")
	public boolean enableUpdater = true;

	@ConfigOptions(name = "enable.metrics")
	public boolean enableMetrics = true;

	@ConfigOptions(name = "schematics.directory")
	public String schematicsDirectory;

	@ConfigOptions(name = "schematics.random-rotation")
	public boolean schematicsRandomRotation = true;

	@ConfigOptions(name = "schematics.check-height")
	public boolean schematicsCheckHeight = false;

	@ConfigOptions(name = "schematics.remove-worldedit-metadata")
	public boolean schematicsRemoveWorldEditMetaData = true;

//	@ConfigOptions(name = "sapling.oak")
	public String saplingOakDir = "oak";
	public String saplingBirchDir = "birch";
	public String saplingJungleDir = "jungle";
	public String saplingAcaciaDir = "acacia";
	public String saplingSpruceDir = "spruce";
	public String saplingDarkOakDir = "dark_oak";
	public String saplingCherryDir = "cherry";
	public String saplingAzaleaDir = "azalea";
	public String mushroomBrownDir = "brown_mushroom";
	public String mushroomRedDir = "red_mushroom";
	public String mushroomCrimsonDir = "crimson_mushroom";
	public String mushroomWarpedDir = "warped_mushroom";

	/**
	 * Creates a new plugin config instance.
	 *
	 * @param dataFolder The plugin data folder.
	 */

	public PluginConfig(final File dataFolder) {
		super(new File(dataFolder, "config.yml"), Collections.singletonList("OwnGarden Configuration File"));

		schematicsDirectory = new File(dataFolder, "schematics/").getPath();

		OwnGarden.treeTypes.put(saplingOakDir, new ArrayList<>());
		OwnGarden.treeTypes.put(saplingBirchDir, new ArrayList<>());
		OwnGarden.treeTypes.put(saplingJungleDir, new ArrayList<>());
		OwnGarden.treeTypes.put(saplingAcaciaDir, new ArrayList<>());
		OwnGarden.treeTypes.put(saplingSpruceDir, new ArrayList<>());
		OwnGarden.treeTypes.put(saplingDarkOakDir, new ArrayList<>());
		OwnGarden.treeTypes.put(saplingCherryDir, new ArrayList<>());
		OwnGarden.treeTypes.put(saplingAzaleaDir, new ArrayList<>());
		OwnGarden.treeTypes.put(mushroomBrownDir, new ArrayList<>());
		OwnGarden.treeTypes.put(mushroomRedDir, new ArrayList<>());
		OwnGarden.treeTypes.put(mushroomCrimsonDir, new ArrayList<>());
		OwnGarden.treeTypes.put(mushroomWarpedDir, new ArrayList<>());
	}
}