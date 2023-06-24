package fi.kissakala.pishockmc;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyMappingsHandler {
	private static final KeyMapping VIBRATE = new KeyMapping(
		"pishockmc.vibrate",
		KeyConflictContext.UNIVERSAL,
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_UNKNOWN,
		KeyMapping.CATEGORY_MISC
	);

	public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
		event.register(VIBRATE);
	}

	public static void onClientTick(final PiShockAPI API) {
		while (VIBRATE.consumeClick()) {
			API.sendRequest(PiShockAPI.OP_CODE.Vibrate, 25, 1);
		}
	}
}