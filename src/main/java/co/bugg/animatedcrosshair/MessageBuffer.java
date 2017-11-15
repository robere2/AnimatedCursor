package co.bugg.animatedcrosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;

/**
 * Buffer for chat messages sent to the
 * client. Will send any messages in the
 * buffer to the player as soon as possible.
 */
public class MessageBuffer extends Thread {
    private ArrayList<IChatComponent> buffer;

    public MessageBuffer() {
        buffer = new ArrayList<>();
    }

    public void add(IChatComponent component) {
        buffer.add(component);
    }

    public IChatComponent peek() {
        return buffer.get(0);
    }

    public IChatComponent pull() {
        IChatComponent component = peek();
        buffer.remove(0);

        return component;
    }

    public int size() {
        return buffer.size();
    }

    public IChatComponent format(String message) {
        IChatComponent component = new ChatComponentText("[AnimatedCrosshair] ");
        component.setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_AQUA));

        component.appendSibling(new ChatComponentText(message).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RESET)));

        return component;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

            try {
                if(size() > 0 && player != null) {
                    player.addChatMessage(pull());
                }

                Thread.sleep(100);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


}
