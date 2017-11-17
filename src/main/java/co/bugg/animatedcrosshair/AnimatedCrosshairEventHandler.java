package co.bugg.animatedcrosshair;

import co.bugg.animatedcrosshair.config.ConfigUtil;
import co.bugg.animatedcrosshair.config.Configuration;
import co.bugg.animatedcrosshair.http.Response;
import co.bugg.animatedcrosshair.http.WebRequests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Contains all the event handlers for the mod
 */
public class AnimatedCrosshairEventHandler {
    @SubscribeEvent
    public void onGameRenderOverlay(RenderGameOverlayEvent event) {
        if(
                event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS &&
                AnimatedCrosshair.INSTANCE.enabled &&
                new File(ConfigUtil.assetsRoot + AnimatedCrosshair.INSTANCE.config.getCurrentCrosshairName().toLowerCase() + ".png").exists() &&
                new File(ConfigUtil.assetsRoot + AnimatedCrosshair.INSTANCE.config.getCurrentCrosshairName().toLowerCase() + ".properties").exists()
            ) {

            GuiIngame gui = Minecraft.getMinecraft().ingameGUI;

            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

            try {
                AnimatedCrosshair.INSTANCE.drawCrosshair(gui, scaledResolution.getScaledWidth() / 2, scaledResolution.getScaledHeight() / 2, AnimatedCrosshair.INSTANCE.config.getCurrentCrosshairName(), AnimatedCrosshair.INSTANCE.config.getCurrentProperties());
            } catch (IOException e) {
                System.out.println("Current crosshair can't be rendered! Trying to go back to default...");
                try {
                    AnimatedCrosshair.INSTANCE.config.setCurrentCrosshairName(Configuration.defaultCrosshairName);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                    System.out.println("I wasn't able to recover, and the default crosshair couldn't be rendered. Shutting down mod.");
                    AnimatedCrosshair.INSTANCE.enabled = false;
                }
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onJoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {

        String ip = AnimatedCrosshair.INSTANCE.getCurrentIP();

        // Send a web request to the website, to
        // track stats & to check for what to do next
        new Thread(() -> {
            Response response = WebRequests.joinRequest(ip);
            WebRequests.basicResponseHandler(response);
        }).start();
    }

    @SubscribeEvent
    public void onLeave(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {

        // Send a web request to the website, to
        // track stats & to check for what to do next
        new Thread(() -> {
            Response response = WebRequests.leaveRequest();
            WebRequests.basicResponseHandler(response);
        }).start();
    }
}
