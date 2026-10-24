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
- Local air temperature: shade and roofs (including tree canopies), underground cave temperature, altitude cooling above Y 70, wind chill in the open, and sun load in warm biomes
- Nearby heat and cold sources change the air temperature: lava, fire, soul fire, lit campfires and furnaces, magma blocks, torches, ice, packed ice, blue ice, snow blocks, powder snow
- Sensory feedback without numbers: heat haze and frost vignette on the screen, aim shiver during hypothermia, breath vapor in the cold, sweat drops in the heat
- Body and air temperature are synced to the player (needed for the visual feedback)
- Client entrypoint and separate client source set (`src/client`)
- Shelters: a closed room (walls, roof, closed doors, up to 250 air blocks) keeps heat in, pulls the air halfway toward cave temperature, and makes heat and cold sources up to 1.6x stronger in tiny rooms
- Clothing, two sets of four pieces with worn textures and recipes: Wool (Cap, Coat, Trousers, Boots) protects from cold but is worse in heat, Linen (Hat, Shirt, Trousers, Sandals) protects from heat
- Vanilla armor now affects temperature: leather slightly warms, metal armor (chainmail, iron, gold, diamond, netherite) makes both cold and heat slightly worse
- More heat sources: lava cauldron, lit redstone lamp, redstone torch and candles, soul torch, lantern, soul lantern, jack o'lantern, shroomlight, glowstone; frosted ice as a cold source
- Clothing and shelters affect what the body feels and what the visual feedback shows; the thermometer still shows the plain air temperature
- Body model with six parts: head, torso, two arms, two legs
- Frostbite per body part: parts freeze when the air they feel is below 0 °C, extremities first, faster when the whole body is cold; warming up only in real warmth; severe frostbite (75+) does not heal on its own
- Clothing now protects per part: a hat the head, a coat the torso and arms, trousers and boots the legs
- Effects Frostbitten Legs (-15 % movement speed per level) and Frostbitten Arms (-15 % attack and mining speed per level)
- Body screen (key B, rebindable): an outlined silhouette of the six parts with a frost icon on affected parts, a feeling panel (body and surroundings in words) and a panel for the hovered part with condition, a four-step bar and its consequence; no numbers, panels widen to fit the longest text; hint messages above the hotbar when a part goes numb or frostbitten
- Effects Frostbitten Face (vision darkens at the edges, sprinting costs more food) and Frostbitten Torso (the body cools 1.25x / 1.5x / 1.75x faster)
- Heartbeat in the ears when the torso frostbite reaches 85 and above, louder and faster towards 100
- Developer-only `/frost <part|all> <0-100>` command

### Changed

- Clothing textures redrawn: open face under hats, wool cap with cuff and pom, coat with collar, buttons and elbow patches, short-sleeved linen shirt, belts, boots and sandals; item icons match
- Thermometer shows the local air temperature (shade, roof, sources) instead of the plain biome value
- Body temperature drift now scales with how far the air is from the comfortable range: 0.25x just outside it, 1x at 10 °C outside (the old constant rate of 0.1 °C per 2 minutes), up to 2x
- Upper comfort limit of the air: 30 °C -> 32 °C
- Biome temperature scale: 20 -> 16 °C per 1.0 of vanilla biome temperature (desert about 34 °C on average instead of 39 °C)
- Day and night swing of the air: 7 °C -> 6 °C, plus up to 6 °C of direct sun in open warm biomes
- Nether now gets a +6 °C offset on top of the biome value (about 40 °C)

<!-- After the GitHub repository exists, add compare links here, for example:
[Unreleased]: https://github.com/<user>/arqon/compare/v0.1.0...HEAD
-->
