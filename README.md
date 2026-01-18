# AntiPiracy-BuiltByBit

A Gradle plugin that injects [BuiltByBit's anti-piracy placeholders](https://builtbybit.com/wiki/anti-piracy-placeholders) into all compiled classes.

## Installation
```kotlin
plugins {
    id("me.serbob.antipiracy") version "(version)"
}
```

## Usage

### Basic
```kotlin
tasks.antipiracy { }
```

This injects default fields into `build/libs/${project.name}-${project.version}.jar`.

### Custom jar target
```kotlin
antipiracy {
    target("build/libs/MyPlugin-1.0-all.jar")
    // or targetBuildLibs("MyPlugin-1.0-all.jar")
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
    nonce("MY_CUSTOM_NONCE")
}
```

### Chain with shadowJar
```kotlin
tasks.named("shadowJar") {
    finalizedBy("injectLicenseFields")
}
```

## Why use this?

By default, the plugin injects fields with obfuscated names (runes, Chinese, Japanese characters) making them harder to find and remove. This helps protect your resources from piracy.

> **Note:** Only `NONCE` is suitable for anti-piracy measures. Other data is known to the downloader and easily modified. Avoid placing identifiable data alongside NONCE. Use multiple different nonces throughout your product.
