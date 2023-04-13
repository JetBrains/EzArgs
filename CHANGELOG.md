<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# EzArgs Changelog

## [Unreleased]
### Fixed
- [RIDER-89156](https://youtrack.jetbrains.com/issue/RIDER-89156/EZ-Args-combobox-changes-color-after-changing-theme)

## [1.0.17]
### Added
- Localization

## [1.0.16]
### Fixed
- Bump plugins versions for 2023.1 compatibility

## [1.0.15]
### Fixed
- Editor field expanding indefinitely occupying whole toolbar

### Added
- Expand button (with Shift + Enter hotkey) to easily modify command arguments
- Settings to limit number of command line arguments history
- Settings to set the size of the edit field
- Settings to change behavior for overwriting Program Arguments from selected Run Configuration
  - Before: EzArgs was appending its arguments to the end of the program arguments of selected Run Configuration
  - Now: you have an option in Settings to change this behavior to overwrite the program arguments from Run Configuration 
- Support for new experimental IntelliJ UI

## [1.0.13]
### Fixed
- Bump plugins versions for 2022.3 compatibility

## [1.0.12]
### Fixed
- Add support for Rider 2022.3 EAP

## [1.0.11]
### Fixed
- Add compatibility with the new toolbar

## [1.0.10]
### Fixed
- Add support for Rider 2022.2 EAP

## [1.0.9]
### Fixed
- Add PROPER support for Rider 2022.1 EAP

## [1.0.8]
### Fixed
- Add support for Rider 2022.1

## [1.0.7]
### Fixed
- Size of editor panel was way of from the rest of toolbar elements

## [1.0.6]
### Fixed
- Support for New Toolbar UI

## [1.0.5]
### Added
- Support for Rider 213 SDK

## [1.0.4]
### Added
- Basic code completion for UE4 command line arguments 

### Fixed
- Fix [A long list of arguments will permanently break the UI](https://github.com/JetBrains/EzArgs/issues/3)

## [1.0.3]

## [1.0.2]
### Fixed
- Fix https://github.com/JetBrains/EzArgs/issues/1

## [1.0.1]
### Added
- Initial release
- Add editable dropdown box on toolbar to specify arguments for C++ Run Configuration