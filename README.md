# AntiPiracy-BuiltByBit

A Gradle plugin that injects [BuiltByBit's anti-piracy placeholders](https://builtbybit.com/wiki/anti-piracy-placeholders) into all compiled classes.

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
}
```

### Chain with shadowJar
```kotlin
tasks.named("shadowJar") {
    finalizedBy("injectLicenseFields")
}
```

## Why use this?

This plugin injects BuiltByBit placeholders into **every single class** in your jar. Even if a pirate finds the nonce value, they must remove it from a lot of classes. Missing even one allows the leak to be traced.
