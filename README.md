> [!IMPORTANT]
> Development work halted in October 2024. There are far better alternatives available now than this one. Feel free to fork this repository if for some odd reason someone wants to continue maintaining this.

# PiShock Shock Collar Integration for Minecraft: Java Edition
This Forge mod allows players
to connect their [PiShock](https://pishock.com) device to the game for added realism and _...fun..._:smiling_imp:.
Whenever the player takes damage, they will get a corresponding shock,
the intensity of which can be configured and will scale up based on the amount of damage taken.

**This is a client-side mod. It works in multiplayer too but should be installed and configured client-side only.**

> [!IMPORTANT]
> If you have updated your PiShock firmware to V3 (beta), you must use version 1.1.2.0 or later of the mod.

## Supported Minecraft/Minecraft Forge versions
### Fully supported (Use the latest version)
* 1.21 (Minecraft Forge 51.x)

### Deprecated support
* 1.19.x (Minecraft Forge 44.x, 45.x) (Use version [1.1.2.1](https://github.com/ojaha065/PiShockForMC/releases/download/1.20-1.1.2.1/pishockmc-1.20-1.1.2.1.jar))
* 1.20.x (Minecraft Forge 46.x, 47.x, 48.x, 49.x) (Use version [1.1.2.1](https://github.com/ojaha065/PiShockForMC/releases/download/1.20-1.1.2.1/pishockmc-1.20-1.1.2.1.jar))

### Untested (might or might not work)
* Minecraft Forge 50.x
* Any **newer** Minecraft or Minecraft Forge version not mentioned

## Needed hardware
* [A PiShock](https://pishock.com) device
* A shocker — needs to be paired to the PiShock
  * See [PiShock website](https://pishock.com) for help on pairing your shocker.
  * Multiple shockers are not yet supported but might be in a future release.

## Setup instructions (tl;dr — I've done this kind of thing before, and I know what I'm doing)
1. Download the latest release and drop the jar into Forge mods folder.
2. Launch the game once and then see the _Mod configuration_ section below.
3. Launch the game again. If everything is set currently, the shocker will vibrate once for one second during the Minecraft startup process.

## Setup instructions (longer version)
1. Have a working Minecraft and Forge installation. See [How to install Forge](https://www.wikihow.com/Install-Minecraft-Forge) if you're unsure.
   * Make sure you've launched the game at least once with Forge installed so the required folder structure is generated.
2. Download the latest release jar from [here](https://github.com/ojaha065/PiShockForMC/releases).
3. Drop the downloaded .jar file into `mods` folder inside Minecraft game directory. See the [Mojang documentation](https://help.minecraft.net/hc/en-us/articles/4409159214605) if you're unsure how to find the correct game directory.
4. Run the game once so a configuration file for the mod gets generated.
5. See the _Mod configuration_ section below. The configuration file can be found inside `config` folder inside Minecraft game directory.
6. Launch the game again. If everything is set correctly, the shocker will vibrate once for one second during the Minecraft startup process.

## Modpacks and compatability with other mods
The mod is licensed under a MIT license. Feel free to include it in any modpack, no permission or attribution required.

This mod should be compatible with almost everything.
If you're using other mods that alter the player health (e.g., changes the maxium health),
please note that the shock intensity scaling might not work as expected.
However,
even in that case the player will never be shocked with a higher intensity than the configured `intensity_range` allows.

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

`username`, `apikey` and `code` are all mandatory, and you can get all of them from [pishock.com](https://pishock.com).
It's also important to set the desired intensity level (`intensity_range`).
The default value is `MINIMAL`,
but for me personally `NORMAL`or `INTENSE` have the best balance
between feeling kinda nasty but not being too overwhelming.
But it's all very dependent on each person's pain tolerance and location of the shocker,
so you need to experiment what works for each person.
Also,
please be aware that for most people getting shocked at the same spot multiple times drastically heightens the sensation
and each consecutive shock will be more painful.

### Punishment for death
Setting `punishment_for_death` --> `enabled` option to `true` will send a shock with the configured intensity and duration when the player dies in-game.

> [!IMPORTANT]
> The mod uses duration of 600 milliseconds for shocks.
Using milliseconds instead of seconds is currently undocumented feature in PiShock API,
and it seems that `Max Duration` setting for share codes does not work correctly with it. (Not even in the new V3 UI)
When creating a share code,
the `Max Duration` value needs to be set to at least 6 (seconds)
or the value of `punishment_for_death` --> `duration` if it's higher than 6.
The mod will warn about a misconfiguration if this is not set correctly.

### Shock intensity calculation
The intensity of the shock is calculated using the following formula:
[The amount of damage taken] * [intensity range multiplayer (see the table below)].
The maximum amount of damage taken into account is 20 (10 hearts).
This won't be exceeded even if other mods raise the player's maximum health.
If [absorption](https://minecraft.fandom.com/wiki/Absorption) cancels the incoming damage, no shock will be triggered.

After a shock is triggered,
there's a cooldown of 25 ticks (just bit over a second) before another shock can be triggered.
The damage taken during that time will be backlogged,
and another shock with the intensity calculated using the accumulated damage will be triggered after the cooldown ends.


### Intensity ranges and multipliers
| intensity_range | Maxium intensity % | Damage Multiplier |
|-----------------|--------------------|-------------------|
| MINIMAL         | 20%                | 1                 |
| NORMAL          | 40%                | 2                 |
| INTENSE         | 60%                | 3                 |
| HARDCORE        | 80%                | 4                 |
| ULTRA_HARDCORE  | 100%               | 5                 |

## Common issues and FAQ
### Sometimes the shocker isn't activated when it should
From a personal experience,
I've found that sometimes the PiShock device itself and/or the shocker are a little finicky
and might sometimes (rarely) skip some shocks if multiple ones are sent in a short period of time.

If you're experiencing this,
the first step is to check the logs at [pishock.com](https://pishock.com)
and see if a correct amount API events from `PiShock integration for Minecraft` is displayed.
If so, the problem is at PiShock's end, and the mod really cannot do anything about it.

### The game is telling me that *There seems to be some kind of misconfiguration or issue with PiShock configuration*.
This usually means that the provided share code has too restrictive limitations (too low Max duration or Max intensity). 
Please take a look in Minecraft logs and search for `pishockmc`.
The actual issue and some helpful advice should be printed there.

### Minecraft fails to start after installing the mod
Double-check your Minecraft and Forge versions and the beginning of this readme
and make sure you're using a compatible version of the mod.

### I'm still having issues
Please open [an issue](https://github.com/ojaha065/PiShockForMC/issues) here on GitHub.

## TODO (PRs welcome)
* **Support for multiple shockers**
  * I currently only own one, so testing and debugging would be kinda hard for me.
* In-game configuration GUI
* **More configuration options**
  * Configurable shock duration
  * Configurable cooldown time

## The boring stuff

Although nothing about this repository is inherently age-gated,
the PiShock device and themes around it are targeted towards mature audiences only.
Proceed at your own discretion.

PiShock or the authors of this mod are not responsible for any harm
caused by misuse of the shock collar sold along with the PiShock device.
We do not recommend putting any kind of electrical device near the heart or use if you have a heart condition.
Shock collars are not meant for use on humans and can cause serious injury, including cardiac events.
