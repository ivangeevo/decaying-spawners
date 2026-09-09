package org.btwr.decaying_spawners.config;

import com.google.gson.*;
import org.btwr.decaying_spawners.Constants;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class ModConfig {

    /** How many mobs a spawner spawns before decaying. -1 = disabled **/
    public static int maxMobSpawnCount = 64;

    /** How many ticks a spawner can be active before decaying. -1 = disabled **/
    public static int maxSpawnerTicks = -1;

    /** Mob spawner types which do not decay **/
    public static Set<String> excludedSpawners = new HashSet<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;

    public static void init(Path configDir) {
        configPath = configDir.resolve("btwr/decaying_spawners/decaying_spawners.json");
        load();
    }

    public static void load() {
        Constants.LOG.info("Loading config from {}", configPath);
        if (!Files.exists(configPath)) {
            Constants.LOG.info("Config missing, creating default config");
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(configPath)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("maxMobSpawnCount")) maxMobSpawnCount = json.get("maxMobSpawnCount").getAsInt();
            if (json.has("maxSpawnerTicks")) maxSpawnerTicks = json.get("maxSpawnerTicks").getAsInt();
            if (json.has("excludedSpawners")) {
                excludedSpawners = new HashSet<>();
                json.get("excludedSpawners").getAsJsonArray().forEach(e -> excludedSpawners.add(e.getAsString()));
            }
        } catch (IOException e) {
            Constants.LOG.warn(Constants.MOD_NAME + "Failed to load config: {}", e.getMessage());
        }

        Constants.LOG.info("Loaded maxMobSpawnCount={}", maxMobSpawnCount);
    }

    public static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("maxMobSpawnCount", maxMobSpawnCount);
        json.addProperty("maxSpawnerTicks", maxSpawnerTicks);
        JsonArray excluded = new JsonArray();
        excludedSpawners.forEach(excluded::add);
        json.add("excludedSpawners", excluded);
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            Constants.LOG.warn(Constants.MOD_NAME + "Failed to save config: {}", e.getMessage());
        }
    }
}
