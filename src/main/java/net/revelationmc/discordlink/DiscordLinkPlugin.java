package net.revelationmc.discordlink;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.revelationmc.discordlink.commands.LinkDiscordCommand;
import net.revelationmc.discordlink.listener.ConnectionListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DiscordLinkPlugin extends JavaPlugin {
    private static final String DISCORD_LINK_API = "https://api.savagedev.net/discordlink/code";

    private final Cache<UUID, String> codeCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5L, TimeUnit.MINUTES)
            .maximumSize(100)
            .build();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        if (this.getConfig().getBoolean("do-kick")) {
            this.getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        }

        final PluginCommand command = this.getCommand("linkdiscord");
        if (command != null) {
            command.setExecutor(new LinkDiscordCommand(this));
        }
    }

    public CompletableFuture<String> getVerificationCode(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return this.codeCache.get(uuid, () -> this.getApiVerificationCode(uuid));
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private String getApiVerificationCode(UUID uuid) {
        final HttpRequest request = HttpRequest.newBuilder(URI.create(DISCORD_LINK_API))
                .header("API-Key", this.getConfig().getString("api-key"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"id\": \"" + uuid + "\"}"))
                .build();
        try {
            final HttpResponse<String> response = HttpClient.newBuilder().build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            final JsonObject object = JsonParser.parseString(response.body().trim()).getAsJsonObject();
            if (object.get("success").getAsBoolean()) {
                return object.get("code").getAsString();
            }
            this.getLogger().log(Level.WARNING, "Unable to get verification code! Reason: " + object.get("error").getAsString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "null";
    }
}
