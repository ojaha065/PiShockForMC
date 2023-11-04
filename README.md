# PiShock Shock Collar Integration for Minecraft
This simple Forge mod allows players
to connect their [PiShock](https://pishock.com) device to the game for added realism and _...fun..._:smiling_imp:
Whenever the player takes damage, they will get a corresponding shock,
the intensity of which can be configured and will scale up based on the amount of damage taken.

**This is a client-side mod. It works in multiplayer too but should be installed and configured client-side only.**

## :information_source: Important info for PiShock firmware V3 users
If you have updated your PiShock firmware to V3 (beta) you must use version 1.1.2.0 or later of the mod.

## Supported Minecraft/Minecraft Forge versions
* 1.19.x (Minecraft Forge 44.x, 45.x)
* 1.20.x (Minecraft Forge 46.x, 47.x, 48.x)

## Needed hardware
* A PiShock
* A shocker — needs to be paired to the PiShock
  * See [PiShock website](https://pishock.com) for help on pairing your shocker.
  * Multiple shockers might be supported in a future release.

## Setup instructions (tl;dr — I've done this kind of thing before, and I know what I'm doing)
1. Download the latest release and drop the jar into Forge `mods` folder.
2. Launch the game once and then see the _Mod configuration_ section below.
3. Launch the game again. If everything is set currently, the shocker will vibrate once for one second during the Minecraft startup process.

## Setup instructions (longer version)
0. Have a working Minecraft and Forge installation. See [How to install Forge](https://www.wikihow.com/Install-Minecraft-Forge) if you're unsure.
   * Make sure you've launched the game at least once with Forge installed so the required folder structure is generated.
1. Download the latest release jar from [here](https://github.com/ojaha065/PiShockForMC/releases).
2. Drop the downloaded .jar file into `mods` folder inside Minecraft game directory. See [Where are Minecraft files stored?](https://help.minecraft.net/hc/en-us/articles/4409159214605-Managing-Data-and-Game-Storage-in-Minecraft-Java-Edition-#h_01FGA90Z06DE00GT8E81SWX9SE) if you're unsure how to find the correct game directory.
3. Run the game once so a configuration file for the mod gets generated.
4. See the _Mod configuration_ section below. The configuration file can be found inside `config` folder inside Minecraft game directory.
5. Launch the game again. If everything is set correctly, the shocker will vibrate once for one second during the Minecraft startup process.

## Modpacks and compatability with other mods
The mod is licensed under a MIT license, so feel free to include it in any modpack. No attribution is required.

This mod should be compatible with almost everything.
If you're using other mods that alter the player health (e.g., changes the maxium health),
please note that the shock intensity scaling might not work as expected.
However, even in that case the player will never be shocked with higher intensity than the configured `intensity_range` allows.

## Mod configuration
The mod configuration file is named `pishockmc-client.toml` and it can be edited with any text editor. In-game settings GUI might be added in a later release to make configuring easier. The configuration file will look like this:

```
mode = "Shock"
intensity_range = "NORMAL"

[punishment_for_death]
	enabled = false
	intensity = 50
	duration = 5

[pishock]
	username = ""
	apikey = ""
	code = ""
```

`username`, `apikey` and `code` are all mandatory, and you must get all of them from [pishock.com](https://pishock.com).
It's also important to set the desired intensity level (`intensity_range`).
The default value is `MINIMAL`, but personally I feel that `NORMAL`or `INTENSE` have the best balance between feeling kinda nasty but not being too overwhelming.
But it's all very dependent on each person's pain tolerance and location of the shocker, so feel free to experiment.
Also, please be aware that for most people getting shocked at the same spot multiple times heightens the sensation and each consecutive shock will be more painful.

### Punishment for death
Setting `punishment_for_death` --> `enabled` option to `true` will send a shock with the configured intensity and duration when the player dies in-game.

### :warning: Important notice
The mod uses duration of 600 milliseconds for shocks.
Using milliseconds instead of seconds is currently undocumented feature in PiShock API,
and it seems that `Max Duration` setting for share codes does not work correctly with it.
When creating a share code,
the `Max Duration` value needs to be set to at least 6 (seconds)
or the value of `punishment_for_death` --> `duration` if it's higher than 6.

### Intensity ranges
| intensity_range | Maxium intensity % |
|-----------------|--------------------|
| MINIMAL         | 20%                |
| NORMAL          | 40%                |
| INTENSE         | 60%                |
| HARDCORE        | 80%                |
| ULTRA_HARDCORE  | 100%               |

## Common issues and FAQ
### Sometimes the shocker isn't activated when it should
From personal experience,
I've found that sometimes the PiShock device itself and/or the shocker are a little finicky
and might sometimes (rarely) skip some shocks if multiple ones are sent in a short period of time.

If you're experiencing this,
the first step is to check the logs at [pishock.com](https://pishock.com)
and see if a correct amount API events from `PiShock integration for Minecraft` is displayed.
If so, the problem is at PiShock's end, and the mod really cannot do anything about it.

## TODO
* **Support for multiple shockers**
  * I currently only own one, so testing and debugging would be kinda hard.
* In-game configuration GUI
* More configuration options

## The boring stuff
### tl;dr
Don't be stupid.

### Long version
Although nothing about this repository is inherently age gated,
the PiShock device and themes around it are targeted towards mature audiences only.
Proceed at your own discretion.

The authors of this mod are not responsible for any injuries caused by use of any shock collar.
It's not recommended to put any kind of electrical device near the heart
or use any kind of shock collar if you have a heart condition.
Shock collars are not meant for use on humans and can cause serious injury, even cardiac events.
We kindly urge you to prioritize safety,
understand your personal and others limitations, and exercise caution at all times.
