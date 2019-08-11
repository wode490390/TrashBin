package cn.wode490390.nukkit.trashbin;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.plugin.Plugin;

public class TrashBinCommand extends Command implements PluginIdentifiableCommand {

    private final Plugin plugin;

    TrashBinCommand(Plugin plugin) {
        super("trashbin", "Opens the trash bin", "/trashbin", new String[]{"tb"});
        this.setPermission("trashbin.command");
        this.getCommandParameters().clear();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!this.plugin.isEnabled() || !this.testPermission(sender)) {
            return false;
        }
        if (sender.isPlayer()) {
            TrashBin.open((Player) sender);
        } else {
            sender.sendMessage(new TranslationContainer("%commands.generic.ingame"));
        }
        return true;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }
}
