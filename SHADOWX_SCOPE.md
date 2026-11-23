# ShadowX engineering scope

This repository starts from Starling's Rust client implementation. Horizon is treated
as an upstream/reference source; the two repositories are not blindly overlaid because
Starling already contains a synchronization merge and project-specific changes.

## Targets
- Preserve the upstream engine and all applicable license/copyright notices.
- Rebrand only after identifying the actual app metadata, package identifiers, icons,
  launcher resources, and platform-specific manifests.
- Maintain Android and desktop targets independently, sharing engine code where supported.
- Keep Microsoft authentication and legitimate offline/local profiles as separate modes.
- Preserve multiplayer authentication requirements; offline profiles do not bypass online-mode
  server authentication or game ownership checks.
- Build a ShadowX mod SDK only against stable engine APIs. Prefer sandboxed WASM for
  portable user mods; native Rust extensions require platform-specific builds and review.
- Import Mojo control layouts only through a documented compatibility adapter; do not assume
  its private/runtime formats are interchangeable with Starling controls.
- Do not claim platform support until its CI build and runtime smoke test pass.
