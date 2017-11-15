package co.bugg.animatedcrosshair.command;

import co.bugg.animatedcrosshair.gui.ConfigGui;
import co.bugg.animatedcrosshair.TickDelay;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to open the ConfigGui class GUI
 */
public class CommandCrosshair implements ICommand {
    @Override
    public String getCommandName() {
        return "crosshair";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/crosshair";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
       new TickDelay(() -> Minecraft.getMinecraft().displayGuiScreen(new ConfigGui()), 1);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return 0;
    }
}
