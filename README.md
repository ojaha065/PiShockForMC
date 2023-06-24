# PiShock Shock Collar Integration for Minecraft
This simple Forge mod allows players to connect their [PiShock](https://pishock.com) device to the game for some added stakes. Whenever the player takes damage, they will get a corresponding shock, the intensity of which can be configured and will scale up based on the amount of damage taken.

## Supported Minecraft/Forge versions
1.19.x (Forge 45.x)

## Needed hardware
* A PiShock
* A shocker; needs to paired to the PiShock (multiple shockers might be supported in a future release)

## Setup instructions (tl;dr - I've done this before and I know what I'm doing)
1. Download the latest release and drop the jar into Forge `mods` folder.
2. Launch the game once and then see the Mod configuration section below.
3. Launch the game again. If everything is set corrently the shocker will vibrate for one second during Minecraft startup process.

## Setup instructions
0. Have a working Minecraft and Forge installation. See [How to install Forge](https://www.wikihow.com/Install-Minecraft-Forge) if you're unsure.
   * Make sure you've launched the game at least once with Forge installed so required folder structure is generated.
1. Download the latest release jar from [here](https://github.com/ojaha065/PiShockForMC/releases).
2. Drop the downloaded .jar file into `mods` folder inside Minecraft game directory. See [Where are Minecraft files stored?](https://help.minecraft.net/hc/en-us/articles/4409159214605-Managing-Data-and-Game-Storage-in-Minecraft-Java-Edition-#h_01FGA90Z06DE00GT8E81SWX9SE) if you're unsure how to find the correct game directory.
3. Run the game once so a configuration file gets generated.
4. See the Mod configuration section below. The config file can be found inside `config` folder inside Minecraft game directory.
5. Launch the game again. If everything is set corrently the shocker will vibrate for one second during Minecraft startup process.

## Mod configuration
The mod configuration file is named `pishockmc-client.toml` and it can be edited with any text editor. In-game settings GUI might be added in a later release to make configuring easier. The configuration file will look like this:

```
#"Shock" is the intended mode for this mod. Vibrate and Beep are more suitable for testing and debugging.
#Allowed Values: Shock, Vibrate, Beep
mode = "Shock"
#Set the shock/vibration/beep intensity range
#Ranges from lowest to highest are: 1 - 20, 21 - 40, 41-60, 61 - 80, 80 - 100
#Allowed Values: MINIMAL, NORMAL, INTENSE, HARDCORE, ULTRA_HARDCORE
intensity_range = "NORMAL"
#If enabled, sends 5 second shock/vibrate/beep at the maximum* intensity when the player dies
#(*the maximum is based on the configured intensity range)
death_punishment = false

[pishock]
	#Username you use to log into PiShock.com. Can be found in the Account section of the website.
	username = ""
	#API Key generated on PiShock.com Can be found in the Account section of the website.
	apikey = ""
	#Sharecode generated on PiShock.com. Limitations can be set when generating the code.
	code = ""


```

`username`, `apikey` and `code` are all mandatory and you can get all of them from https://pishock.com. It's also important to set the desired intensity level (`intensity_range`). The default value is `MINIMAL`, but personally I feel that `NORMAL` has the best balance between feeling kinda nasty but not being too overwhelming. But it's all very dependant on each persons pain tolerance and location of the shocker so feel free to experiment.

### :warning: Important notice
The mod uses duration of 600 milliseconds for most shocks. Using milliseconds insteads of seconds is currently undocumented feature in PiShock API and it seems that `Max Duration` setting for share codes does not work corectly with it. When creating a share code you need to set `Max Duration` to value of at least 6 (seconds).

### Intensity ranges
| intensity_range  | Shock intensity % range |
| ---------------- | ----------------------- |
| MINIMAL          | 1% - 20%                |
| NORMAL           | 21% - 40%               |
| INTENSE          | 41% - 60%               |
| HARDCORE         | 61% - 80%               |
| ULTRA_HARDCORE   | 81% - 100%              |

## The boring stuff
Authors of this mod are not responsible for any injuries caused by use of the shock collar. It's not recommended to put any kind of electrical device near the heart or use any kind of shock collar if you have a heart condition. Shock collars are not meant for use on humans and can cause serious injury, even cardiac events. We kindly urge you to prioritize safety, understand your personal and others limitations, and exercise caution at all times.
