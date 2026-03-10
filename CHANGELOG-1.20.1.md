# Changelog for Minecraft 1.20.1
All notable changes to this project will be documented in this file.

<a name="1.20.1-1.6.20"></a>
## [1.20.1-1.6.20](/compare/1.20.1-1.6.19...1.20.1-1.6.20) - 2026-03-10 14:23:33


### Fixed
* Fix flat plans not showing dependencies anymore

<a name="1.20.1-1.6.19"></a>
## [1.20.1-1.6.19](/compare/1.20.1-1.6.18...1.20.1-1.6.19) - 2026-03-08 07:41:08 +0100


### Fixed
* Fix crafting jobs not being sent to clients anymore

This was a regression due to b5c618b8d2bbe101f50aac79e272aeb82e9d5fb6

Closes #194

<a name="1.20.1-1.6.18"></a>
## [1.20.1-1.6.18](/compare/1.20.1-1.6.17...1.20.1-1.6.18) - 2026-02-17 11:43:36 +0100


### Changed
* Show ingredient alternatives in flat crafting plan, Closes #189

<a name="1.20.1-1.6.17"></a>
## [1.20.1-1.6.17](/compare/1.20.1-1.6.16...1.20.1-1.6.17) - 2026-01-17 14:26:49 +0100


### Changed
* Use a different icon for showin to-craft items

### Fixed
* Fix flattened plans showing too high counts on parallelized plans, Closes #190

<a name="1.20.1-1.6.16"></a>
## [1.20.1-1.6.16](/compare/1.20.1-1.6.15...1.20.1-1.6.16) - 2026-01-09 19:42:01 +0100


### Fixed
* Fix static plans never successfully deserialising

<a name="1.20.1-1.6.15"></a>
## [1.20.1-1.6.15](/compare/1.20.1-1.6.14...1.20.1-1.6.15) - 2025-12-31 15:10:58 +0100


### Changed
* Update to new crafting storage in Integrated Crafting

Required for CyclopsMC/IntegratedCrafting#112

<a name="1.20.1-1.6.14"></a>
## [1.20.1-1.6.14](/compare/1.20.1-1.6.13...1.20.1-1.6.14) - 2025-11-24 16:50:13 +0100


### Fixed
* Fix rare deadlock when opening terminal

Closes CyclopsMC/IntegratedDynamics#1572

<a name="1.20.1-1.6.13"></a>
## [1.20.1-1.6.13](/compare/1.20.1-1.6.12...1.20.1-1.6.13) - 2025-11-16 15:28:48 +0100


### Fixed
* Fix serialize threads not closing after terminal is closed
  This makes sure that thread pools are localized per container, and
  is closed once the container is closed.
  Closes #185

<a name="1.20.1-1.6.12"></a>
## [1.20.1-1.6.12](/compare/1.20.1-1.6.11...1.20.1-1.6.12) - 2025-11-11 15:05:04 +0100


### Fixed
* Fix terminal serialization thread sometimes blocking shutdown, Closes #184
* Deserialize terminal packets off-thread
  Related to CyclopsMC/IntegratedCrafting#156
* Fix dragged storage slots not rendering over other slots
* Allow shift-clicking in terminal crafting grid, Closes #170
* Don't re-send crafting options for every storage change event
  Related to CyclopsMC/IntegratedCrafting#156

<a name="1.20.1-1.6.11"></a>
## [1.20.1-1.6.11](/compare/1.20.1-1.6.10...1.20.1-1.6.11) - 2025-10-17 15:12:19 +0200


### Changed
* Don't re-send crafting options for every storage change event
  Related to CyclopsMC/IntegratedCrafting#156
* Serialize terminal elements in off-thead
  Related to CyclopsMC/IntegratedCrafting#156

<a name="1.20.1-1.6.10"></a>
## [1.20.1-1.6.10](/compare/1.20.1-1.6.9...1.20.1-1.6.10) - 2025-09-13 15:07:40 +0200


### Fixed
* Fix rare terminal crash when clicking on desynced slot, Closes #180
* Properly handle long overflows in channel quantities

<a name="1.20.1-1.6.9"></a>
## [1.20.1-1.6.9](/compare/1.20.1-1.6.8...1.20.1-1.6.9) - 2025-08-15 08:44:50 +0200


### Fixed
* Fix wrong slot offset for JEI fill on some recipes, Closes CyclopsMC/IntegratedTerminals#178
* Fix static crafting plan being scrollable with half rows, Closes CyclopsMC/IntegratedCrafting#150

<a name="1.20.1-1.6.8"></a>
## [1.20.1-1.6.8](/compare/1.20.1-1.6.7...1.20.1-1.6.8) - 2025-06-18 17:09:30 +0200


### Fixed
* Fix JEI search not syncing on right-click, Closes CyclopsMC/IntegratedTerminals#171

<a name="1.20.1-1.6.7"></a>
## [1.20.1-1.6.7](/compare/1.20.1-1.6.6...1.20.1-1.6.7) - 2025-05-20 17:30:39 +0200


### Fixed
* Fix search hotkey also typing in hotkey in search box
  Closes CyclopsMC/IntegratedTerminals#168
* Fix typo in manual

<a name="1.20.1-1.6.6"></a>
## [1.20.1-1.6.6](/compare/1.20.1-1.6.5...1.20.1-1.6.6) - 2025-03-11 07:53:57 +0100


### Fixed
* Fix JEI recipe fill not working for damaged items
Closes CyclopsMC/IntegratedDynamics#1485

<a name="1.20.1-1.6.5"></a>
## [1.20.1-1.6.5](/compare/1.20.1-1.6.4...1.20.1-1.6.5) - 2025-02-08 16:14:08 +0100


### Changed
* Optimize client performance of large networks with many changes, Closes #139
* Hide finishing crafting jobs from view, CyclopsMC/IntegratedCrafting#132

### Fixed
* Fix showing Crafting label when zero
* Fix plan view not resetting to empty when job is done, CyclopsMC/IntegratedCrafting#132
* Fix crafting plan title not centered in flat mode, Closes CyclopsMC/IntegratedCrafting#132
* Fix crafting job entry text overlap, Closes CyclopsMC/IntegratedCrafting#132
* Fix scroll/toggle state of crafting jobs not being preserved
  This was a regression since to the recent flat crafting job view.
  Closes CyclopsMC/IntegratedCrafting#126

<a name="1.20.1-1.6.4"></a>
## [1.20.1-1.6.4](/compare/1.20.1-1.6.3...1.20.1-1.6.4) - 2025-01-19 10:10:55 +0100


### Fixed
* Fix EMI render issue when showing button-less recipes
  Closes CyclopsMC/IntegratedTerminals#150

<a name="1.20.1-1.6.3"></a>
## [1.20.1-1.6.3](/compare/1.20.1-1.6.2...1.20.1-1.6.3) - 2024-11-27 10:00:47 +0100


### Changed
* Improve JEI/REI/EMI performance, CyclopsMC/IntegratedTerminals#139
* Optimize client performance of large networks with many changes, Closes #139

### Fixed
* Fix JEI lag caused by non-crafting recipes being considered in terminal
  Closes CyclopsMC/IntegratedTerminals#141
* Fix JEI/EMI/REI cache not invalidating when player inv changes

<a name="1.20.1-1.6.2"></a>
## [1.20.1-1.6.2](/compare/1.20.1-1.6.1...1.20.1-1.6.2) - 2024-11-02 15:51:46 +0100


### Fixed
* Fix rare CME when switching terminal tabs, Closes #136

<a name="1.20.1-1.6.1"></a>
## [1.20.1-1.6.1](/compare/1.20.1-1.6.0...1.20.1-1.6.1) - 2024-10-28 16:44:25 +0100


### Fixed
* Fix EMI crash if EmiSearchWidget is not present, Closes CyclopsMC/IntegratedTerminals#133

<a name="1.20.1-1.6.0"></a>
## [1.20.1-1.6.0](/compare/1.20.1-1.5.1...1.20.1-1.6.0) - 2024-10-23 18:50:44 +0200


### Added
* Trigger crafting jobs from JEI by Ctrl-clicking recipes, Closes CyclopsMC/IntegratedTerminals#127
* Add dedicated support for EMI
* Add dedicated support for REI

<a name="1.20.1-1.5.1"></a>
## [1.20.1-1.5.1](/compare/1.20.1-1.5.0...1.20.1-1.5.1) - 2024-08-25 16:08:27 +0200


### Fixed
* Fix terminal diffs being applied to incorrect channels
  This was a regression since fae46e8f049b5a7cc861f5cdb770c01f2fb21bc7
  Closes #124

<a name="1.20.1-1.5.0"></a>
## [1.20.1-1.5.0](/compare/1.20.1-1.4.16...1.20.1-1.5.0) - 2024-07-31 13:12:14 +0200


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

<a name="1.20.1-1.4.16"></a>
## [1.20.1-1.4.16](/compare/1.20.1-1.4.15...1.20.1-1.4.16) - 2024-07-23 13:45:53 +0200


### Fixed
* Optimize server-side ticking when terminal is open
  This skips unnecessary filtering across the whole network of ingredients
  every tick for when no variable-based filters are active in the
  terminal.
  Closes CyclopsMC/IntegratedDynamics#1359

<a name="1.20.1-1.4.15"></a>
## [1.20.1-1.4.15](/compare/1.20.1-1.4.14...1.20.1-1.4.15) - 2024-06-24 09:05:44 +0200


### Fixed
* Fix rare sorted terminal crash, Closes #119

<a name="1.20.1-1.4.14"></a>
## [1.20.1-1.4.14](/compare/1.20.1-1.4.13...1.20.1-1.4.14) - 2024-04-15 10:29:59 +0200


### Fixed
* Fix JEI-related crash at startup, Closes #10

<a name="1.20.1-1.4.13"></a>
## [1.20.1-1.4.13](/compare/1.20.1-1.4.12...1.20.1-1.4.13) - 2024-04-14 14:09:58 +0200


### Fixed
* Fix JEI not syncing initially upon terminal re-open, Closes CyclopsMC/IntegratedDynamics#1340

<a name="1.20.1-1.4.12"></a>
## [1.20.1-1.4.12](/compare/1.20.1-1.4.11...1.20.1-1.4.12) - 2023-12-04 10:27:10 +0100


### Fixed
* Lower default number of storage instances per packet
  This fixes rare cases where the terminal would cause client disconnects
  on large networks due to too many (and large) storage instances would be
  sent per packet from server to client.
  Closes #112

<a name="1.20.1-1.4.11"></a>
## [1.20.1-1.4.11](/compare/1.20.1-1.4.10...1.20.1-1.4.11) - 2023-08-29 17:45:31 +0200


### Fixed
* Reduce client load when terminal storage is open
  The computational load is reduced 2-fold by reducing the number
  of times the full storage needs to be copied.
  Closes CyclopsMC/IntegratedDynamics#1303

<a name="1.20.1-1.4.10"></a>
## [1.20.1-1.4.10](/compare/1.20.1-1.4.9...1.20.1-1.4.10) - 2023-07-31 15:10:24 +0200


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

<a name="1.20.1-1.4.9"></a>
## [1.20.1-1.4.9](/compare/1.20.1-1.4.8...1.20.1-1.4.9) - 2023-07-15 10:38:42 +0200


### Fixed
* Fix broken compat with REI, Closes #103
* Fix crash with EMI, Closes #104

<a name="1.20.1-1.4.8"></a>
## [1.20.1-1.4.8] - 2023-07-02 08:11:44 +0200


Initial 1.20.1 release
