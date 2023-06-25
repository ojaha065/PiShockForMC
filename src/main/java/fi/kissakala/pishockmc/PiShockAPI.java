package fi.kissakala.pishockmc;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Methods for accessing PiShock API
 * <a href="https://pishock.com">API documentation</a>
 */
public class PiShockAPI implements Closeable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    private final URL API_URL;
    private final ExecutorService worker;

    public PiShockAPI() {
        try {
            this.API_URL = new URL("https://do.pishock.com/api/apioperate/");
        } catch (final MalformedURLException malformedURLException) {
            throw new RuntimeException(malformedURLException);
        }

        this.worker = Executors.newSingleThreadExecutor();
        sendRequest(OP_CODE.Vibrate, 25, 1);
    }

    /**
     * Call PiShock API
     * @param operation One of {@link OP_CODE}
     * @param intensity An integer between 1 - 100
     * @param duration An integer between 1 - 15 (seconds) or 100 - 15_000 (milliseconds)
     */
    public void sendRequest(final OP_CODE operation, final int intensity, final int duration) {
        if (Stream.of(Config.username, Config.apikey, Config.code).map(ForgeConfigSpec.ConfigValue::get).anyMatch(String::isBlank)) {
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

                final HttpURLConnection con = (HttpURLConnection) API_URL.openConnection();
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

                if (!"Operation Succeeded.".equals(responseString)) {
                    throw new RuntimeException("PiShock API request failed: " + responseString);
                }
            } catch (final Exception error) {
                LOGGER.error("Error accessing PiShock API", error);
            }
        });
    }

    @Override
    public void close() {
        worker.shutdown();
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
}