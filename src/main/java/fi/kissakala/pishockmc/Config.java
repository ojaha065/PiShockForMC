package fi.kissakala.pishockmc;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
	public static ForgeConfigSpec INSTANCE;

	public static ForgeConfigSpec.ConfigValue<String> username;
	public static ForgeConfigSpec.ConfigValue<String> apikey;
	public static ForgeConfigSpec.ConfigValue<String> code;

	public static ForgeConfigSpec.EnumValue<INTENSITY_SETTING_VALUE> intensity;
	public static ForgeConfigSpec.EnumValue<PiShockAPI.OP_CODE> mode;

	public static ForgeConfigSpec.BooleanValue punishmentForDeathEnabled;
	public static ForgeConfigSpec.IntValue punishmentForDeathIntensity;
	public static ForgeConfigSpec.IntValue punishmentForDeathDuration;

	static {
		INSTANCE = build(new ForgeConfigSpec.Builder()).build();
	}

	private static ForgeConfigSpec.Builder build(final ForgeConfigSpec.Builder builder) {
		username = builder
			.comment("Username you use to log into PiShock.com. Can be found in the Account section of the website.")
			.define("pishock.username", "");

		apikey = builder
			.comment("API Key generated on PiShock.com Can be found in the Account section of the website.")
			.define("pishock.apikey", "");

		code = builder
			.comment("Sharecode generated on PiShock.com. Limitations can be set when generating the code.")
			.define("pishock.code", "");

		mode = builder
			.comment("\"Shock\" is the intended mode for this mod. Vibrate and Beep are more suitable for testing and debugging.")
			.defineEnum("mode", PiShockAPI.OP_CODE.Shock, PiShockAPI.OP_CODE.values());

		intensity = builder
			.comment("Set the shock/vibration/beep intensity range", "The maxium intensities from lowest to highest are: 20, 40, 60, 80 and 100")
			.defineEnum("intensity_range", INTENSITY_SETTING_VALUE.MINIMAL, INTENSITY_SETTING_VALUE.values());

		punishmentForDeathEnabled = builder
			.comment("If enabled, sends a shock with the configured intensity and duration when the player dies.")
			.define("punishment_for_death.enabled", false);

		punishmentForDeathIntensity = builder
			.comment("Intensity of the punishment shock. Must be a integer between 1 and 100.")
			.defineInRange("punishment_for_death.intensity", 50, 1, 100);

		punishmentForDeathDuration = builder
			.comment("Duration of the punishment shock in seconds. Must be a integer between 1 and 15.")
			.defineInRange("punishment_for_death.duration", 5, 1, 15);

		return builder;
	}

	public enum INTENSITY_SETTING_VALUE {
		MINIMAL(1f), // 1 - 20
		NORMAL(2f), // 21 - 40
		INTENSE(3f), // 41 - 60
		HARDCORE(4f), // 61 - 80
		ULTRA_HARDCORE(5f); // 81 - 100

		private final float multiplier;
		INTENSITY_SETTING_VALUE(final float multiplier) {
			this.multiplier = multiplier;
		}

		public float getMultiplier() {
			return multiplier;
		}
	}
}