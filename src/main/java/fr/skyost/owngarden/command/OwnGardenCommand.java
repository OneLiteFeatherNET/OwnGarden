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
        sender.sendMessage(plugin.mini.deserialize("<gold>SCHEMATICS : "));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Oak : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.OAK))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Spruce : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.SPRUCE))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Birch : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.BIRCH))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Jungle : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.JUNGLE))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Acacia : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.ACACIA))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Dark Oak : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.DARK_OAK))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Cherry : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.CHERRY))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Azalea : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.AZALEA))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Brown Mushroom : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.BROWN_SHROOM))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Red Mushroom : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.RED_SHROOM))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Crimson Mushroom : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.CRIMSON_FUNGUS))));
        sender.sendMessage(plugin.mini.deserialize("<bold><yellow>- Warped Mushroom : <reset>" + Joiner.on(' ').join(plugin.pluginConfig.getForType(PluginConfig.DefaultTreeType.WARPED_FUNGUS))));
        sender.sendMessage(plugin.mini.deserialize("<reset>" + line));
        sender.sendMessage(plugin.mini.deserialize("<gold>PERMISSIONS : "));
        for (final Permission permission : description.getPermissions()) {
            sender.sendMessage(plugin.mini.deserialize(sender.hasPermission(permission) 
                ? "<green>- You have the permission <bold>" + permission.getName() + "<green>."
                : "<red>- You do not have the permission <bold>" + permission.getName() + "<red>."));
        }
        sender.sendMessage(plugin.mini.deserialize("<reset>" + line));
        sender.sendMessage(plugin.mini.deserialize("<dark_aqua><italic>The above list is scrollable."));
        return true;
    }
}