package fr.skyost.owngarden.command;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.OwnGarden;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.util.ChatPaginator;

/**
 * The /owngarden command.
 */
public record OwnGardenCommand(OwnGarden plugin) implements CommandExecutor {

    private static final ComponentLogger logger = ComponentLogger.logger(OwnGardenCommand.class.getSimpleName());

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!sender.hasPermission("owngarden.command")) {
            logger.info(Component.text("You do not have the permission to execute this command.", NamedTextColor.RED));
            return false;
        }
        final PluginMeta description = plugin.getPluginMeta();
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>" + description.getName() + " v" + description.getVersion()
            + " <reset>by <gold>" + Joiner.on(' ').join(description.getAuthors())));
        final String line = "=".repeat(ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - 2);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>SCHEMATICS : "));
        plugin.getTreeService().listTrees(sender);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<reset>" + line));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gold>PERMISSIONS : "));
        for (final Permission permission : description.getPermissions()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(sender.hasPermission(permission)
                ? "<green>- You have the permission <bold>" + permission.getName() + "<green>."
                : "<red>- You do not have the permission <bold>" + permission.getName() + "<red>."));
        }
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<reset>" + line));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<dark_aqua><italic>The above list is scrollable."));
        return true;
    }
}