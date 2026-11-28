# ShadowX

ShadowX is an independent Minecraft client and launcher project based on the Starling source tree, with Horizon upstream compatibility work. Upstream notices and third-party licenses remain in this repository; see `LICENSE`, `NOTICE`, and `THIRD_PARTY_NOTICES.md`.

## Project components

| Component | Current implementation |
| --- | --- |
| Android game client | Rust `NativeActivity` application packaged as an APK. The build supports ARM64, ARM32, x86 and x86_64; ARM64 is the primary Android build target. |
| Desktop launcher | Separate Rust/GPUI application for Windows, macOS and Linux. It includes account, instance, version, mod and server-management flows. |
| Touch controls | Native touch state machine, editable layouts, custom controls, Bedrock-style controls and strict JSON import/export validation. |
| Mod tooling | Rust CLI exposed as `cargo shadowx-mod`; supports content mods and code-mod workflows (WASM and native templates). |
| Android mods | Android loads native mod libraries built for the target Android ABI and matching game build. Desktop binaries and arbitrary WASM mods are not assumed to run on Android. |

## CI builds and downloads

- The **ShadowX CI** workflow runs Rust formatting, workspace/launcher checks, tests and optimized desktop launcher compilation on the development branch.
- The **ShadowX bootstrap and build** workflow builds the Android ARM64 development APK and desktop launcher artifacts. Open the repository's Actions tab, select the completed workflow run, and download the `shadowx-android-arm64-dev` artifact for the APK.
- The **ShadowX release builds** workflow builds desktop packages and Android APKs for ARM64, ARM32, x86 and x86_64. It publishes a GitHub Release when a `launcher@*` tag is pushed.
- CI APKs use a development/debug signing key unless a release keystore is explicitly configured. They are for development and installation testing, not Play Store production signing.

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

## Platform scope and compatibility

The Android APK is the native game client; it is not yet a full Android Java Edition launcher equivalent to Mojo Launcher. The desktop launcher and Android client are separate front ends. The native touch-layout format has strict import/export validation. Pojav/Mojo control-map JSON uses a different schema and must pass through a dedicated converter before it can be claimed as compatible. A full Android instance/version manager and end-to-end Microsoft/offline account flow on Android are not claimed as complete.

A successful compile proves source/build compatibility only. It does not replace device smoke tests, multiplayer checks, graphics-driver checks or installer testing.

## License and attribution

ShadowX retains the upstream project licenses, copyright notices and third-party notices. Branding changes do not transfer ownership of upstream code or assets.
