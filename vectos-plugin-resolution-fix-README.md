# vectos-plugin-resolution-fix.patch

## What broke
After `vectos-dynamic-feature-delivery.patch` (patch 3) landed, `./gradlew build`
failed at the very first step:

```
Error resolving plugin [id: 'com.android.dynamic-feature', version: '8.13.2']
> The request for this plugin could not be satisfied because the plugin is
  already on the classpath with an unknown version, so compatibility cannot
  be checked.
```

## Root cause
The root `build.gradle.kts` registers AGP's plugins (`com.android.library`,
`com.android.application`, etc.) in its top-level `plugins { }` block with
`apply false`. This is the standard AGP multi-module convention — it resolves
each AGP plugin's classes onto the buildscript classpath exactly once, and
every submodule then applies the plugin without a version.

`com.android.dynamic-feature` was added to `feature/beam/build.gradle.kts`
(via `alias(libs.plugins.android.dynamic.feature)`) as part of patch 3, but
was never added to the root file's `plugins { }` block. Gradle's per-module
version resolution then conflicted with the plugin already being present on
the classpath from AGP's single bundled artifact, causing the resolution
failure.

## Fix
One line added to the root `build.gradle.kts`, registering the plugin the
same way its siblings already are:

```kotlin
alias(libs.plugins.android.dynamic.feature) apply false
```

Placed alongside `android.library` (both are AGP-family plugins), before
`hilt.android`.

The corresponding version-catalog alias (`android-dynamic-feature`) was
already added to `gradle/libs.versions.toml` by patch 3 — confirmed present
before writing this patch, so no catalog changes are needed here.

## What this patch does NOT verify
This is a resolution-error fix, not a build-success guarantee. Per the
handoff, patch 3 is entirely unverified beyond this point:

- Whether the rest of patch 3 compiles cleanly (Hilt + dynamic-feature
  module component generation across the split boundary is a known
  general friction point — watch for Hilt-specific errors next).
- Whether `SplitInstallManager.installedModules` actually reports the
  `beam` split as present after a local `installDebug` on this AGP
  8.13.2 setup.
- Runtime behavior of `VectosApplication.grantDebugSubscriptions()`,
  `SubscriptionAwareInstalledModuleRepository`, or any of patch 3's
  other new wiring.

## How to apply
```
git apply --check vectos-plugin-resolution-fix.patch
git apply vectos-plugin-resolution-fix.patch
./gradlew build
```

If `./gradlew build` succeeds, the next backlog items are #2–#6 in the
handoff (run on device, verify the migration against a real v1 DB, click
through all 6 building codes, spot-check the wood ASD utilization change).

If it fails with a *different* error (not plugin resolution), paste the
full console output back — likely candidates per the handoff are Hilt
component generation across the dynamic-feature boundary.
