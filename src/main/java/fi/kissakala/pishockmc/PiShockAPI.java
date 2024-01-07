package fi.kissakala.pishockmc;

import com.google.gson.Gson;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Methods for accessing PiShock API
 * <a href="https://pishock.com">API documentation</a>
 */
public class PiShockAPI implements Closeable {
    private static final Logger LOGGER = LogManager.getLogger("pishockmc");
    private static final Gson GSON = new Gson();

    private final URI API_URL;
    private final ExecutorService worker;
    private CONNECTION_STATE connectionState;

    public PiShockAPI() {
        try {
            this.API_URL = new URI("https://do.pishock.com/api/");
            this.worker = Executors.newSingleThreadExecutor();

            connect().get();
        } catch (final URISyntaxException | ExecutionException | InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Call PiShock API
     * @param operation One of {@link OP_CODE}
     * @param intensity An integer between 1 - 100
     * @param duration An integer between 1 - 15 (seconds) or 100 - 15_000 (milliseconds)
     * TODO: Return Future
     */
    public void sendRequest(final OP_CODE operation, final int intensity, final int duration) {
        if (!hasCredentials()) {
            LOGGER.error("PiShock API credentials are not configured --> No request will be send");
            return;
        }

        worker.execute(() -> {
            try {
                final Map<String, Object> json = Map.ofEntries(
                    Map.entry("Username", Config.username.get()),
                    Map.entry("Apikey", Config.apikey.get()),
                    Map.entry("Code", Config.code.get()),
                    Map.entry("Name", "PiShock integration for Minecraft"),
                    Map.entry("Op", operation.getValue()),
                    Map.entry("Duration", duration >= 100 ? Utils.clamp(duration, 100, 15_000) : Utils.clamp(duration, 1, 15)),
                    Map.entry("Intensity", Utils.clamp(intensity, 1, 100))
                );

                final HttpURLConnection con = (HttpURLConnection) API_URL.resolve("./apioperate").toURL().openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "text/plain");
                con.setRequestProperty("User-Agent", "PiShock for Minecraft");
                con.setDoOutput(true);

                try(final OutputStream os = con.getOutputStream()) {
                    os.write(GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
                }

                final String responseString;
                try(final InputStream is = con.getInputStream()) {
                    responseString = new String(is.readAllBytes());
                }

                if (!StringUtils.equalsAny(responseString, "Operation Attempted.", "Operation Succeeded.")) {
                    throw new RuntimeException("PiShock API request failed: " + responseString);
                }
            } catch (final Exception error) {
                LOGGER.error("Error accessing PiShock API", error);
            }
        });
    }

    public CompletableFuture<CONNECTION_STATE> connect() {
        if (!hasCredentials()) {
            LOGGER.warn(Utils.log("PiShock API credentials are not configured"));

            this.connectionState = CONNECTION_STATE.NOT_CONNECTED;
            return CompletableFuture.completedFuture(this.connectionState);
        }

        return getShockerInfo().orTimeout(8, TimeUnit.SECONDS).handle((shockerInfo, error) -> {
            if (error != null) {
                LOGGER.warn(Utils.log("Could not get Shocker Info from PiShock API"), error);

                this.connectionState = CONNECTION_STATE.NOT_CONNECTED;
                return this.connectionState;
            }

            final float requiredMaxDuration = Math.max(
                6,
                Config.punishmentForDeathEnabled.get()
                    ? Config.punishmentForDeathDuration.get()
                    : 0
            );
            if (shockerInfo.maxDuration < requiredMaxDuration) {
                LOGGER.warn(
                    Utils.log("The Max Duration setting of PiShock shocker \"{}\" is too low ({}). It needs to be at least {}. See the mod documentation for more details."),
                    shockerInfo.name,
                    shockerInfo.maxDuration,
                    requiredMaxDuration
                );

                this.connectionState = CONNECTION_STATE.CONNECTED_WITH_WARNING;
                return this.connectionState;
            }

            final float requiredMaxIntensity = Math.max(
                Config.intensity.get().getMultiplier() * 20,
                Config.punishmentForDeathEnabled.get()
                    ? Config.punishmentForDeathIntensity.get()
                    : 0
            );
            if (shockerInfo.maxIntensity < requiredMaxIntensity) {
                LOGGER.warn(
                    Utils.log("The Max Intensity setting of PiShock shocker \"{}\" is too low ({}). It either needs to be set it at least {} or you need to lower the intensity settings in the mod configuration. See the mod documentation for more details."),
                    shockerInfo.name,
                    shockerInfo.maxIntensity,
                    requiredMaxIntensity
                );

                this.connectionState = CONNECTION_STATE.CONNECTED_WITH_WARNING;
                return this.connectionState;
            }

            LOGGER.info(Utils.log("Shocker \"{}\" found! Sending vibrate..."));
            sendRequest(OP_CODE.Vibrate, 25, 1);

            this.connectionState = CONNECTION_STATE.OK;
            return this.connectionState;
        });
    }

    @Override
    public void close() {
        worker.shutdown();
    }

    private CompletableFuture<GetShockerInfoResponse> getShockerInfo() {
        if (!hasCredentials()) {
            return CompletableFuture.failedFuture(new RuntimeException("PiShock API credentials are not configured"));
        }

        final CompletableFuture<GetShockerInfoResponse> completableFuture = new CompletableFuture<>();
        worker.execute(() -> {
            try {
                final Map<String, Object> json = Map.ofEntries(
                    Map.entry("Username", Config.username.get()),
                    Map.entry("Apikey", Config.apikey.get()),
                    Map.entry("Code", Config.code.get())
                );

                final HttpURLConnection con = (HttpURLConnection) API_URL.resolve("./GetShockerInfo").toURL().openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestProperty("User-Agent", "PiShock for Minecraft");
                con.setDoOutput(true);

                try(final OutputStream os = con.getOutputStream()) {
                    os.write(GSON.toJson(json).getBytes(StandardCharsets.UTF_8));
                }

                if (con.getResponseCode() != 200) {
                    throw new RuntimeException("HTTP " + con.getResponseCode());
                }

                try(final InputStream is = con.getInputStream()) {
                    completableFuture.complete(GSON.fromJson(new String(is.readAllBytes()), GetShockerInfoResponse.class));
                }
            } catch (final Exception error) {
                completableFuture.completeExceptionally(error);
            }
        });

        return completableFuture;
    }

    public CONNECTION_STATE getConnectionState() {
        return this.connectionState;
    }

    public enum OP_CODE {
        Shock(0),
        Vibrate(1),
        Beep(2);

        private final int value;
        OP_CODE(final int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public enum CONNECTION_STATE {
        OK,
        CONNECTED_WITH_WARNING,
        NOT_CONNECTED
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasCredentials() {
        return Stream.of(Config.username, Config.apikey, Config.code)
            .map(ForgeConfigSpec.ConfigValue::get)
            .noneMatch(String::isBlank);
    }

    private record GetShockerInfoResponse(
        Integer clientId,
        Integer id,
        String name,
        Boolean paused,
        Integer maxIntensity,
        Integer maxDuration
    ){}
}