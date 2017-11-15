package co.bugg.animatedcrosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.*;
import net.minecraft.util.text.*;

import java.util.ArrayList;

/**
 * Buffer for chat messages sent to the
 * client. Will send any messages in the
 * buffer to the player as soon as possible.
 */
public class MessageBuffer extends Thread {
    private ArrayList<ITextComponent> buffer;

    public MessageBuffer() {
        buffer = new ArrayList<>();
    }

    public void add(ITextComponent component) {
        buffer.add(component);
    }

    public ITextComponent peek() {
        return buffer.get(0);
    }

    public ITextComponent pull() {
        ITextComponent component = peek();
        buffer.remove(0);

        return component;
    }

    public int size() {
        return buffer.size();
    }

    public ITextComponent format(String message) {
        ITextComponent component = new TextComponentTranslation("animatedcrosshair.chat.prefix").appendText(" ");
        component.setStyle(new Style().setColor(TextFormatting.DARK_AQUA));

        component.appendSibling(new TextComponentString(message).setStyle(new Style().setColor(TextFormatting.RESET)));

        return component;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            EntityPlayerSP player = Minecraft.getMinecraft().player;

            try {
                if(size() > 0 && player != null) {
                    player.sendMessage(pull());
                }

                Thread.sleep(100);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


}
