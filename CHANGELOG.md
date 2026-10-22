# Changelog

All notable changes to Arqon are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

<!--
How to write entries
- Add every player-visible or developer-visible change under [Unreleased] in the same PR/commit that makes it.
- Sections, in this order: Added, Changed, Deprecated, Removed, Fixed, Security.
- One line per change, imperative mood, no trailing period. Reference an issue or PR when there is one: (#12).
- Gameplay balance changes (numbers, rates, thresholds) go under Changed and must include the old and new value.
- On release: rename [Unreleased] to [x.y.z] - YYYY-MM-DD, add a fresh empty [Unreleased] above it,
  and update the compare links at the bottom.
-->

## [Unreleased]

### Added

- Thermometer item: shows the air temperature above the hotbar while held in either hand
- Clinical thermometer item: costs half a hunger point per use and shows body temperature (36.6 °C by default)
- Air temperature model based on biome, time of day and weather (rain -3 °C, thunderstorm -5 °C); no day/night or weather in the Nether and the End
- Body temperature that drifts by 0.1 °C every 2 minutes depending on the air temperature, saved with the player and reset on death
- Heat debuffs: Asphyxia (38.0-39.9 °C) and Hyperthermia (40.0-41.9 °C), instant death by hyperthermia at 42.0 °C and above
- Cold debuffs: Mild Hypothermia (35.0-32.1 °C) and Moderate Hypothermia (32.0-28.0 °C), instant death by hypothermia below 28.0 °C
- Crafting recipes for both thermometers
- English and Russian translations
- Developer-only `/bodytemp <value>` command, registered in the development environment only

<!-- After the GitHub repository exists, add compare links here, for example:
[Unreleased]: https://github.com/<user>/arqon/compare/v0.1.0...HEAD
-->
