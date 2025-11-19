package pl.nollis.connector.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import pl.nollis.connector.NollisPluginsConnector;

public class InventoryProtectionListener implements Listener {

    private final NollisPluginsConnector plugin;

    public InventoryProtectionListener(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevent block breaking in spawn world
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("protection.prevent-block-break", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        if (hasAdminPermission(player)) {
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Prevent block placing in spawn world
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfig().getBoolean("protection.prevent-block-place", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        if (hasAdminPermission(player)) {
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Prevent PvP in spawn world
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("protection.prevent-pvp", true)) {
            return;
        }

        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }

        if (!plugin.getSpawnManager().isSpawnWorld(victim.getWorld())) {
            return;
        }

        if (hasAdminPermission(attacker)) {
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Prevent item dropping in spawn world
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.getConfig().getBoolean("protection.prevent-item-drop", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        if (hasAdminPermission(player)) {
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Prevent item pickup in spawn world (optional)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (!plugin.getConfig().getBoolean("protection.prevent-item-pickup", false)) {
            return;
        }

        Player player = event.getPlayer();

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        if (hasAdminPermission(player)) {
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Prevent hunger in spawn world
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!plugin.getConfig().getBoolean("protection.prevent-hunger", true)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        event.setCancelled(true);
        player.setFoodLevel(20); // Keep food level at max
    }

    /**
     * Check if player has admin permission to bypass protection
     * @param player Player to check
     * @return true if player has permission or is OP
     */
    private boolean hasAdminPermission(Player player) {
        return player.isOp() || player.hasPermission("nollisconnector.admin");
    }
}
