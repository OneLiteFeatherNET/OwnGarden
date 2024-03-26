package fr.skyost.owngarden.config;

import fr.skyost.owngarden.OwnGarden;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The plugin configuration.
 */

public class PluginConfig {

	private static final String troot = "trees";
	private static final String saplings = "saplings";
	private static final String schematics = "schematics";

	private final OwnGarden plugin;
	//	public boolean enableUpdater = true;
//	public boolean enableMetrics = true;
//	public boolean removeMetaData = true;
	public boolean checkHeight = false;
	public boolean randomRotation = true;

	public String schematicsDirectory = "schematics";

	/**
	 * Creates a new plugin config instance.
	 *
	 * @param plugin The plugin.
	 */

	public PluginConfig(final OwnGarden plugin) {
		this.plugin = plugin;
		reloadConfig();
	}

	/**
	 * Refills the EnumMap of schematics.
	 */

	public void reloadConfig() {
		plugin.treeMap.clear();
		plugin.reloadConfig();
		final FileConfiguration config = plugin.getConfig();
		addDefault(config, "checkHeight", checkHeight);
		checkHeight = config.getBoolean("checkHeight");
		addDefault(config, "randomRotation", randomRotation);
		randomRotation = config.getBoolean("randomRotation");
//		addDefault(config, "removeMetaData", removeMetaData);
//		removeMetaData = config.getBoolean("removeMetaData");

		final ConfigurationSection trees;
		if (config.getConfigurationSection(troot) == null) {
			trees = config.createSection(troot);
			for (final DefaultTreeType type : DefaultTreeType.values()) {
				final ConfigurationSection typeSec = trees.createSection(type.name().toLowerCase());
				final ArrayList<String> saps = new ArrayList<>();
				for (final Material sapling : type.saplings) {
					saps.add(sapling.name());
				}
				typeSec.set(saplings, saps);
				typeSec.set(schematics, Arrays.asList("1.schematic"));
			}
		} else trees = config.getConfigurationSection(troot);

		for (final String type : trees.getKeys(false)) {
			final ConfigurationSection typeSec = trees.getConfigurationSection(type);
			final List<File> schems = typeSec.getStringList(schematics).stream().map(schem ->
				new File(plugin.getDataFolder() + File.separator + schematicsDirectory
					+ File.separator + type + File.separator + schem)).collect(Collectors.toList());

			for (final String sapling : typeSec.getStringList(saplings)) {
				final Material sapl = Material.getMaterial(sapling);
				if (sapl == null) continue;
				plugin.treeMap.put(sapl, schems);
			}
		}
		plugin.saveConfig();
	}

	private void addDefault(final ConfigurationSection config, final String path, final Object value) {
		if (config.get(path) == null) config.set(path, value);
	}

	public List<File> getForType(final DefaultTreeType type) {
		if (type.saplings.length == 0) return List.of();
		return plugin.treeMap.get(type.saplings[0]);
	}

	public enum DefaultTreeType {
		OAK(Material.OAK_SAPLING),
		ACACIA(Material.ACACIA_SAPLING),
		BIRCH(Material.BIRCH_SAPLING),
		SPRUCE(Material.SPRUCE_SAPLING),
		JUNGLE(Material.JUNGLE_SAPLING),
		DARK_OAK(Material.DARK_OAK_SAPLING),
		CHERRY(Material.CHERRY_SAPLING),
		AZALEA(Material.AZALEA, Material.FLOWERING_AZALEA),
		RED_SHROOM(Material.RED_MUSHROOM),
		BROWN_SHROOM(Material.BROWN_MUSHROOM),
		CRIMSON_FUNGUS(Material.CRIMSON_FUNGUS),
		WARPED_FUNGUS(Material.WARPED_FUNGUS),
		;

		private final Material[] saplings;

        DefaultTreeType(final Material... saplings) {
            this.saplings = saplings;
        }
    }
}