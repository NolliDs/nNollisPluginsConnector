package pl.nollis.connector.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import pl.nollis.connector.NollisPluginsConnector;

public class ReloadCommand implements CommandExecutor {

    private final NollisPluginsConnector plugin;

    public ReloadCommand(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check permission
        if (!sender.hasPermission("nollisconnector.admin") && !sender.isOp()) {
            sender.sendMessage(plugin.getConfig().getString("messages.no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        // Reload config
        plugin.reloadConfig();
        sender.sendMessage("§a[nNollisPluginsConnector] Configuration reloaded successfully!");
        plugin.getLogger().info("Configuration reloaded by " + sender.getName());

        return true;
    }
}
