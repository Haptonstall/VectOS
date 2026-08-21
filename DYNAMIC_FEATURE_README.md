# Dynamic Feature Delivery — apply instructions

## Applying

This patch assumes the build-fix patch from earlier in this session
(`vectos-build-fix.patch`) is already applied — it is, since your build and
test runs succeeded after that. From your repo root:

```
git apply --check vectos-dynamic-feature-delivery.patch   # dry run
git apply vectos-dynamic-feature-delivery.patch
```

Verified against a fresh clone at the exact state your repo should be in
right now (post build-fix) — applies without conflicts.

14 files changed: 10 modified, 2 new, 1 deleted, plus a new `res/` directory
for `feature/beam`.

**This is the largest-risk patch of the session and the one I'm least able
to pre-verify** — it touches Gradle plugin types, an AndroidManifest, and a
real Google Play library (`com.google.android.play:feature-delivery` /
`-ktx`, version 2.1.0, confirmed current via Google's own Play Feature
Delivery documentation). None of this compiles in my sandbox (no Android
SDK). A `./gradlew build` here is not optional — do it before anything else.

---

## What changed, and why

### The crash's actual root cause
`DefaultInstalledModuleRepository` was a hardcoded stub that unconditionally
claimed the beam module was `INSTALLED`/`enabled=true` regardless of
licensing or whether its code was physically present. `:feature:beam` was a
plain `com.android.library` module with zero relationship (compile or
dynamic-feature) to `:app` — so `Class.forName("com.lz.beam.api.Beam
RuntimeModuleProvider")` had no way to ever succeed. That's the crash.

### The fix: real Android Dynamic Feature Modules
- **`feature/beam/build.gradle.kts`**: plugin changed `com.android.library`
  -> `com.android.dynamic-feature`; added `implementation(project(":app"))`
  (required by AGP — dynamic-feature modules depend on the base app, the
  reverse of a normal library, which is exactly what keeps `:app` itself
  dependency-free of feature modules); `minSdk` aligned to 28 to match `:app`.
- **`app/build.gradle.kts`**: added `dynamicFeatures += setOf(":feature:beam")`
  — an AGP packaging association, not a Gradle dependency edge. Added the
  Play Feature Delivery dependency. Enabled `buildConfig = true` (needed for
  the debug-only subscription convenience below).
- **`feature/beam/src/main/AndroidManifest.xml`**: added the `<dist:module>`
  block — on-demand delivery, `fusing include="true"`. Fusing is what makes
  a plain `./gradlew installDebug` / Android Studio "Run" install this
  module locally as a genuine split APK without any Play Store transaction —
  this is the practical answer to "how do we test this locally."
- **`feature/beam/src/main/res/values/strings.xml`**: new — the manifest's
  `dist:title` needs a string resource.
- **`gradle/libs.versions.toml`**: added the `android-dynamic-feature`
  plugin alias and `play-feature-delivery`/`-ktx` library aliases (v2.1.0).

### The actual discovery logic (this is the part that was genuinely missing)
- **`ModuleDescriptor.kt`**: added `entryPointClassName` (which provider
  class to `Class.forName()`) and `requiresSubscription` (defaults `true`;
  set `false` for beam per your loss-leader plan — see
  `LocalModuleCatalogRepository.kt`).
- **New: `SubscriptionAwareInstalledModuleRepository.kt`** — replaces the
  deleted `DefaultInstalledModuleRepository` stub. For each catalog module,
  cross-references `SubscriptionRepository` (licensed?) against
  `SplitInstallManager.installedModules` (physically present?) and reports
  accurate `InstallState`/`enabled`. The existing, unmodified
  `RuntimeModuleInstaller` (runtime.loader) already correctly only attempts
  to load modules that come back both `enabled` and `INSTALLED` — a
  licensed-but-not-yet-downloaded module is safely skipped at boot rather
  than crashing.
- **`RuntimeModuleInstaller.kt`** (app/plugin layer, implements the domain
  `ModuleInstaller`): now actually calls `SplitInstallManager.startInstall()`
  instead of just checking an existing registry. This is the trigger for
  downloading a module a user is licensed for but doesn't have yet — not
  wired into any UI in this pass, but the mechanism is real and callable.
- **`VectosApplication.kt`**: `attachBaseContext()` now calls
  `SplitCompat.install(this)` — without this, an installed split's classes
  can be on disk but still fail `Class.forName()` (an easy, common thing to
  miss with Hilt-based Applications, since the usual `SplitCompatApplication`
  base class isn't compatible with Hilt's generated base class).
- **`RuntimeInitializer.kt` / `ModuleBindings.kt`**: wired the new
  repository through Hilt's DI graph in place of the old stub.
- **`RuntimeManager.kt`**: found and fixed an adjacent, not-yet-triggered
  bug while touching this area — it maintained its own separate singleton
  field that nothing ever populated (`.initialize()` had zero callers), so
  `RuntimeViewModel.kt`'s use of it would have thrown the moment it was
  actually exercised. Now delegates directly to `RuntimeInitializer`'s
  already-correctly-populated singleton instead of duplicating that state.

### Your second question — giving the test app a "subscription"
`VectosApplication.grantDebugSubscriptions()` (debug builds only, gated by
`BuildConfig.DEBUG`) auto-grants every catalog module on startup. Beam
itself doesn't need this anymore (loss-leader, `requiresSubscription = false`
— always considered licensed), but it's there for testing the gating logic
itself and any future paid module (e.g. "column"). Deliberately synchronous
(`runBlocking`, not fire-and-forget) — see the in-code comment for why an
async grant would race against `MainActivity`'s startup license check.

---

## Known caveats — please verify these on a real device

- **Local-install split detection is the biggest unknown.** I'm relying on
  the standard, documented behavior that a project with `dynamicFeatures`
  declared gets all its splits installed together via a normal debug
  install, and that `SplitInstallManager.installedModules` correctly
  reports a locally-installed (not Play-delivered) split as present. This
  is well-established Play Feature Delivery behavior, but I have no way to
  confirm it against your specific AGP 8.13.2 setup without a device.
- **Hilt + dynamic-feature modules is a known friction point** in the
  Android ecosystem generally — `feature/beam` still applies the Hilt
  plugin and does its own KSP codegen. If Hilt component generation across
  the split boundary causes issues, that's the first place to look.
- **`GooglePlayPurchaseManager`** (the real production entitlement source)
  wasn't touched or verified in this pass — the debug auto-grant bypasses
  it entirely, which is correct for local testing but means real Play
  Billing wiring is still a separate, unverified piece.

---

## Deliberately not touched — flagging, not fixing

While tracing this, I found a **third** parallel module-registration
mechanism: `feature/beam/src/main/java/com/lz/beam/plugin/BeamPlugin.kt`
implements a `CalculatorPlugin` interface referencing a *different*
`com.lz.domain.module.ModuleEntryPoint` type and a `BeamEntryPoint` class I
never opened. It has zero consumers anywhere in the codebase today.
Similarly, `DefaultModuleBootstrapper.bootstrap()` (the `domain.module`
layer's own "Later: discover installed dynamic features..." placeholder)
is left as a no-op — it also has zero consumers today, and wiring it
correctly would mean first understanding how it's meant to relate to
`BeamPlugin`/`CalculatorPlugin`. Building further on top of a third
unmapped mechanism in the same pass as everything else risked exactly the
kind of tangle the regulatory-editions work ran into earlier this session.
Worth its own investigation before anyone builds on it.
