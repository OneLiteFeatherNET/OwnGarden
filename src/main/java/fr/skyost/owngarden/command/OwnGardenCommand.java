package fr.skyost.owngarden.command;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.OwnGarden;
import fr.skyost.owngarden.config.PluginConfig;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.util.ChatPaginator;

/**
 * The /owngarden command.
 */
public record OwnGardenCommand(OwnGarden plugin) implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!sender.hasPermission("owngarden.command")) {
            plugin.logger.info(Component.text("You do not have the permission to execute this command.", NamedTextColor.RED));
            return false;
        }
        plugin.loadSchematics();
        plugin.pluginConfig.reloadConfig();
        final PluginMeta description = plugin.getPluginMeta();
        sender.sendMessage("§a" + description.getName() + " v" + description.getVersion()
            + " §7by §6" + Joiner.on(' ').join(description.getAuthors()));
        final String line = "=".repeat(ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - 2);
        sender.sendMessage("§r" + line);
        sender.sendMessage("§6SCHEMATICS : ");
        sender.sendMessage("§l- Oak : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.OAK)));
        sender.sendMessage("§l- Spruce : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.SPRUCE)));
        sender.sendMessage("§l- Birch : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.BIRCH)));
        sender.sendMessage("§l- Jungle : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.JUNGLE)));
        sender.sendMessage("§l- Acacia : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.ACACIA)));
        sender.sendMessage("§l- Dark Oak : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.DARK_OAK)));
        sender.sendMessage("§l- Cherry : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.CHERRY)));
        sender.sendMessage("§l- Azalea : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.AZALEA)));
        sender.sendMessage("§l- Brown Mushroom : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.BROWN_SHROOM)));
        sender.sendMessage("§l- Red Mushroom : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.RED_SHROOM)));
        sender.sendMessage("§l- Crimson Mushroom : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.CRIMSON_FUNGUS)));
        sender.sendMessage("§l- Warped Mushroom : §r" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.WARPED_FUNGUS)));
        sender.sendMessage("§r" + line);
        sender.sendMessage("§6PERMISSIONS : ");
        for (final Permission permission : description.getPermissions()) {
            sender.sendMessage(sender.hasPermission(permission) ? "§a- You have the permission §l" + permission.getName() + "§r§a."
                : "§c- You do not have the permission §l" + permission.getName() + "§r§c."
            );
        }
        sender.sendMessage("§r" + line);
        sender.sendMessage("§3§oThe above list is scrollable.");
        return true;
    }
}