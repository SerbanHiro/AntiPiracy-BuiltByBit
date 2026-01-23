# AntiPiracy-BuiltByBit

A Gradle plugin that injects [BuiltByBit's anti-piracy placeholders](https://builtbybit.com/wiki/anti-piracy-placeholders) into all compiled classes.

## Features

- **Automatic injection** — Injects placeholders into every class in your jar
- **Randomized field names** — Field names are randomly generated each build
- **Hidden nonce** — Only ONE random class contains the real nonce, all others get fake values
- **Multiple nonce support** — Each nonce placeholder gets its own random class
- **Nonce mapping file** — Tracks which class has the real nonce for each version

## Installation
```kotlin
plugins {
    id("me.serbob.antipiracy") version "1.0.4"
}
```

## Usage

### Basic
```kotlin
antipiracy { }
```

This injects default fields into `build/libs/${project.name}-${project.version}.jar`.

### Custom jar target
```kotlin
antipiracy {
    target("build/libs/MyPlugin-1.0-all.jar")
    // or
    targetBuildLibs("MyPlugin-1.0-all.jar")
}
```

### Adding fields

There are two types of fields: **regular fields** and **nonce fields**.

#### Regular fields

Regular fields are injected into **every class** with the same value.
```kotlin
antipiracy {
    // With a specific name (same name in every class)
    addField("BUYER_ID", "%%__USER__%%")
    
    // With a random name (different random name per class)
    addRandomField("%%__USERNAME__%%")
}
```

#### Nonce fields

Nonce fields are special — the real placeholder is injected into **one random class**, while all other classes get fake values that look identical.
```kotlin
antipiracy {
    // With a specific name
    addNonceField("LICENSE", "%%__NONCE__%%")
    
    // With a random name (different random name per class)
    addRandomNonceField("%%__NONCE__%%")
}
```

#### Multiple nonces

You can add multiple nonce placeholders. Each one will be hidden in a **different random class**:
```kotlin
antipiracy {
    addNonceField("LICENSE", "%%__NONCE__%%")
    addRandomNonceField("%%__NONCE2__%%")
    addRandomNonceField("%%__NONCE3__%%")
}
```

Result:
```
PlayerListener.class (assigned for %%__NONCE__%%):
    LICENSE = "%%__NONCE__%%"        // real
    xK9mQ2 = "a8f3b2c1d4e5..."       // fake
    pL5wR1 = "9c2d1e4f5a6b..."       // fake

MainCommand.class (assigned for %%__NONCE2__%%):
    zT8kM3 = "b7d4e9f2a1c8..."       // fake
    aB3nF7 = "%%__NONCE2__%%"        // real
    kW4mP9 = "2f8a4c6e1b3d..."       // fake

Helper.class (assigned for %%__NONCE3__%%):
    qR7sN2 = "c1e5f9a3b7d2..."       // fake
    mJ6tK8 = "4a8c2e6f0b9d..."       // fake
    vX3pL5 = "%%__NONCE3__%%"        // real
```

### Disable default fields

By default, the plugin injects all BuiltByBit placeholders. To use only your custom fields:
```kotlin
antipiracy {
    noDefaultFields()
    addField("BUYER", "%%__USER__%%")
    addNonceField("LICENSE", "%%__NONCE__%%")
}
```

### Disable hidden nonce

By default, only one random class gets the real nonce placeholder. All other classes get fake nonce values. To disable this and inject real placeholders everywhere:
```kotlin
antipiracy {
    disableHiddenNonce()
}
```

### Chain with shadowJar
```kotlin
tasks.named("shadowJar") {
    finalizedBy("injectLicenseFields")
}
```

## Configuration Reference

| Method | Description |
|--------|-------------|
| `target(path)` | Set the jar file path to inject |
| `targetBuildLibs(path)` | Set the jar file path relative to `build/libs/` |
| `noDefaultFields()` | Disable default BuiltByBit placeholder injection |
| `disableHiddenNonce()` | Put real nonce in all classes (no fakes) |
| `addField(name, value)` | Add a field with specific name to all classes |
| `addRandomField(value)` | Add a field with random name to all classes |
| `addNonceField(name, value)` | Add a nonce field with specific name (hidden in one class) |
| `addRandomNonceField(value)` | Add a nonce field with random name (hidden in one class) |

## How It Works

### Hidden Nonce System

When enabled (default), the plugin:

1. Injects regular placeholders into **all classes**
2. For each nonce placeholder, picks **one random class** to receive the real value
3. All other classes receive **fake nonces** (random 32-char hex strings that look identical)
4. Saves the real nonce class locations to `nonce-mappings.properties`

This means pirates see nonce-like strings everywhere but can't tell which one is real.

### Nonce Mappings File

After each build, the plugin saves the real nonce locations to `nonce-mappings.properties` in your project root:
```properties
# Real nonce class mappings - DO NOT SHARE
MyPlugin-1.0.%%__NONCE__%%=com/example/listeners/PlayerListener
MyPlugin-1.0.%%__NONCE2__%%=com/example/commands/MainCommand
MyPlugin-1.1.%%__NONCE__%%=com/example/utils/Helper
MyPlugin-1.1.%%__NONCE2__%%=com/example/events/JoinListener$Handler
```

> **Important:** Add `nonce-mappings.properties` to your `.gitignore`. Never share this file.

## Finding the Nonce in a Leaked Jar

When you find a leaked copy of your plugin:

1. Check `nonce-mappings.properties` for the version's real nonce class
2. Open the leaked jar (it's just a zip file)
3. Navigate to the class path listed in the mappings
4. Decompile or inspect the class to find the nonce value
5. Report the nonce to BuiltByBit staff

### About Class Names

The class path might contain `$` characters, like:
```
me/serbob/myplugin/listeners/PlayerListener$EventHandler
```

This is normal — `$` indicates an **inner class**. In this example, `EventHandler` is an inner class of `PlayerListener`. Just navigate to `PlayerListener.class` and look for the inner class inside.

## Disclaimer

No client-side protection is uncrackable. This plugin makes piracy **tedious**, not impossible. Determined pirates can still remove protections. The goal is to ensure lazy leakers leave traces behind.