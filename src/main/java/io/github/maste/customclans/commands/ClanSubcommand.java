package io.github.maste.customclans.commands;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface ClanSubcommand {

    String name();

    String permission();

    boolean playerOnly();

    void execute(CommandSender sender, String[] args);

    default List<String> tabComplete(CommandSender sender, String[] args) {
        return List.of();
    }
}
