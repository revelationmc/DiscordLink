package net.revelationmc.discordlink.listener;

import net.revelationmc.discordlink.DiscordLinkPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

public class ConnectionListener implements Listener {
    private final DiscordLinkPlugin context;

    public ConnectionListener(DiscordLinkPlugin context) {
        this.context = context;
    }

    @EventHandler
    public void on(AsyncPlayerPreLoginEvent event) {
        final UUID uuid = event.getUniqueId();

        if (this.context.getServer().getWhitelistedPlayers().stream().anyMatch(player -> player.getUniqueId().equals(uuid))) {
            return;
        }

        final String code = this.context.getVerificationCode(uuid).join();

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        event.setKickMessage(ChatColor.RED + "Your verification code: " + code + "\nThis code is only valid for 5 minutes.");
    }
}
