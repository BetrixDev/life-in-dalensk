# AGENTS.md - Life in Dalensk (Minecraft Fabric Mod)

## Project Overview

A Minecraft Fabric mod (1.21.1) implementing survival looter-shooter mechanics with traders, currency system, and injury effects.

**Mod ID:** `lifeindalensk`  
**Package:** `dev.betrix.lifeindalensk`  
**Java Version:** 21  
**License:** Apache-2.0

## Build Commands

```bash
# Build the mod JAR
./gradlew build

# Run Minecraft client with the mod loaded
./gradlew runClient

# Run Minecraft server with the mod loaded
./gradlew runServer

# Generate data files (loot tables, recipes, models)
./gradlew runDatagen

# Clean build artifacts
./gradlew clean

# List all available Gradle tasks
./gradlew tasks
```

**Note:** No unit testing framework is configured. Test changes by running `./gradlew runClient`.

## Project Structure

```
src/
├── client/java/dev/betrix/lifeindalensk/
│   ├── client/              # Client initializer, HUD, screens, renderers
│   └── mixin/client/        # Client-side mixins
├── main/java/dev/betrix/lifeindalensk/
│   ├── LifeInDalensk.java   # Main mod entry point
│   ├── block/               # Custom blocks and block entities
│   ├── command/             # Server commands
│   ├── currency/            # CCA components for player data
│   ├── effect/              # Status effects
│   ├── entity/              # Custom entities (traders)
│   ├── event/               # Event handlers
│   ├── inventory/           # Screen handlers
│   ├── item/                # Custom items
│   ├── mixin/               # Server-side mixins
│   ├── network/packet/      # Network packets
│   ├── registry/            # Registration classes (ModItems, ModBlocks, etc.)
│   └── trader/              # Trader system data classes
└── main/resources/
    ├── assets/lifeindalensk/    # Textures, models, lang files
    ├── data/lifeindalensk/      # Loot tables, dimension configs, trader JSON
    ├── fabric.mod.json          # Mod metadata and entrypoints
    └── lifeindalensk.accesswidener
```

## Code Style Guidelines

### Imports

- Group imports: project classes, then Fabric API, then Minecraft, then Java stdlib
- No wildcard imports
- Remove unused imports

### Formatting

- 4-space indentation (no tabs)
- Opening braces on same line as declaration
- Single blank line between methods
- Max line length ~120 characters

### Naming Conventions

- Classes: `PascalCase` (e.g., `TraderBuyC2SPacket`)
- Methods/variables: `camelCase` (e.g., `getRoubles`, `traderId`)
- Constants: `UPPER_SNAKE_CASE` (e.g., `MOD_ID`, `BROKEN_LEG_SLOWDOWN_ID`)
- Packet classes: `<Name><Direction>Packet` (e.g., `TraderBuyC2SPacket`, `SyncCurrencyS2CPacket`)
- Registry classes: `Mod<Type>` (e.g., `ModItems`, `ModBlocks`, `ModEffects`)

### Identifiers

Always use the mod ID constant for identifiers:
```java
Identifier.of(LifeInDalensk.MOD_ID, "item_name")
```

### Logging

Use the shared logger from the main class:
```java
LifeInDalensk.LOGGER.info("Message");
```

## Common Patterns

### Registry Pattern

```java
public class ModItems {
    public static final Item MY_ITEM = register("my_item", new MyItem(new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(LifeInDalensk.MOD_ID, name), item);
    }

    public static void register() {
        LifeInDalensk.LOGGER.info("Registering mod items for " + LifeInDalensk.MOD_ID);
        // Add to creative tabs here
    }
}
```

### Network Packet Pattern (C2S)

```java
public record MyC2SPacket(String data, int value) implements CustomPayload {
    public static final CustomPayload.Id<MyC2SPacket> ID = new CustomPayload.Id<>(
            Identifier.of(LifeInDalensk.MOD_ID, "my_packet"));

    public static final PacketCodec<RegistryByteBuf, MyC2SPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, MyC2SPacket::data,
            PacketCodecs.VAR_INT, MyC2SPacket::value,
            MyC2SPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

    public static void handle(MyC2SPacket packet, ServerPlayNetworking.Context context) {
        context.player().getServer().execute(() -> {
            // Handle on server thread
        });
    }
}
```

### Mixin Pattern

```java
@Mixin(TargetClass.class)
public abstract class TargetClassMixin {
    @Unique
    private static final float MY_CONSTANT = 1.0f;

    @Shadow
    public abstract void shadowedMethod();

    /**
     * Javadoc explaining what this injection does.
     */
    @Inject(method = "targetMethod", at = @At("TAIL"))
    private void onTargetMethod(CallbackInfo ci) {
        TargetClass self = (TargetClass) (Object) this;
        // Implementation
    }
}
```

### Cardinal Components Pattern

Interface:
```java
public interface MyComponent extends AutoSyncedComponent {
    ComponentKey<MyComponent> KEY = ComponentRegistry.getOrCreate(
            Identifier.of(LifeInDalensk.MOD_ID, "my_component"),
            MyComponent.class);
    // Methods
}
```

Registration in `ModComponents`:
```java
registry.registerForPlayers(MyComponent.KEY, MyComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
```

## Key Dependencies

- **Fabric API** (`fabric-api`): Core mod APIs
- **Cardinal Components API**: Player data persistence (`cardinal-components-entity`)
- **Yarn Mappings**: Deobfuscated Minecraft code

## Important Files

- `fabric.mod.json`: Mod metadata, entrypoints, mixins, dependencies
- `lifeindalensk.mixins.json` / `lifeindalensk.client.mixins.json`: Mixin configs
- `lifeindalensk.accesswidener`: Access widener for private Minecraft members
- `gradle.properties`: Version numbers for dependencies

## Error Handling

- Validate packet data before processing (null checks, bounds checks)
- Use early returns for invalid states
- Execute packet handlers on the server thread via `server.execute()`
- Don't throw exceptions in mixins - use null checks and early returns

## Adding New Content

1. **New Item**: Add to `ModItems`, create item class if custom behavior needed
2. **New Block**: Add to `ModBlocks`, create block entity if needed, add to `ModBlockEntities`
3. **New Packet**: Create in `network/packet/`, register in `ModNetworking`
4. **New Effect**: Add to `ModEffects`
5. **New Mixin**: Add class, register in appropriate mixins JSON file
6. **New Component**: Create interface + impl, register in `ModComponents`, add to `fabric.mod.json` custom section
