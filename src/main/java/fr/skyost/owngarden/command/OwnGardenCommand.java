package fr.skyost.owngarden.command;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.OwnGarden;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.util.ChatPaginator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The /owngarden command.
 */
public record OwnGardenCommand(OwnGarden plugin) implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!sender.hasPermission("owngarden.command")) {
            plugin.logger.info(Component.text("You do not have the permission to execute this command.", NamedTextColor.RED));
            return false;
        }
        plugin.loadConfigs();
        plugin.loadSchematics();
        final PluginMeta description = plugin.getPluginMeta();
        sender.sendMessage(OwnGarden.mini.deserialize("<green>" + description.getName() + " v" + description.getVersion()
            + " <reset>by <gold>" + Joiner.on(' ').join(description.getAuthors())));
        final String line = "=".repeat(ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - 2);
        sender.sendMessage(OwnGarden.mini.deserialize("<gold>SCHEMATICS : "));
        for (final Map.Entry<Material, List<Path>> en : plugin.treeMap.entrySet()) {
            sender.sendMessage(OwnGarden.mini.deserialize("<bold><yellow>- " + en.getKey().name() + " : <reset>"
                + Joiner.on('\n').join(en.getValue().stream().map(Path::toString).toList())));
        }
        sender.sendMessage(OwnGarden.mini.deserialize("<reset>" + line));
        sender.sendMessage(OwnGarden.mini.deserialize("<gold>PERMISSIONS : "));
        for (final Permission permission : description.getPermissions()) {
            sender.sendMessage(OwnGarden.mini.deserialize(sender.hasPermission(permission)
                ? "<green>- You have the permission <bold>" + permission.getName() + "<green>."
                : "<red>- You do not have the permission <bold>" + permission.getName() + "<red>."));
        }
        sender.sendMessage(OwnGarden.mini.deserialize("<reset>" + line));
        sender.sendMessage(OwnGarden.mini.deserialize("<dark_aqua><italic>The above list is scrollable."));
        return true;
    }
}