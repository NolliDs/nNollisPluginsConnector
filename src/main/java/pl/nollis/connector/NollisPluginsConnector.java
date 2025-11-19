package pl.nollis.connector;

import org.bukkit.plugin.java.JavaPlugin;
import pl.nollis.connector.commands.SpawnCommand;
import pl.nollis.connector.listeners.InventoryProtectionListener;
import pl.nollis.connector.listeners.LobbyItemListener;
import pl.nollis.connector.listeners.PlayerJoinListener;
import pl.nollis.connector.managers.GameModeGUIManager;
import pl.nollis.connector.managers.LobbyItemManager;
import pl.nollis.connector.managers.SpawnManager;

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
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyItemListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryProtectionListener(this), this);
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
