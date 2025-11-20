package pl.nollis.connector.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import pl.nollis.connector.NollisPluginsConnector;

public class LobbyItemListener implements Listener {

    private final NollisPluginsConnector plugin;

    public LobbyItemListener(NollisPluginsConnector plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle player clicking the compass to open GUI
     * Using LOWEST priority to cancel the event as early as possible
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        // Check if it's the game selector compass
        if (plugin.getLobbyItemManager().isGameSelectorItem(item)) {
            // Cancel ALL interactions with the compass to prevent ANY action (including teleporting)
            event.setCancelled(true);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
            event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);

            // Only open GUI on right-click
            if (event.getAction() == Action.RIGHT_CLICK_AIR ||
                event.getAction() == Action.RIGHT_CLICK_BLOCK) {

                // Block if player is in CaveWars waiting room
                if (isInCaveWarsLobby(player)) {
                    player.sendMessage("§cYou are already in a CaveWars waiting lobby! Use §e/cavewars leave §cto return to the hub.");
                    return;
                }

                // Block if player is in CaveWars game
                if (isInCaveWarsGame(player)) {
                    player.sendMessage("§cYou are in a CaveWars game! Use §e/cavewars leave §cto return to the hub.");
                    return;
                }

                plugin.getGameModeGUIManager().openGameModeGUI(player);
            }
        }
    }

    /**
     * Check if player is in CaveWars waiting lobby using reflection
     */
    private boolean isInCaveWarsLobby(Player player) {
        try {
            Object caveWarsPlugin = plugin.getServer().getPluginManager().getPlugin("CaveWars");
            if (caveWarsPlugin == null) {
                return false;
            }

            // Get LobbyManager
            Object lobbyManager = caveWarsPlugin.getClass().getMethod("getLobbyManager").invoke(caveWarsPlugin);
            if (lobbyManager == null) {
                return false;
            }

            // Call isPlayerInAnyLobby(player)
            Boolean result = (Boolean) lobbyManager.getClass()
                    .getMethod("isPlayerInAnyLobby", Player.class)
                    .invoke(lobbyManager, player);

            return result != null && result;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if player is in CaveWars active game using reflection
     */
    private boolean isInCaveWarsGame(Player player) {
        try {
            Object caveWarsPlugin = plugin.getServer().getPluginManager().getPlugin("CaveWars");
            if (caveWarsPlugin == null) {
                return false;
            }

            // Get GameManager
            Object gameManager = caveWarsPlugin.getClass().getMethod("getGameManager").invoke(caveWarsPlugin);
            if (gameManager == null) {
                return false;
            }

            // Call isInGame(player)
            Boolean result = (Boolean) gameManager.getClass()
                    .getMethod("isInGame", Player.class)
                    .invoke(gameManager, player);

            return result != null && result;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Handle player clicking items in the GUI
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String title = event.getView().getTitle();

        // Check if it's our game mode GUI
        if (!plugin.getGameModeGUIManager().isGameModeGUI(title)) {
            // Prevent moving the compass in spawn world
            ItemStack currentItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();

            if (plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
                if ((currentItem != null && plugin.getLobbyItemManager().isGameSelectorItem(currentItem)) ||
                    (cursorItem != null && plugin.getLobbyItemManager().isGameSelectorItem(cursorItem))) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }

        // Cancel the event to prevent taking items
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // Ignore filler items (glass panes)
        if (clicked.getType() == Material.BLACK_STAINED_GLASS_PANE) {
            return;
        }

        // Handle game mode click
        plugin.getGameModeGUIManager().handleGameModeClick(player, clicked);
    }

    /**
     * Prevent dropping the compass in spawn world
     */
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        ItemStack item = event.getItemDrop().getItemStack();
        if (plugin.getLobbyItemManager().isGameSelectorItem(item)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevent swapping compass to offhand in spawn world
     */
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getSpawnManager().isSpawnWorld(player.getWorld())) {
            return;
        }

        ItemStack mainHand = event.getMainHandItem();
        ItemStack offHand = event.getOffHandItem();

        if ((mainHand != null && plugin.getLobbyItemManager().isGameSelectorItem(mainHand)) ||
            (offHand != null && plugin.getLobbyItemManager().isGameSelectorItem(offHand))) {
            event.setCancelled(true);
        }
    }
}
