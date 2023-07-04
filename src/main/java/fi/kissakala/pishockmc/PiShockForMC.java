package fi.kissakala.pishockmc;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import javax.annotation.Nullable;

@Mod("pishockmc")
public class PiShockForMC {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static PiShockAPI API = null;

    // Code smell but having these as statics here keeps the things simple.
    // Since this is a single-player client-only mod, this shouldn't cause any issues.
    @Nullable private static Float previousTickHealth = null;
    @Nullable private static Integer cooldownTimer = null;
    @Nullable private static Integer gracePeriodTimer = null;
    private static float damageBacklog = 0f;
    private static PUNISHMENT_FOR_DEATH_STATE punishmentForDeathState = PUNISHMENT_FOR_DEATH_STATE.INACTIVE;

    public PiShockForMC() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.INSTANCE);

            final var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
            modEventBus.addListener(this::onClientSetup);
            modEventBus.addListener(KeyMappingsHandler::onRegisterKeyMappings);

            MinecraftForge.EVENT_BUS.addListener(this::onClientTick);
        });
    }

    private void onClientSetup(final FMLClientSetupEvent _event) {
        API = new PiShockAPI();
        LOGGER.info(Utils.log("PiShock for Minecraft initialized"));
    }

    // As far as I know, there isn't a client-side event to easily watch player health in Forge.
    // So need to do this manually.
    private void onClientTick(final TickEvent.ClientTickEvent event) {
        // Skip tick start event
        if (TickEvent.Phase.START.equals(event.phase)) {
            return;
        }

        KeyMappingsHandler.onClientTick(API);

        // Check for a waiting punishment for death early in the flow
        // so the player cannot escape it by pausing or exiting the world once it's activated
        if (PUNISHMENT_FOR_DEATH_STATE.WAITING.equals(punishmentForDeathState)) {
            if (cooldownTimer == null || --cooldownTimer <= 0) {
                cooldownTimer = null;
                punishmentForDeathState = PUNISHMENT_FOR_DEATH_STATE.DONE;
                API.sendRequest(
                    Config.mode.get(),
                    (int) (20f * Config.intensity.get().getMultiplier()),
                    5
                );
            }

            return;
        }

        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            // If player is null it most likely means that we're not in-game right now.
            // Just reset everything and do nothing else
            resetState();
            return;
        }

        // Skip if the game is paused
        if (Minecraft.getInstance().isPaused()) {
            return;
        }

        // Skip if the player is in creative or spectator mode
        // Player usually cannot take damage while in those game modes,
        // and even if they somehow do, we don't want to shock them for it
        if (player.isCreative() || player.isSpectator()) {
            resetState();
            return;
        }

        final float currentHealth = player.getHealth();

        if (gracePeriodTimer != null) {
            // If the grace period is still active, just update the current health and short circuit
            // Also, do not advance the grace period while the player is dead
            //  --> Fixes an edge case issue where punishment for death would be unfairly activated if the player joins a world where they are currently dead
            if (currentHealth <= 0 || --gracePeriodTimer > 0) {
                previousTickHealth = currentHealth;
                return;
            }

            // FIXME: Messy
            if (API.getConnectionState().equals(PiShockAPI.CONNECTION_STATE.OK)) {
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal("PiShock enabled. You'll be punished for any damage you take..."));
                gracePeriodTimer = null;
            } else if (API.getConnectionState().equals(PiShockAPI.CONNECTION_STATE.CONNECTED_WITH_WARNING)) {
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal("PiShock enabled. You'll be punished for any damage you take..."));
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal("[WARNING] There seems to be some kind of misconfiguration or issue with PiShock configuration. Please check the Minecraft Output logs and mod documentation for more details."));
                gracePeriodTimer = null;
            } else {
                gracePeriodTimer = 20 * 10;
                API.connect().thenAccept(connectionState -> {
                    if (connectionState.equals(PiShockAPI.CONNECTION_STATE.OK)) {
                        Minecraft.getInstance().gui.getChat().addMessage(Component.literal("PiShock enabled. You'll be punished for any damage you take..."));
                    } else if (connectionState.equals(PiShockAPI.CONNECTION_STATE.CONNECTED_WITH_WARNING)) {
                        Minecraft.getInstance().gui.getChat().addMessage(Component.literal("PiShock enabled. You'll be punished for any damage you take..."));
                        Minecraft.getInstance().gui.getChat().addMessage(Component.literal("[WARNING] There seems to be some kind of misconfiguration or issue with PiShock configuration. Please check the Minecraft Output logs and mod documentation for more details."));
                    } else {
                        Minecraft.getInstance().gui.getChat().addMessage(Component.literal("It seems that Minecraft cannot connect to your PiShock device. Please check the Minecraft Output logs and mod documentation for more help."));
                    }

                    gracePeriodTimer = null;
                });
            }
        }

        // If we don't know the health from the previous tick,
        // it most likely means that the player just joined the world.
        // Just start the grace period (to avoid unfairly shocking the player immediately after the world has been loaded) for 100 ticks and skip the rest
        if (previousTickHealth == null) {
            previousTickHealth = currentHealth;
            gracePeriodTimer = 100;
            return;
        }

        // The main business logic
        // First checks if the player is dead and if the punishment for death is enabled...
        if (currentHealth <= 0 && Config.punishmentForDeath.get()) {
            if (PUNISHMENT_FOR_DEATH_STATE.DONE.equals(punishmentForDeathState)) {
                LOGGER.trace(Utils.log("Player is dead but has been already punished for this death --> Do nothing"));
                return;
            }

            LOGGER.debug(Utils.log("Player has died and the punishment for death is enabled --> Get ready..."));
            punishmentForDeathState = PUNISHMENT_FOR_DEATH_STATE.WAITING;
            damageBacklog = 0f; // Cancel any other pending damage
        }

        // ..If not, then check if the player has taken any damage since the last tick...
        else if (currentHealth < previousTickHealth) {
            final float lostHealth = previousTickHealth - currentHealth;
            LOGGER.debug(Utils.log("Player has taken %s damage since the last tick".formatted(lostHealth)));
            punishmentForDeathState = PUNISHMENT_FOR_DEATH_STATE.INACTIVE;

            if (currentHealth < player.getMaxHealth()) { // Ignore this "damage" if the player somehow already is at their maximum health
                if (cooldownTimer == null) {
                    LOGGER.debug(Utils.log("No active cooldown --> Will trigger PiShock device now and start a cooldown"));
                    cooldownTimer = 25;
                    API.sendRequest(
                        Config.mode.get(),
                        (int) Math.min(
                            Math.ceil(lostHealth * Config.intensity.get().getMultiplier()),
                            20f * Config.intensity.get().getMultiplier()
                        ),
                        600 // TODO: Make configurable
                    );
                } else {
                    LOGGER.debug(Utils.log("A cooldown is active --> The damage will be backlogged"));
                    damageBacklog += lostHealth;
                }
            }
        }

        // ...If not, only then handle the possibly active cooldown and damage backlog
        else if (cooldownTimer != null && --cooldownTimer <= 0) {
            if (damageBacklog > 0f && currentHealth < player.getMaxHealth()) {
                LOGGER.debug(Utils.log("A cooldown has ended and the player has %s backlogged damage --> will trigger PiShock device now and start a new cooldown".formatted(damageBacklog)));
                cooldownTimer = 25;
                API.sendRequest(
                    Config.mode.get(),
                    (int) Math.min(
                        Math.ceil(damageBacklog * Config.intensity.get().getMultiplier()),
                        20f * Config.intensity.get().getMultiplier()
                    ),
                    600 // TODO: Make configurable
                );
            } else {
                LOGGER.trace(Utils.log("A cooldown has ended but the player has no backlogged damage or is already fully healed --> Do nothing"));
                cooldownTimer = null;
            }

            damageBacklog = 0f;
        }

        previousTickHealth = currentHealth;
    }

    private static void resetState() {
        previousTickHealth = null;
        cooldownTimer = null;
        gracePeriodTimer = null;
        damageBacklog = 0f;
        punishmentForDeathState = PUNISHMENT_FOR_DEATH_STATE.INACTIVE;
    }

    private enum PUNISHMENT_FOR_DEATH_STATE {
        INACTIVE,
        WAITING,
        DONE
    }
}