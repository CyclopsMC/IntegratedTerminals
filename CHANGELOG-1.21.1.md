# Changelog for Minecraft 1.21.1
All notable changes to this project will be documented in this file.

<a name="1.21.1-1.6.28"></a>
## [1.21.1-1.6.28](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.27...1.21.1-1.6.28) - 2026-03-10 14:24:18


### Fixed
* Fix flat plans not showing dependencies anymore

<a name="1.21.1-1.6.27"></a>
## [1.21.1-1.6.27](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.26...1.21.1-1.6.27) - 2026-03-08 08:52:43 +0100


### Fixed
* Migrate to com.gradleup.shadow to fix compilation issues

<a name="1.21.1-1.6.26"></a>
## [1.21.1-1.6.26](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.25...1.21.1-1.6.26) - 2026-03-08 07:44:12 +0100


### Fixed
* Fix crafting jobs not being sent to clients anymore

This was a regression due to b5c618b8d2bbe101f50aac79e272aeb82e9d5fb6

Closes #194

<a name="1.21.1-1.6.25"></a>
## [1.21.1-1.6.25](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.24...1.21.1-1.6.25) - 2026-02-17 11:45:52 +0100


### Changed
* Show ingredient alternatives in flat crafting plan, Closes #189

<a name="1.21.1-1.6.24"></a>
## [1.21.1-1.6.24](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.23...1.21.1-1.6.24) - 2026-01-17 14:27:49 +0100


### Changed
* Use a different icon for showin to-craft items

### Fixed
* Fix flattened plans showing too high counts on parallelized plans, Closes #190

<a name="1.21.1-1.6.23"></a>
## [1.21.1-1.6.23](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.22...1.21.1-1.6.23) - 2026-01-09 19:43:53 +0100


### Fixed
* Fix static plans never successfully deserialising

<a name="1.21.1-1.6.22"></a>
## [1.21.1-1.6.22](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.21...1.21.1-1.6.22) - 2025-12-31 15:15:47 +0100


### Added
* Add translations through Crowdin (#187)

### Changed
* Update to new crafting storage in Integrated Crafting

Required for CyclopsMC/IntegratedCrafting#112

<a name="1.21.1-1.6.21"></a>
## [1.21.1-1.6.21](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.20...1.21.1-1.6.21) - 2025-11-24 16:52:10 +0100


### Fixed
* Fix rare deadlock when opening terminal

Closes CyclopsMC/IntegratedDynamics#1572

<a name="1.21.1-1.6.20"></a>
## [1.21.1-1.6.20](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.19...1.21.1-1.6.20) - 2025-11-16 15:33:59 +0100


### Fixed
* Fix serialize threads not closing after terminal is closed
  This makes sure that thread pools are localized per container, and
  is closed once the container is closed.
  Closes #185

<a name="1.21.1-1.6.19"></a>
## [1.21.1-1.6.19](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.18...1.21.1-1.6.19) - 2025-11-15 06:06:13 +0100


### Fixed
* Avoid recipe lookups for empty crafting grids
  Closes CyclopsMC/IntegratedCrafting#164

<a name="1.21.1-1.6.18"></a>
## [1.21.1-1.6.18](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.17...1.21.1-1.6.18) - 2025-11-11 15:09:47 +0100


### Changed
* Don't re-send crafting options for every storage change event
  Related to CyclopsMC/IntegratedCrafting#156
* Serialize terminal elements in off-thead
  Related to CyclopsMC/IntegratedCrafting#156
* Deserialize terminal packets off-thread
  Related to CyclopsMC/IntegratedCrafting#156
* Allow shift-clicking in terminal crafting grid, Closes #170

### Fixed
* Fix terminal serialization thread sometimes blocking shutdown, Closes #184
* Fix dragged storage slots not rendering over other slots

<a name="1.21.1-1.6.17"></a>
## [1.21.1-1.6.17](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.16...1.21.1-1.6.17) - 2025-10-17 15:20:05 +0200


### Changed
* Don't re-send crafting options for every storage change event
  Related to CyclopsMC/IntegratedCrafting#156
* Serialize terminal elements in off-thead
  Related to CyclopsMC/IntegratedCrafting#156

<a name="1.21.1-1.6.16"></a>
## [1.21.1-1.6.16](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.15...1.21.1-1.6.16) - 2025-09-13 15:08:23 +0200


### Fixed
* Fix rare terminal crash when clicking on desynced slot, Closes #180
* Properly handle long overflows in channel quantities

<a name="1.21.1-1.6.15"></a>
## [1.21.1-1.6.15](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.14...1.21.1-1.6.15) - 2025-08-15 08:46:50 +0200


### Added
* Add translations through Crowdin (#176)
* Add PT_BR localization (#175)

### Changed
* Fix some spelling and grammar typos in lang

### Fixed
* Fix wrong slot offset for JEI fill on some recipes, Closes CyclopsMC/IntegratedTerminals#178
* Fix static crafting plan being scrollable with half rows, Closes CyclopsMC/IntegratedCrafting#150

<a name="1.21.1-1.6.14"></a>
## [1.21.1-1.6.14](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.13...1.21.1-1.6.14) - 2025-06-18 17:11:38 +0200


### Fixed
* Fix JEI search not syncing on right-click, Closes CyclopsMC/IntegratedTerminals#171

<a name="1.21.1-1.6.13"></a>
## [1.21.1-1.6.13](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.12...1.21.1-1.6.13) - 2025-06-10 17:24:03 +0200


### Fixed
* Fix unobtainable Dynamic Storage Terminal Filtering Advancement
  Closes CyclopsMC/IntegratedDynamics#1521

<a name="1.21.1-1.6.12"></a>
## [1.21.1-1.6.12](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.11...1.21.1-1.6.12) - 2025-05-25 07:02:21 +0200


### Added
* Add translations through Crowdin

### Fixed
* Fix cursor centering on gui switching, Closes CyclopsMC/IntegratedDynamics#1514

<a name="1.21.1-1.6.11"></a>
## [1.21.1-1.6.11](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.10...1.21.1-1.6.11) - 2025-05-20 17:32:59 +0200


### Fixed
* Fix search hotkey also typing in hotkey in search box
  Closes CyclopsMC/IntegratedTerminals#168
* Fix typo in manual

<a name="1.21.1-1.6.10"></a>
## [1.21.1-1.6.10](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.9...1.21.1-1.6.10) - 2025-03-11 07:54:35 +0100


### Added
* Add translations through Crowdin (#161)

### Fixed
* Fix JEI recipe fill not working for damaged items
Closes CyclopsMC/IntegratedDynamics#1485

<a name="1.21.1-1.6.9"></a>
## [1.21.1-1.6.9](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.8...1.21.1-1.6.9) - 2025-02-15 10:20:45 +0100


### Fixed
* Fix broken advancement icons

<a name="1.21.1-1.6.8"></a>
## [1.21.1-1.6.8](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.7...1.21.1-1.6.8) - 2025-02-08 16:16:32 +0100


### Added
* Add tr_tr translations through Crowdin (#153)

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

<a name="1.21.1-1.6.7"></a>
## [1.21.1-1.6.7](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.6...1.21.1-1.6.7) - 2025-01-19 10:29:15 +0100


### Added
* Add cs_cz translations

### Fixed
* Fix EMI render issue when showing button-less recipes
  Closes CyclopsMC/IntegratedTerminals#150

<a name="1.21.1-1.6.6"></a>
## [1.21.1-1.6.6](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.5...1.21.1-1.6.6) - 2024-11-27 09:42:03 +0100


### Changed
* Optimize client performance of large networks with many changes, Closes #139
* Improve JEI/REI/EMI performance, CyclopsMC/IntegratedTerminals#139

### Fixed
* Fix JEI lag caused by non-crafting recipes being considered in terminal, Closes CyclopsMC/IntegratedTerminals#141
* Fix JEI/EMI/REI cache not invalidating when player inv changes

<a name="1.21.1-1.6.5"></a>
## [1.21.1-1.6.5](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.4...1.21.1-1.6.5) - 2024-11-22 07:13:44 +0100


### Fixed
* Fix unable to clear part IDs, Closes CyclopsMC/IntegratedTunnels#309

<a name="1.21.1-1.6.4"></a>
## [1.21.1-1.6.4](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.3...1.21.1-1.6.4) - 2024-11-02 15:53:57 +0100


### Fixed
* Fix rare CME when switching terminal tabs, Closes #136

<a name="1.21.1-1.6.3"></a>
## [1.21.1-1.6.3](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.2...1.21.1-1.6.3) - 2024-10-28 16:44:26 +0100


### Fixed
* Fix EMI crash if EmiSearchWidget is not present, Closes CyclopsMC/IntegratedTerminals#133

<a name="1.21.1-1.6.2"></a>
## [1.21.1-1.6.2](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.1...1.21.1-1.6.2) - 2024-10-27 06:43:52 +0100


### Fixed
* Fix broken Curios integration, Closes CyclopsMC/IntegratedCrafting#111

<a name="1.21.1-1.6.1"></a>
## [1.21.1-1.6.1](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.6.0...1.21.1-1.6.1) - 2024-10-24 15:46:34 +0200


### Fixed
* Fix dupe bug when crafting in terminal, Closes #132

<a name="1.21.1-1.6.0"></a>
## [1.21.1-1.6.0](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.5.2...1.21.1-1.6.0) - 2024-10-23 18:50:58 +0200


### Added
* Trigger crafting jobs from JEI by Ctrl-clicking recipes, Closes CyclopsMC/IntegratedTerminals#127
* Add dedicated support for EMI
* Add dedicated support for REI

<a name="1.21.1-1.5.2"></a>
## [1.21.1-1.5.2](https://github.com/CyclopsMC/IntegratedTerminals/compare/1.21.1-1.5.1...1.21.1-1.5.2) - 2024-08-25 16:10:05 +0200


### Fixed
* Fix terminal diffs being applied to incorrect channels
  This was a regression since fae46e8f049b5a7cc861f5cdb770c01f2fb21bc7
  Closes #124
* Refer to NeoForge's updateJSONURL instead of Forge's

<a name="1.21.1-1.5.1"></a>
## [1.21.1-1.5.1] - 2024-08-09 21:12:28 +0200


### Fixed
* Fix terminals not opening when network contains enchanted items
  Closes CyclopsMC/IntegratedDynamics#1375
