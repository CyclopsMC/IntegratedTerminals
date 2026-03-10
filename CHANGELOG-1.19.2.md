# Changelog for Minecraft 1.19.2
All notable changes to this project will be documented in this file.

<a name="1.19.2-1.6.17"></a>
## [1.19.2-1.6.17](/compare/1.19.2-1.6.16...1.19.2-1.6.17) - 2026-03-10 14:22:24


### Fixed
* Fix flat plans not showing dependencies anymore

<a name="1.19.2-1.6.16"></a>
## [1.19.2-1.6.16](/compare/1.19.2-1.6.15...1.19.2-1.6.16) - 2026-02-17 11:41:13 +0100


### Added
* Show ingredient alternatives in flat crafting plan, Closes #189

<a name="1.19.2-1.6.15"></a>
## [1.19.2-1.6.15](/compare/1.19.2-1.6.14...1.19.2-1.6.15) - 2026-01-17 14:24:55 +0100


### Changed
* Use a different icon for showin to-craft items

### Fixed
* Fix flattened plans showing too high counts on parallelized plans, Closes #190

<a name="1.19.2-1.6.14"></a>
## [1.19.2-1.6.14](/compare/1.19.2-1.6.13...1.19.2-1.6.14) - 2025-11-24 16:49:25 +0100


### Fixed
* Fix rare deadlock when opening terminal

Closes CyclopsMC/IntegratedDynamics#1572

<a name="1.19.2-1.6.13"></a>
## [1.19.2-1.6.13](/compare/1.19.2-1.6.12...1.19.2-1.6.13) - 2025-11-16 15:22:42 +0100


### Fixed
* Fix serialize threads not closing after terminal is closed
  This makes sure that thread pools are localized per container, and
  is closed once the container is closed.
  Closes #185

<a name="1.19.2-1.6.12"></a>
## [1.19.2-1.6.12](/compare/1.19.2-1.6.11...1.19.2-1.6.12) - 2025-11-11 14:55:56 +0100


### Fixed
* Fix dragged storage slots not rendering over other slots
* Allow shift-clicking in terminal crafting grid, Closes #170

<a name="1.19.2-1.6.11"></a>
## [1.19.2-1.6.11](/compare/1.19.2-1.6.10...1.19.2-1.6.11) - 2025-10-17 15:11:22 +0200


### Changed
* Don't re-send crafting options for every storage change event
  Related to CyclopsMC/IntegratedCrafting#156
* Serialize terminal elements in off-thead
  Related to CyclopsMC/IntegratedCrafting#156

<a name="1.19.2-1.6.10"></a>
## [1.19.2-1.6.10](/compare/1.19.2-1.6.9...1.19.2-1.6.10) - 2025-09-13 15:06:36 +0200


### Fixed
* Fix rare terminal crash when clicking on desynced slot, Closes #180

<a name="1.19.2-1.6.9"></a>
## [1.19.2-1.6.9](/compare/1.19.2-1.6.8...1.19.2-1.6.9) - 2025-08-15 08:44:17 +0200


### Fixed
* Fix wrong slot offset for JEI fill on some recipes, Closes CyclopsMC/IntegratedTerminals#178
* Fix static crafting plan being scrollable with half rows, Closes CyclopsMC/IntegratedCrafting#150

<a name="1.19.2-1.6.8"></a>
## [1.19.2-1.6.8](/compare/1.19.2-1.6.7...1.19.2-1.6.8) - 2025-06-18 17:09:19 +0200


### Fixed
* Fix JEI search not syncing on right-click, Closes CyclopsMC/IntegratedTerminals#171

<a name="1.19.2-1.6.7"></a>
## [1.19.2-1.6.7](/compare/1.19.2-1.6.6...1.19.2-1.6.7) - 2025-05-20 17:28:29 +0200


### Fixed
* Fix search hotkey also typing in hotkey in search box
  Closes CyclopsMC/IntegratedTerminals#168
* Fix typo in manual

<a name="1.19.2-1.6.6"></a>
## [1.19.2-1.6.6](/compare/1.19.2-1.6.5...1.19.2-1.6.6) - 2025-03-11 07:50:53 +0100


### Fixed
* Fix JEI recipe fill not working for damaged items
  Closes CyclopsMC/IntegratedDynamics#1485

<a name="1.19.2-1.6.5"></a>
## [1.19.2-1.6.5](/compare/1.19.2-1.6.4...1.19.2-1.6.5) - 2025-02-08 16:01:24 +0100


### Changed
* Hide finishing crafting jobs from view, CyclopsMC/IntegratedCrafting#132

### Fixed
* Fix showing Crafting label when zero
* Fix plan view not resetting to empty when job is done, CyclopsMC/IntegratedCrafting#132
* Fix crafting job entry text overlap, Closes CyclopsMC/IntegratedCrafting#132
* Fix scroll/toggle state of crafting jobs not being preserved
  This was a regression since to the recent flat crafting job view.
  Closes CyclopsMC/IntegratedCrafting#126

<a name="1.19.2-1.6.4"></a>
## [1.19.2-1.6.4](/compare/1.19.2-1.6.3...1.19.2-1.6.4) - 2025-01-19 10:10:44 +0100


### Fixed
* Fix EMI render issue when showing button-less recipes
  Closes CyclopsMC/IntegratedTerminals#150

<a name="1.19.2-1.6.3"></a>
## [1.19.2-1.6.3](/compare/1.19.2-1.6.2...1.19.2-1.6.3) - 2024-11-27 10:00:27 +0100


### Changed
* Improve JEI/REI/EMI performance, CyclopsMC/IntegratedTerminals#139
* Optimize client performance of large networks with many changes, Closes #139

### Fixed
* Fix JEI lag caused by non-crafting recipes being considered in terminal
  Closes CyclopsMC/IntegratedTerminals#141
* Fix JEI/EMI/REI cache not invalidating when player inv changes

<a name="1.19.2-1.6.2"></a>
## [1.19.2-1.6.2](/compare/1.19.2-1.6.1...1.19.2-1.6.2) - 2024-11-02 15:50:59 +0100


### Fixed
* Fix rare CME when switching terminal tabs, Closes #136

<a name="1.19.2-1.6.1"></a>
## [1.19.2-1.6.1](/compare/1.19.2-1.6.0...1.19.2-1.6.1) - 2024-10-28 16:44:22 +0100


### Fixed
* Fix EMI crash if EmiSearchWidget is not present, Closes CyclopsMC/IntegratedTerminals#133

<a name="1.19.2-1.6.0"></a>
## [1.19.2-1.6.0](/compare/1.19.2-1.5.1...1.19.2-1.6.0) - 2024-10-23 18:50:28 +0200


### Added
* Trigger crafting jobs from JEI by Ctrl-clicking recipes, Closes CyclopsMC/IntegratedTerminals#127
* Add dedicated support for EMI
* Add dedicated support for REI

<a name="1.19.2-1.5.1"></a>
## [1.19.2-1.5.1](/compare/1.19.2-1.5.0...1.19.2-1.5.1) - 2024-08-25 16:07:01 +0200


### Fixed
* Fix terminal diffs being applied to incorrect channels
  This was a regression since fae46e8f049b5a7cc861f5cdb770c01f2fb21bc7
  Closes #124

<a name="1.19.2-1.5.0"></a>
## [1.19.2-1.5.0](/compare/1.19.2-1.4.15...1.19.2-1.5.0) - 2024-07-31 12:53:02 +0200


### Added
* Add compacted crafting plan viewing mode

  This groups all missing and available ingredients per type,
  which is more convenient to view at a glance what the problems are for
  large and nested crafting jobs.

  This is shown by default over the old tree-based view, but can be
  toggled in-game.

  This default can be changed using the `terminalStorageDefaultToCraftingPlanTree`
  config option.

  The tree-based view will be unavailable for very large crafting jobs as
  it causes packets to become too large. The threshold for this can be
  modified using the `terminalStorageMaxTreePlanSize` config option.

  Closes CyclopsMC/IntegratedTerminals#14
  Closes CyclopsMC/IntegratedDynamics#1341

### Changed
* Remove unused crafting plan in HandlerWrappedTerminalCraftingPlan

<a name="1.19.2-1.4.15"></a>
## [1.19.2-1.4.15](/compare/1.19.2-1.4.14...1.19.2-1.4.15) - 2024-07-23 13:42:12 +0200


### Fixed
* Optimize server-side ticking when terminal is open
  This skips unnecessary filtering across the whole network of ingredients
  every tick for when no variable-based filters are active in the
  terminal.
  Closes CyclopsMC/IntegratedDynamics#1359

<a name="1.19.2-1.4.14"></a>
## [1.19.2-1.4.14](/compare/1.19.2-1.4.13...1.19.2-1.4.14) - 2024-06-24 09:02:34 +0200


### Fixed
* Fix rare sorted terminal crash, Closes #119

<a name="1.19.2-1.4.13"></a>
## [1.19.2-1.4.13](/compare/1.19.2-1.4.12...1.19.2-1.4.13) - 2024-04-15 10:29:29 +0200


### Fixed
* Fix JEI-related crash at startup, Closes #10

<a name="1.19.2-1.4.12"></a>
## [1.19.2-1.4.12](/compare/1.19.2-1.4.11...1.19.2-1.4.12) - 2024-04-14 14:06:35 +0200


### Fixed
* Fix JEI not syncing initially upon terminal re-open, Closes CyclopsMC/IntegratedDynamics#1340

<a name="1.19.2-1.4.11"></a>
## [1.19.2-1.4.11](/compare/1.19.2-1.4.10...1.19.2-1.4.11) - 2023-12-04 10:25:03 +0100


### Fixed
* Lower default number of storage instances per packet
  This fixes rare cases where the terminal would cause client disconnects
  on large networks due to too many (and large) storage instances would be
  sent per packet from server to client.
  Closes #112

<a name="1.19.2-1.4.10"></a>
## [1.19.2-1.4.10](/compare/1.19.2-1.4.9...1.19.2-1.4.10) - 2023-08-29 17:38:35 +0200


### Changed
* Reduce client load when terminal storage is open
  The computational load is reduced 2-fold by reducing the number
  of times the full storage needs to be copied.
  Closes CyclopsMC/IntegratedDynamics#1303

<a name="1.19.2-1.4.9"></a>
## [1.19.2-1.4.9](/compare/1.19.2-1.4.8...1.19.2-1.4.9) - 2023-07-31 15:05:36 +0200


### Changed
* Improve quick move stack actions in the storage terminal
  This allows double-clicking on items to store all of them in the terminal.
  This also improves support for mods such as MouseTweaks.
  Closes #18.

### Fixed
* Fix specific items not being extractable from terminals
  This could occur for items that mutate their NBT tags
  during client-side rendering or tooltip creation.
  Closes #106

<a name="1.19.2-1.4.8"></a>
## [1.19.2-1.4.8](/compare/1.19.2-1.4.7...1.19.2-1.4.8) - 2023-07-15 10:35:07 +0200


### Fixed
* Fix broken compat with REI, Closes #103
* Fix crash with EMI, Closes #104

<a name="1.19.2-1.4.7"></a>
## [1.19.2-1.4.7](/compare/1.19.2-1.4.6...1.19.2-1.4.7) - 2023-05-06 16:40:46 +0200


### Fixed
* Improve quick move stack actions in the storage terminal
  This allows double-clicking on items to store all of them in the terminal.
  This also improves support for mods such as MouseTweaks.
  Closes #18.

<a name="1.19.2-1.4.6"></a>
## [1.19.2-1.4.6](/compare/1.19.2-1.4.5...1.19.2-1.4.6) - 2023-04-22 16:02:45 +0200


### Fixed
* Fix NBT size error when many recipes are in a network
  If more problems would be encountered, lowering the config value
  `terminalStoragePacketMaxRecipes` may help.
  Closes #99

<a name="1.19.2-1.4.5"></a>
## [1.19.2-1.4.5](/compare/1.19.2-1.4.4...1.19.2-1.4.5) - 2023-03-05 11:57:01 +0100


### Changed
* Use collapsed ingredient storage by default
  Ingredient networks will now perform better for match-based lookups.
  Related to CyclopsMC/IntegratedDynamics#1247

<a name="1.19.2-1.4.4"></a>
## [1.19.2-1.4.4](/compare/1.19.2-1.4.3...1.19.2-1.4.4) - 2023-02-18 07:30:53 +0100


### Fixed
* Fix lag when viewing JEI recipes
  Closes CyclopsMC/IntegratedDynamics#1247

<a name="1.19.2-1.4.3"></a>
## [1.19.2-1.4.3](/compare/1.19.2-1.4.2...1.19.2-1.4.3) - 2023-02-11 14:06:54 +0100


### Fixed
* Update to Integrated Crafting API with non-blocking mode changes

<a name="1.19.2-1.4.2"></a>
## [1.19.2-1.4.2](/compare/1.19.2-1.4.1...1.19.2-1.4.2) - 2022-12-11 13:51:52 +0100


### Fixed
* Fix crash with REI, Closes CyclopsMC/IntegratedTerminals#96
* Fix invisible "Cancel All Jobs" button, Closes #95
* Fix crash on newer JEI versions, Closes CyclopsMC/IntegratedTerminals#93

<a name="1.19.2-1.4.1"></a>
## [1.19.2-1.4.1](/compare/1.19.2-1.4.0...1.19.2-1.4.1) - 2022-10-18 12:25:46 +0200


### Fixed
* Fix portable terminal linking only working on front face, Closes #90
* Fix server crash when opening storage terminal, Closes #92

<a name="1.19.2-1.4.0"></a>
## [1.19.2-1.4.0](/compare/1.19.2-1.3.3...1.19.2-1.4.0) - 2022-10-10 18:52:13 +0200


### Added
* Add responsive terminal scaling modes
  By default, the full width and height of the screen will be taken up.
  Different modes with configuration options are available as well.
  Closes #21
* Show armor and offhand slots in terminal ui

### Fixed
* Fix item dupe when shift-clearing grid, Closes #89

<a name="1.19.2-1.3.3"></a>
## [1.19.2-1.3.3](/compare/1.19.2-1.3.2...1.19.2-1.3.3) - 2022-10-02 08:22:43 +0200


### Fixed
* Fix crash when confirming jobs via Enter, Closes #87

<a name="1.19.2-1.3.2"></a>
## [1.19.2-1.3.2](/compare/1.19.2-1.3.1...1.19.2-1.3.2) - 2022-09-17 10:58:36 +0200


### Fixed
* Fix JEI crash when clicking on energy in terminal, Closes #86

<a name="1.19.2-1.3.1"></a>
## [1.19.2-1.3.1] - 2022-08-11 19:48:29 +0200


Update to MC 1.19.2
