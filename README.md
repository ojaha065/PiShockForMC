# PiShock Shock Collar Integration for Minecraft
This simple Forge mod allows players
to connect their [PiShock](https://pishock.com) device to the game for added realism and _...fun..._:smiling_imp:
Whenever the player takes damage, they will get a corresponding shock,
the intensity of which can be configured and will scale up based on the amount of damage taken.

**This is a client-side mod, it should only be installed client-side, and it works in multiplayer too.**

## Supported Minecraft/Minecraft Forge versions
* 1.19.x (Minecraft Forge 44.x, 45.x)

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
The mod is licensed under a MIT license, so feel free to include it in any modpack. No attribution required.

This mod should be compatible with almost everything.
If you're using other mods that alter the player health (e.g., changes the maxium health),
please note that the shock intensity scaling might not work as expected.
However, even in that case the player will never be shocked with higher intensity than the configured `intensity_range` allows.

## Mod configuration
The mod configuration file is named `pishockmc-client.toml` and it can be edited with any text editor. In-game settings GUI might be added in a later release to make configuring easier. The configuration file will look like this:

```
#"Shock" is the intended mode for this mod. Vibrate and Beep are more suitable for testing and debugging.
#Allowed Values: Shock, Vibrate, Beep
mode = "Shock"
#Set the shock/vibration/beep intensity range
#The percentage ranges from lowest to highest are: 1 - 20, 21 - 40, 41 - 60, 61 - 80, 80 - 100
#Allowed Values: MINIMAL, NORMAL, INTENSE, HARDCORE, ULTRA_HARDCORE
intensity_range = "MINIMAL"
#If enabled, sends 5 second shock/vibrate/beep at the maximum* intensity when the player dies
#(*the maximum is based on the configured intensity range)
punishment_for_death = false

[pishock]
	#Username you use to log into PiShock.com. Can be found in the Account section of the website.
	username = ""
	#API Key generated on PiShock.com Can be found in the Account section of the website.
	apikey = ""
	#Sharecode generated on PiShock.com. Limitations can be set when generating the code.
	code = ""


```

`username`, `apikey` and `code` are all mandatory, and you must get all of them from https://pishock.com. It's also important to set the desired intensity level (`intensity_range`). The default value is `MINIMAL`, but personally I feel that `NORMAL` has the best balance between feeling kinda nasty but not being too overwhelming. But it's all very dependent on each person's pain tolerance and location of the shocker, so feel free to experiment.

### Punishment for death
Setting `punishment_for_death` option to `true` will send 5-seconds shock at the maxium (within the configured `intensity_range`) intensity when the player dies in-game.
More configurable (intensity, duration etc.) punishment might be added in a later release.

### :warning: Important notice
The mod uses duration of 600 milliseconds for most shocks.
Using milliseconds instead of seconds is currently undocumented feature in PiShock API,
and it seems that `Max Duration` setting for share codes does not work correctly with it.
When creating a share code, the `Max Duration` value needs to be set to at least 6 (seconds).

### Intensity ranges
| intensity_range  | Shock intensity % range |
| ---------------- | ----------------------- |
| MINIMAL          | 1% - 20%                |
| NORMAL           | 21% - 40%               |
| INTENSE          | 41% - 60%               |
| HARDCORE         | 61% - 80%               |
| ULTRA_HARDCORE   | 81% - 100%              |

## TODO
* **Support for multiple shockers**
  * I currently only own one, so testing and debugging would be kinda hard.
* **Support for wider range of MC/Forge versions**
* In-game configuration GUI
* More configuration options
* Better documentation

## The boring stuff
### tl;dr
Don't be stupid.

### Long version
Although nothing about this repository is inherently age gated,
the PiShock device and themes around it are targeted towards mature audiences only.
Proceed at your own discretion.

The author of this mod is not responsible for any injuries caused by use of any shock collar.
It's not recommended to put any kind of electrical device near the heart
or use any kind of shock collar if you have a heart condition.
Shock collars are not meant for use on humans and can cause serious injury, even cardiac events.
I kindly urge you to prioritize safety,
understand your personal and others limitations, and exercise caution at all times.
