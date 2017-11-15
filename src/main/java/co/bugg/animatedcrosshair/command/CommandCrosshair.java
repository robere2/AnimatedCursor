package co.bugg.animatedcrosshair.command;

import co.bugg.animatedcrosshair.TickDelay;
import co.bugg.animatedcrosshair.gui.ConfigGui;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to open the ConfigGui class GUI
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommandCrosshair implements ICommand {
    @Override
    public String getName() {
        return "crosshair";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/crosshair";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
       new TickDelay(() -> Minecraft.getMinecraft().displayGuiScreen(new ConfigGui()), 1);
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        return new ArrayList<>();
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
