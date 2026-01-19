# AntiPiracy-BuiltByBit

A Gradle plugin that injects [BuiltByBit's anti-piracy placeholders](https://builtbybit.com/wiki/anti-piracy-placeholders) into all compiled classes.

## Features

- **Automatic injection** — Injects placeholders into every class in your jar
- **Randomized field names** — Field names are randomly generated each build
- **Hidden nonce** — Only ONE random class contains the real nonce, all others get fake values
- **Nonce mapping file** — Tracks which class has the real nonce for each version

## Installation
```kotlin
plugins {
    id("me.serbob.antipiracy") version "(latest version)"
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

### Add custom fields
```kotlin
antipiracy {
    addField("LICENSE_KEY", "%%__NONCE__%%")
    addField("BUYER_ID", "%%__USER__%%")
}
```

### Disable default fields
```kotlin
antipiracy {
    noDefaultFields()
    addField("MY_NONCE", "%%__NONCE__%%")
}
```

### Disable hidden nonce

By default, only one random class gets the real `%%__NONCE__%%` placeholder. All other classes get fake nonce values. To disable this and inject real placeholders everywhere:
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

## How It Works

### Hidden Nonce System

When enabled (default), the plugin:

1. Injects placeholders into **all classes**
2. Picks **one random class** to receive the real `%%__NONCE__%%` placeholder
3. All other classes receive **fake nonces** (random 32-char hex strings that look identical)
4. Saves the real nonce class location to `nonce-mappings.properties`

This means pirates see nonce-like strings everywhere but can't tell which one is real.

### Nonce Mappings File

After each build, the plugin saves the real nonce location to `nonce-mappings.properties` in your project root:
```properties
# Real nonce class mappings - DO NOT SHARE
MyPlugin-1.0=com/example/listeners/PlayerListener
MyPlugin-1.1=com/example/commands/MainCommand
MyPlugin-2.0=com/example/utils/Helper$Inner
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

This is normal — `$` indicates an **inner class**. In this example, `EventHandler` is  inner class of `anPlayerListener`. Just navigate to `PlayerListener.class` and look for the inner class inside.

## Disclaimer

No client-side protection is uncrackable. This plugin makes piracy **tedious**, not impossible. Determined pirates can still remove protections. The goal is to ensure lazy leakers leave traces behind.
