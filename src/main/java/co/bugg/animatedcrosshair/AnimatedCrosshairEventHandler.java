package co.bugg.animatedcrosshair;

import co.bugg.animatedcrosshair.config.ConfigUtil;
import co.bugg.animatedcrosshair.http.Response;
import co.bugg.animatedcrosshair.http.WebRequests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.File;

/**
 * Contains all the event handlers for the mod
 */
public class AnimatedCrosshairEventHandler {
    @SubscribeEvent
    public void onGameRenderOverlay(RenderGameOverlayEvent event) {
        if(
                event.getType() == RenderGameOverlayEvent.ElementType.CROSSHAIRS &&
                AnimatedCrosshair.INSTANCE.enabled &&
                new File(ConfigUtil.assetsRoot + AnimatedCrosshair.INSTANCE.config.getCurrentCrosshairName().toLowerCase() + ".png").exists() &&
                new File(ConfigUtil.assetsRoot + AnimatedCrosshair.INSTANCE.config.getCurrentCrosshairName().toLowerCase() + ".properties").exists()
            ) {

            GuiIngame gui = Minecraft.getMinecraft().ingameGUI;

            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();

            double scale = AnimatedCrosshair.INSTANCE.config.getCurrentProperties().crosshairScale;
            int[] coords = AnimatedCrosshair.INSTANCE.calculateCoords(AnimatedCrosshair.INSTANCE.frame);

            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, AnimatedCrosshair.INSTANCE.config.getCurrentCrosshairName().toLowerCase() + ".png"));
            GlStateManager.scale(scale, scale, scale);

            if(AnimatedCrosshair.INSTANCE.config.getCurrentProperties().negativeColor) GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);

            gui.drawTexturedModalRect(
                    (int) (((scaledResolution.getScaledWidth() / 2) / scale) - 16 / 2),
                    (int) (((scaledResolution.getScaledHeight() / 2) / scale) - 16 / 2),
                    coords[1],
                    coords[0],
                    16,
                    16
            );

            if(AnimatedCrosshair.INSTANCE.config.getCurrentProperties().negativeColor) GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.popMatrix();

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
