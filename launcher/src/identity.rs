//! Repository-selected identity. An upstream build retains its original namespaces.
macro_rules! identity {
    ($($name:ident: $upstream:literal => $fork:literal),* $(,)?) => {$ (
        pub const $name: &str = if cfg!(horizon_fork_launcher) { $fork } else { $upstream };
    )*};
}
identity! {
    VERSION_PREFIX: "shadowx-launcher " => "horizon-fork-launcher ",
    DESKTOP_ENTRY: "shadowx-launcher.desktop" => "io.github.arismoko.horizonfork.launcher.desktop",
    WINDOW_TITLE: "ShadowX" => "Horizon Fork Launcher (arismoko)",
    QUIT_LABEL: "Quit ShadowX" => "Quit Horizon Fork Launcher (arismoko)",
    DISPLAY_NAME: "ShadowX Launcher" => "Horizon Fork Launcher (arismoko)",
    EXECUTABLE: "shadowx-launcher" => "horizon-fork-launcher",
    WINDOWS_EXECUTABLE: "shadowx-launcher.exe" => "horizon-fork-launcher.exe",
    WINDOWS_EXECUTABLE_PREFIX: "shadowx-launcher.exe." => "horizon-fork-launcher.exe.",
    APP_ID: "dev.shadowx.launcher" => "io.github.arismoko.horizonfork.launcher",
    KEYCHAIN_SERVICE: "dev.shadowx.launcher" => "io.github.arismoko.horizonfork.launcher",
    LOCATION_FOLDER: "shadowx-launcher-location" => "horizon-fork-arismoko-location",
    LAUNCHER_FOLDER: "shadowx/launcher" => "horizon-fork-arismoko/launcher",
    GAME_FOLDER: "shadowx/game" => "horizon-fork-arismoko/game",
    PUBLISHER: "ShadowX" => "arismoko",
    UNINSTALL_KEY: "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\HorizonLauncher" => "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\ArismokoHorizonForkLauncher",
    INSTALL_KEY: "Software\\ShadowX\\Launcher" => "Software\\ArismokoHorizonFork\\Launcher",
    REGISTRY_PARENT: "Software\\ShadowX" => "Software\\ArismokoHorizonFork",
    UNINSTALL_PREFIX: "shadowx-launcher-uninstall-" => "horizon-fork-launcher-uninstall-",
    SETUP_PREFIX: "shadowx-launcher-setup" => "horizon-fork-launcher-setup",
    PROCESS_FILTER: "IMAGENAME eq shadowx-launcher.exe" => "IMAGENAME eq horizon-fork-launcher.exe",
}

/// Product home used by the Windows installer's About link.
pub const ABOUT_URL: &str = if cfg!(horizon_fork_launcher) {
    horizon_addresses::FORK_LAUNCHER_ABOUT
} else {
    horizon_addresses::LAUNCHER_ABOUT
};

/// The complete launcher-owned suffix, not just a generic leaf selected by an override.
/// Uninstallation keeps the existing named-folder guard for relocated data as well.
pub fn owns_launcher_data_dir(path: &std::path::Path) -> bool {
    let mut actual = path.components().rev();
    std::path::Path::new(LAUNCHER_FOLDER)
        .components()
        .rev()
        .all(|expected| {
            actual.next().is_some_and(|component| {
                component
                    .as_os_str()
                    .eq_ignore_ascii_case(expected.as_os_str())
            })
        })
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::path::Path;

    #[test]
    fn data_removal_requires_the_complete_owned_namespace() {
        let profile = Path::new("profile");
        assert!(owns_launcher_data_dir(&profile.join(LAUNCHER_FOLDER)));
        assert!(owns_launcher_data_dir(
            &profile.join(LAUNCHER_FOLDER.to_ascii_uppercase())
        ));
        let other = if cfg!(horizon_fork_launcher) {
            "horizon-launcher"
        } else {
            "horizon-fork-arismoko/launcher"
        };
        for unowned in [
            other,
            "launcher",
            "unrelated/launcher",
            "not-horizon-fork-arismoko/launcher",
        ] {
            assert!(!owns_launcher_data_dir(&profile.join(unowned)), "{unowned}");
        }
    }
}
