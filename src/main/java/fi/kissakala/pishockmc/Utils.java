package fi.kissakala.pishockmc;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class Utils {
	/**
	 * Clamp integer between two integers
	 * @param integer The input
	 * @param min Minimum value (inclusive)
	 * @param max Maximum value (inclusive)
	 * @return The input or given minimum/maxium value if the input is below/above that
	 */
	public static int clamp(final int integer, final int min, final int max) {
		return Math.max(Math.min(integer, max), min);
	}

	/**
	 * Simple helper for logging. Prepends "[PiShock]" to String
	 * @param message The input
	 * @return "[PiShock] " + the given input
	 */
	public static String log(@Nonnull final String message) {
		return "[PiShock] " + message;
	}

	/**
	 * Adds a message to the chat
	 * @param message The message
	 */
	public static void logToChat(@Nonnull final String message) {
		Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
	}
}