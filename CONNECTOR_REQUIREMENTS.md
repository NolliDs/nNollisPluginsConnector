# 🔌 nNollisPluginsConnector - Wymagania

**Stwórz plugin `nNollisPluginsConnector` który łączy wszystkie tryby gry na serwerze.**

---

## 📋 Wymagania Funkcjonalne

### 1️⃣ Komenda `/spawn`

**Działanie:**
- Teleportuje gracza na główne lobby (świat `spawn`)
- Teleportuje na określone koordynaty (konfigurowalny spawn point)
- Usuwa gracza z aktywnych gier we wszystkich trybach (TowerPvP, Cave Wars, etc.)
- Aliasy: `/lobby`, `/hub`

**Konfiguracja (config.yml):**
```yaml
spawn:
  world: "spawn"
  x: 0.5
  y: 100.0
  z: 0.5
  yaw: 0.0
  pitch: 0.0
```

**Przykład użycia:**
```
/spawn  →  Teleportuje na spawn (0.5, 100, 0.5)
```

---

### 2️⃣ Item na spawnie - Kompas (Game Mode Selector)

**Po teleportacji na `/spawn`:**
- Gracz otrzymuje **kompas** na **środkowym slocie** (slot 4)
- Nazwa itemka: `§6§lGame Mode Selector` (konfigurowalna)
- Material: `COMPASS`
- Kompas jest niezniszczalny (Unbreakable)

**Konfiguracja (config.yml):**
```yaml
lobby-items:
  game-selector:
    enabled: true
    slot: 4  # Środkowy slot (0-8)
    material: COMPASS
    name: "§6§lGame Mode Selector"
    lore:
      - "§7Click to choose a game mode!"
      - ""
      - "§eRight click to open menu"
```

---

### 3️⃣ GUI - Wybór Trybu Gry

**Po kliknięciu kompasu (prawy/lewy przycisk myszy):**

Otwiera się GUI (inventory) o nazwie: `§6§lGame Mode Selector`
- Rozmiar: 9 slotów (1 rząd)
- Tło: Czarne szkło (BLACK_STAINED_GLASS_PANE) na pozostałych slotach

**Zawartość GUI:**

| Slot | Item | Nazwa | Lore | Akcja |
|------|------|-------|------|-------|
| **4** (środek) | **BEDROCK** | `§6§lTower PvP` | `§7Battle on towers!`<br>`§7Build, fight, survive!`<br>`§a▶ Click to join!` | Teleport na `towerpvp_spawn` |

**Konfiguracja (config.yml):**
```yaml
game-modes:
  towerpvp:
    enabled: true
    display-name: "§6§lTower PvP"
    material: BEDROCK
    slot: 4
    lore:
      - "§7Battle on towers!"
      - "§7Build, fight, survive!"
      - ""
      - "§a▶ Click to join!"
    target-world: "towerpvp_spawn"
    target-coords:
      x: 0.5
      y: 100.0
      z: 0.5
      yaw: 0.0
      pitch: 0.0
```

---

### 4️⃣ Teleportacja do TowerPvP

**Po kliknięciu bedroka w GUI:**

1. **Zamknij GUI**
2. **Sprawdź czy gracz jest w grze TowerPvP:**
   - Jeśli TAK → usuń go z gry (użyj TowerPvP API)
3. **Teleportuj gracza na `towerpvp_spawn`:**
   - Świat: `towerpvp_spawn`
   - Koordynaty z konfiguracji (lub domyślne: 0.5, 100, 0.5)
4. **Daj graczowi itemki TowerPvP:**
   - Wywołaj: `TowerPvP.getInstance().getLobbyItemManager().giveLobbyItems(player)`
   - To daje: Game Mode Selector + Show/Hide Players
5. **Wyślij wiadomość:**
   - `§aTeleporting to Tower PvP...`

---

## 🔧 Integracja z TowerPvP API

### Używanie TowerPvP API:

```java
import pl.towerpvp.TowerPvP;
import pl.towerpvp.api.TowerPvPAPI;

// Pobierz API
TowerPvPAPI api = TowerPvP.getInstance().getAPI();

// Sprawdź czy gracz jest w grze TowerPvP
if (api.isPlayerInGame(player)) {
    player.sendMessage("§eLeaving Tower PvP game...");
    api.teleportToLobby(player); // To usunie go z gry
}

// Teleportuj na towerpvp_spawn
Location towerSpawn = new Location(
    Bukkit.getWorld("towerpvp_spawn"),
    0.5, 100.0, 0.5, 0.0f, 0.0f
);
player.teleport(towerSpawn);

// Daj itemki TowerPvP
TowerPvP.getInstance().getLobbyItemManager().giveLobbyItems(player);
```

---

## 📂 Struktura Projektu

```
nNollisPluginsConnector/
├── pom.xml
├── src/main/java/pl/nollis/connector/
│   ├── NollisPluginsConnector.java (Main class)
│   ├── commands/
│   │   └── SpawnCommand.java
│   ├── listeners/
│   │   ├── PlayerJoinListener.java
│   │   ├── LobbyItemListener.java
│   │   └── InventoryProtectionListener.java
│   ├── managers/
│   │   ├── SpawnManager.java
│   │   ├── LobbyItemManager.java
│   │   └── GameModeGUIManager.java
│   └── utils/
│       └── ConfigUtil.java
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## 📝 plugin.yml

```yaml
name: nNollisPluginsConnector
version: '1.0.0'
main: pl.nollis.connector.NollisPluginsConnector
api-version: '1.21'
depend: []
softdepend: [TowerPvP, CaveWars]
authors: [Nollis]
description: |
  Multi-game mode connector plugin
  Manages main spawn and game mode selection

commands:
  spawn:
    description: Teleport to main spawn
    usage: /spawn
    aliases: [lobby, hub]

permissions:
  nollisconnector.spawn:
    description: Use /spawn command
    default: true
  nollisconnector.admin:
    description: Admin permissions
    default: op
```

---

## ⚙️ config.yml (Domyślny)

```yaml
# ===========================================
# nNollisPluginsConnector Configuration
# ===========================================

# Main spawn location
spawn:
  world: "spawn"
  x: 0.5
  y: 100.0
  z: 0.5
  yaw: 0.0
  pitch: 0.0

# Lobby items (on main spawn)
lobby-items:
  game-selector:
    enabled: true
    slot: 4  # Middle slot (0-8)
    material: COMPASS
    name: "§6§lGame Mode Selector"
    lore:
      - "§7Click to choose a game mode!"
      - ""
      - "§eRight click to open menu"
    unbreakable: true
    hide-attributes: true

# Game modes available
game-modes:
  towerpvp:
    enabled: true
    display-name: "§6§lTower PvP"
    material: BEDROCK
    slot: 4  # Position in GUI
    lore:
      - "§7Battle on towers!"
      - "§7Build, fight, survive!"
      - ""
      - "§a▶ Click to join!"
    target-world: "towerpvp_spawn"
    target-coords:
      x: 0.5
      y: 100.0
      z: 0.5
      yaw: 0.0
      pitch: 0.0
    # Integration with TowerPvP API
    api-class: "pl.towerpvp.api.TowerPvPAPI"
    give-lobby-items: true

  # Future game mode: Cave Wars
  # cavewars:
  #   enabled: false
  #   display-name: "§c§lCave Wars"
  #   material: IRON_PICKAXE
  #   slot: 3
  #   lore:
  #     - "§7Mine and fight in caves!"
  #     - ""
  #     - "§a▶ Click to join!"
  #   target-world: "cavewars_spawn"
  #   target-coords:
  #     x: 0.5
  #     y: 64.0
  #     z: 0.5

# GUI settings
gui:
  title: "§6§lGame Mode Selector"
  size: 9  # 1 row
  filler-material: BLACK_STAINED_GLASS_PANE
  filler-name: " "

# Messages
messages:
  teleporting-to-spawn: "§aTeleporting to spawn..."
  teleporting-to-gamemode: "§aTeleporting to %gamemode%..."
  gamemode-disabled: "§cThis game mode is currently disabled!"
  no-permission: "§cYou don't have permission to use this command!"

# Protection settings
protection:
  spawn-world: true  # Protect main spawn world
  prevent-block-break: true
  prevent-block-place: true
  prevent-pvp: true
  prevent-item-drop: true
  prevent-item-pickup: false
  prevent-hunger: true
```

---

## 🎯 Funkcjonalność Krok Po Kroku

### Scenariusz 1: Gracz dołącza na serwer

1. Gracz łączy się z serwerem
2. `PlayerJoinListener` teleportuje go na spawn
3. `SpawnManager` daje mu kompas na slot 4
4. Gracz widzi kompas "§6§lGame Mode Selector"

### Scenariusz 2: Gracz klika kompas

1. Gracz klika kompas (prawy/lewy przycisk)
2. `LobbyItemListener` wykrywa kliknięcie
3. `GameModeGUIManager` otwiera GUI z bedrockiem
4. GUI pokazuje: `§6§lTower PvP` (bedrock) na slocie 4

### Scenariusz 3: Gracz wybiera Tower PvP

1. Gracz klika bedrock w GUI
2. `GameModeGUIManager` zamyka GUI
3. Sprawdza czy TowerPvP plugin jest dostępny
4. Jeśli gracz jest w grze → wywołuje `api.teleportToLobby(player)`
5. Teleportuje na `towerpvp_spawn` (0.5, 100, 0.5)
6. Wywołuje `TowerPvP.getInstance().getLobbyItemManager().giveLobbyItems(player)`
7. Gracz dostaje itemki TowerPvP (Game Mode Selector + Show/Hide Players)

### Scenariusz 4: Gracz wraca na spawn

1. Gracz pisze `/spawn`
2. `SpawnCommand` sprawdza czy jest w grze (TowerPvP/CaveWars)
3. Jeśli TAK → usuwa go z gry przez API
4. Teleportuje na główny spawn (świat `spawn`)
5. Daje mu kompas na slot 4
6. Czyści inventory z itemków gry

---

## 🔒 Ochrona Lobby (spawn world)

**Na świecie `spawn` zablokuj:**
- ✅ Niszczenie bloków
- ✅ Stawianie bloków
- ✅ PvP
- ✅ Wyrzucanie itemków
- ✅ Głód (hunger = false)

**Wyjątki:**
- OPowie mogą budować
- Gracze z permisją `nollisconnector.admin` mogą budować

---

## 📊 Integracja z Przyszłymi Pluginami (Cave Wars)

### Kiedy dodasz Cave Wars:

1. **Dodaj do config.yml:**
```yaml
game-modes:
  cavewars:
    enabled: true
    display-name: "§c§lCave Wars"
    material: IRON_PICKAXE
    slot: 3  # Lewy od Tower PvP
    lore:
      - "§7Mine and fight in caves!"
      - "§7Dig for resources!"
      - ""
      - "§a▶ Click to join!"
    target-world: "cavewars_spawn"
    target-coords:
      x: 0.5
      y: 64.0
      z: 0.5
    api-class: "pl.nollis.cavewars.api.CaveWarsAPI"
    give-lobby-items: true
```

2. **GUI automatycznie dostosuje się:**
   - Slot 3: Cave Wars (pickaxe)
   - Slot 4: Tower PvP (bedrock)
   - Slot 5: Następny tryb...

---

## 🛠️ Kluczowe Klasy Do Implementacji

### 1. SpawnCommand.java

```java
@Override
public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
        sender.sendMessage("§cOnly players!");
        return true;
    }

    // 1. Usuń z gier
    removeFromAllGames(player);

    // 2. Teleportuj na spawn
    Location spawn = spawnManager.getSpawnLocation();
    player.teleport(spawn);

    // 3. Daj lobby items
    lobbyItemManager.giveLobbyItems(player);

    player.sendMessage("§aTeleporting to spawn...");
    return true;
}

private void removeFromAllGames(Player player) {
    // TowerPvP
    if (Bukkit.getPluginManager().isPluginEnabled("TowerPvP")) {
        TowerPvPAPI api = TowerPvP.getInstance().getAPI();
        if (api.isPlayerInGame(player)) {
            api.teleportToLobby(player);
        }
    }

    // Cave Wars (future)
    // if (Bukkit.getPluginManager().isPluginEnabled("CaveWars")) {
    //     CaveWarsAPI api = CaveWars.getInstance().getAPI();
    //     if (api.isPlayerInGame(player)) {
    //         api.teleportToLobby(player);
    //     }
    // }
}
```

### 2. GameModeGUIManager.java

```java
public void openGameModeGUI(Player player) {
    Inventory gui = Bukkit.createInventory(null, 9, "§6§lGame Mode Selector");

    // Wypełnij tłem
    ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    ItemMeta fillerMeta = filler.getItemMeta();
    fillerMeta.setDisplayName(" ");
    filler.setItemMeta(fillerMeta);
    for (int i = 0; i < 9; i++) {
        gui.setItem(i, filler);
    }

    // Dodaj Tower PvP na slot 4
    ItemStack towerPvP = new ItemStack(Material.BEDROCK);
    ItemMeta meta = towerPvP.getItemMeta();
    meta.setDisplayName("§6§lTower PvP");
    meta.setLore(Arrays.asList(
        "§7Battle on towers!",
        "§7Build, fight, survive!",
        "",
        "§a▶ Click to join!"
    ));
    towerPvP.setItemMeta(meta);
    gui.setItem(4, towerPvP);

    player.openInventory(gui);
}
```

### 3. LobbyItemListener.java

```java
@EventHandler
public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    ItemStack item = event.getItem();

    if (item == null) return;

    // Kompas - otwórz GUI
    if (lobbyItemManager.isGameSelectorItem(item)) {
        event.setCancelled(true);
        guiManager.openGameModeGUI(player);
    }
}

@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) return;

    String title = event.getView().getTitle();
    if (!title.equals("§6§lGame Mode Selector")) return;

    event.setCancelled(true);

    ItemStack clicked = event.getCurrentItem();
    if (clicked == null || clicked.getType() == Material.AIR) return;

    // Bedrock → Tower PvP
    if (clicked.getType() == Material.BEDROCK) {
        player.closeInventory();
        teleportToTowerPvP(player);
    }
}

private void teleportToTowerPvP(Player player) {
    // 1. Sprawdź czy TowerPvP jest dostępny
    if (!Bukkit.getPluginManager().isPluginEnabled("TowerPvP")) {
        player.sendMessage("§cTower PvP is not available!");
        return;
    }

    // 2. Usuń z gry jeśli w grze
    TowerPvPAPI api = TowerPvP.getInstance().getAPI();
    if (api.isPlayerInGame(player)) {
        api.teleportToLobby(player);
    }

    // 3. Teleportuj na towerpvp_spawn
    Location spawn = new Location(
        Bukkit.getWorld("towerpvp_spawn"),
        0.5, 100.0, 0.5, 0.0f, 0.0f
    );
    player.teleport(spawn);

    // 4. Daj itemki TowerPvP
    TowerPvP.getInstance().getLobbyItemManager().giveLobbyItems(player);

    player.sendMessage("§aTeleporting to Tower PvP...");
}
```

---

## ✅ Checklist Implementacji

- [ ] Stworzyć strukturę projektu (Maven/Gradle)
- [ ] Dodać dependency na TowerPvP w pom.xml
- [ ] Stworzyć plugin.yml z komendą /spawn
- [ ] Stworzyć config.yml z konfiguracją spawn + GUI
- [ ] Implementować SpawnCommand (teleportacja na spawn)
- [ ] Implementować SpawnManager (zarządzanie spawn location)
- [ ] Implementować LobbyItemManager (kompas na slot 4)
- [ ] Implementować GameModeGUIManager (GUI z bedrockiem)
- [ ] Implementować LobbyItemListener (kliknięcie kompasu + GUI)
- [ ] Implementować PlayerJoinListener (teleport na spawn po join)
- [ ] Implementować InventoryProtectionListener (ochrona lobby)
- [ ] Dodać integrację z TowerPvP API
- [ ] Testować teleportację: spawn → towerpvp_spawn → spawn
- [ ] Testować dawanie itemków TowerPvP
- [ ] Testować usuwanie z gry przed teleportacją

---

## 🧪 Scenariusze Testowe

### Test 1: Teleportacja na spawn
```
1. Gracz pisze /spawn
2. Sprawdź: Czy teleportował się na świat "spawn" (koordynaty z config)
3. Sprawdź: Czy ma kompas na slocie 4
4. PASS jeśli TAK
```

### Test 2: Otwieranie GUI
```
1. Gracz klika kompas (prawy przycisk)
2. Sprawdź: Czy otworzyło się GUI "§6§lGame Mode Selector"
3. Sprawdź: Czy na slocie 4 jest bedrock "§6§lTower PvP"
4. PASS jeśli TAK
```

### Test 3: Teleportacja do Tower PvP
```
1. Gracz klika bedrock w GUI
2. Sprawdź: Czy teleportował się na "towerpvp_spawn"
3. Sprawdź: Czy ma itemki TowerPvP (Game Mode Selector + Hide Players)
4. Sprawdź: Czy kompas z spawna zniknął
5. PASS jeśli TAK
```

### Test 4: Usuwanie z gry
```
1. Gracz jest w grze TowerPvP
2. Gracz pisze /spawn
3. Sprawdź: Czy został usunięty z gry
4. Sprawdź: Czy teleportował się na spawn
5. PASS jeśli TAK
```

### Test 5: Ochrona lobby
```
1. Gracz (nie-op) próbuje zniszczyć blok na spawnie
2. Sprawdź: Czy akcja została zablokowana
3. Gracz próbuje zaatakować innego gracza
4. Sprawdź: Czy PvP jest zablokowane
5. PASS jeśli TAK
```

---

## 📞 Kontakt z TowerPvP API

**Dostępne metody API:**

| Metoda | Opis |
|--------|------|
| `isPlayerInGame(Player)` | Czy gracz jest w grze? |
| `isPlayerInParty(Player)` | Czy gracz jest w party? |
| `isTowerPvPWorld(World)` | Czy świat należy do TowerPvP? |
| `teleportToLobby(Player)` | Teleportuj do lobby TowerPvP |
| `getActiveGames()` | Pobierz aktywne gry |
| `getTotalPlayersInGames()` | Liczba graczy w grach |

**Przykład:**
```java
TowerPvPAPI api = TowerPvP.getInstance().getAPI();
if (api.isPlayerInGame(player)) {
    api.teleportToLobby(player);
}
```

---

## 📦 Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Paper API -->
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.21.1-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>

    <!-- TowerPvP (soft-depend) -->
    <dependency>
        <groupId>pl.towerpvp</groupId>
        <artifactId>TowerPvP</artifactId>
        <version>3.4-BETA</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 🎨 Kolorystyka

**Użyj tych kolorów:**
- `§6` - Złoty (nazwy, tytuły)
- `§e` - Żółty (lore, akcje)
- `§7` - Szary (opisy)
- `§a` - Zielony (sukces)
- `§c` - Czerwony (błędy)

---

## 🚀 Następne Kroki (Po Zakończeniu)

1. Przetestować całą funkcjonalność
2. Dodać więcej trybów gry do config.yml (Cave Wars)
3. Dodać statystyki graczy (opcjonalne)
4. Dodać top graczy na hologramach (opcjonalne)

---

**WAŻNE:** Plugin MUSI być kompatybilny z TowerPvP v3.4-BETA i działać zgodnie z wymaganiami opisanymi powyżej!

**Good luck! 🎮**
