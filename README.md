# ShadowX

**ShadowX** is a cross-platform Rust Minecraft client/launcher project.

## Current engineering status

The repository bootstrap workflow is being prepared to import the Starling Rust client source while retaining upstream licensing and notices. The source import and initial Cargo checks are not yet confirmed successful. Do not treat this repository as a completed launcher or download a release yet.

## Intended direction

- Android and desktop targets, subject to each platform's build and runtime verification.
- Touch-control profiles, including an importer for compatible exported layouts.
- Microsoft sign-in plus a separate local/offline profile mode, without bypassing authenticated-server requirements.
- Version/instance and world management where supported by the engine.
- A ShadowX mod SDK, prioritizing a sandboxed portable API before native extensions.
- Performance profiling and device-specific graphics settings.

## Upstream and licensing

The initial implementation is based on the Starling codebase. Horizon is an upstream/reference project. These projects are not blindly overlaid: Starling already contains project-specific synchronization work. Preserve all applicable license files, copyright notices, third-party attributions, and component-level license obligations.

See [SHADOWX_SCOPE.md](SHADOWX_SCOPE.md) and [the bootstrap workflow](.github/workflows/shadowx-bootstrap.yml).

## Releases

No release should be published until the relevant platform build succeeds and the resulting artifact has been smoke-tested.