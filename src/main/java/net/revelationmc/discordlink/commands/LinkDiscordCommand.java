package net.revelationmc.discordlink.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.revelationmc.discordlink.DiscordLinkPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkDiscordCommand implements CommandExecutor {
    private final DiscordLinkPlugin context;

    public LinkDiscordCommand(DiscordLinkPlugin context) {
        this.context = context;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            this.context.getVerificationCode(((Player) sender).getUniqueId())
                    .thenAccept(code -> {
                        final BaseComponent[] message = new ComponentBuilder("Your verification code is: " + code)
                                .color(ChatColor.GREEN)
                                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, code))
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.YELLOW + "Click to copy.")))
                                .create();
                        sender.spigot().sendMessage(message);
                        sender.sendMessage(ChatColor.GREEN + "This code is valid for 5 minutes.");
                    });
        }
        return true;
    }
}
