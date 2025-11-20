package pl.nollis.connector;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pl.nollis.connector.commands.ReloadCommand;
import pl.nollis.connector.commands.SpawnCommand;
import pl.nollis.connector.commands.TowerPvPCommand;
import pl.nollis.connector.listeners.InventoryProtectionListener;
import pl.nollis.connector.listeners.LobbyItemListener;
import pl.nollis.connector.listeners.PlayerJoinListener;
import pl.nollis.connector.listeners.SecureLoginListener;
import pl.nollis.connector.managers.GameModeGUIManager;
import pl.nollis.connector.managers.LobbyItemManager;
import pl.nollis.connector.managers.SpawnManager;
import pl.nollis.connector.tasks.CompassCheckTask;

public class NollisPluginsConnector extends JavaPlugin {

    private static NollisPluginsConnector instance;

    private SpawnManager spawnManager;
    private LobbyItemManager lobbyItemManager;
    private GameModeGUIManager gameModeGUIManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.spawnManager = new SpawnManager(this);
        this.lobbyItemManager = new LobbyItemManager(this);
        this.gameModeGUIManager = new GameModeGUIManager(this);

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        // Start compass check task (runs every 5 seconds = 100 ticks)
        new CompassCheckTask(this).runTaskTimer(this, 100L, 100L);

        getLogger().info("nNollisPluginsConnector has been enabled!");
        getLogger().info("Game mode connector is ready!");
    }

    @Override
    public void onDisable() {
        getLogger().info("nNollisPluginsConnector has been disabled!");
    }

    private void registerCommands() {
        SpawnCommand spawnCommand = new SpawnCommand(this);
        getCommand("spawn").setExecutor(spawnCommand);

        TowerPvPCommand towerPvPCommand = new TowerPvPCommand(this);
        getCommand("lobby").setExecutor(towerPvPCommand);
        getCommand("hub").setExecutor(towerPvPCommand);

        ReloadCommand reloadCommand = new ReloadCommand(this);
        getCommand("nreload").setExecutor(reloadCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryProtectionListener(this), this);

        // Register SecureLogin listener if SecureLogin plugin is present
        if (Bukkit.getPluginManager().isPluginEnabled("SecureLogin")) {
            SecureLoginListener secureLoginListener = new SecureLoginListener(this);
            secureLoginListener.register();
            getLogger().info("SecureLogin integration enabled!");
        }
    }

    public static NollisPluginsConnector getInstance() {
        return instance;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public LobbyItemManager getLobbyItemManager() {
        return lobbyItemManager;
    }

    public GameModeGUIManager getGameModeGUIManager() {
        return gameModeGUIManager;
    }
}
