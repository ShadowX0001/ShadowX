# ShadowX

ShadowX is an independent Minecraft client and launcher project based on the Starling source tree, with Horizon upstream compatibility work. Upstream notices and third-party licenses remain in this repository; see `LICENSE`, `NOTICE`, and `THIRD_PARTY_NOTICES.md`.

## Project components

| Component | Current implementation |
| --- | --- |
| Android client | Rust `NativeActivity` game client packaged as an APK. The Android build script supports ARM64, ARM32, x86 and x86_64 ABIs; runtime testing is still required on real devices. |
| Desktop launcher | Separate Rust/GPUI application for Windows, macOS and Linux. Its source includes account, instance, version, mod and server-management flows. |
| Mod tooling | Rust CLI exposed as `cargo shadowx-mod`; supports content mods and code-mod workflows (WASM and native templates). |
| Android mods | The current Android client loads native mod libraries. Native mods must be built for the target Android ABI and the matching game build. Do not assume desktop binaries or every WASM mod can run on Android. |

## Build and release

- The `ShadowX bootstrap and build` workflow checks the workspace and produces CI artifacts.
- The `ShadowX release builds` workflow builds desktop packages and Android APKs for ARM64, ARM32, x86 and x86_64; each ABI is published as a separate asset. Push a `v*` tag to publish a GitHub Release after all required jobs pass.
- CI APKs use the build script's local debug signing key unless a release keystore is explicitly configured. They are suitable for testing, not a Play Store production signing setup.
- A successful compile is not a substitute for device smoke tests, multiplayer tests, graphics-driver checks or installer testing. Android APKs built by CI use a development signing key and are not suitable as production Play Store releases.

## Mod SDK quick start

Build the CLI from this repository:

```sh
cargo build --release -p cargo-horizon-mod --bin cargo-shadowx-mod
```

Create a WASM code-mod project:

```sh
cargo shadowx-mod new my-mod --template wasm
cd my-mod
cargo shadowx-mod check
cargo shadowx-mod build --wasm
```

For a native code mod, use `--template native`. Native mods are platform-specific and should only use declared/stable mod APIs; pin and check the target game build before distributing them.

Useful commands include `cargo shadowx-mod hooks`, `cargo shadowx-mod sdk`, `cargo shadowx-mod fits` and `cargo shadowx-mod run`. Run `cargo shadowx-mod --help` to see the CLI's current command help.

## Important platform boundaries

The Android app is currently the native game client, not a full Android launcher equivalent to Mojo Launcher. The desktop launcher and Android client are separate application front ends. Mojo control-layout import, a full Mojo-style Android instance/version manager, and verified end-to-end offline-account behavior on every platform are not claimed as implemented. These require dedicated implementation and real-device validation.

## License and attribution

ShadowX retains the upstream project licenses, copyright notices and third-party notices. Branding changes do not transfer ownership of upstream code or assets.
