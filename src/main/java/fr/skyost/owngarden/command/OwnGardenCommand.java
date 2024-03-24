package fr.skyost.owngarden.command;

import com.google.common.base.Joiner;
import fr.skyost.owngarden.OwnGarden;
import io.papermc.paper.plugin.configuration.PluginMeta;
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
            plugin.log(NamedTextColor.RED, "You do not have the permission to execute this command.");
            return true;
        }
        final PluginMeta description = plugin.getPluginMeta();
        sender.sendMessage("§a" + description.getName() + " v" + description.getVersion()
            + " §7by §6" + Joiner.on(' ').join(description.getAuthors()));
        final String line = "=".repeat(ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - 2);
        sender.sendMessage("§r" + line);
        sender.sendMessage("§6SCHEMATICS : ");
        sender.sendMessage("§l- Oak : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.saplingOakDir)));
        sender.sendMessage("§l- Spruce : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.saplingSpruceDir)));
        sender.sendMessage("§l- Birch : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.saplingBirchDir)));
        sender.sendMessage("§l- Jungle : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.saplingJungleDir)));
        sender.sendMessage("§l- Acacia : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.saplingAcaciaDir)));
        sender.sendMessage("§l- Dark Oak : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.saplingDarkOakDir)));
        sender.sendMessage("§l- Cherry : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.saplingCherryDir)));
        sender.sendMessage("§l- Azalea : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.saplingAzaleaDir)));
        sender.sendMessage("§l- Brown Mushroom : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.mushroomBrownDir)));
        sender.sendMessage("§l- Red Mushroom : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.mushroomRedDir)));
        sender.sendMessage("§l- Crimson Mushroom : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.mushroomCrimsonDir)));
        sender.sendMessage("§l- Warped Mushroom : §r" + Joiner.on(' ').join(OwnGarden.treeTypes.get(plugin.pluginConfig.mushroomWarpedDir)));
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