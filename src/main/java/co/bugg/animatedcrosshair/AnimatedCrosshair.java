package co.bugg.animatedcrosshair;

import co.bugg.animatedcrosshair.command.CommandCrosshair;
import co.bugg.animatedcrosshair.config.ConfigUtil;
import co.bugg.animatedcrosshair.config.Configuration;
import co.bugg.animatedcrosshair.config.Properties;
import co.bugg.animatedcrosshair.http.Response;
import co.bugg.animatedcrosshair.http.WebRequests;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION,
        clientSideOnly = true,
        acceptedMinecraftVersions = Reference.ACCEPTED_VERSIONS
)
public class AnimatedCrosshair {

    /**
     * Instance of this mod
     */
    @Mod.Instance
    public static AnimatedCrosshair INSTANCE = new AnimatedCrosshair();
    /**
     * Mod configuration
     */
    public Configuration config;
    /**
     * "Resource Pack" that the crosshair file is in
     * .minecraft/ROOT/assets/
     */
    public FolderResourcePack resourcePack;
    /**
     * Thread for incrementing frames at set framerate
     */
    public Thread framerateThread;
    /**
     * Whether the mod is currently enabled or not.
     */
    public boolean enabled = true;
    /**
     * Buffer to send messages that should be sent to the client to
     */
    public MessageBuffer messageBuffer;
    /**
     * Number of milliseconds between "ping" requests
     * Defaults to every 60 seconds, but can be changed on web server
     */
    public int pingInterval = 60000;
    /**
     * Thread that sends the ping request whenever necessary
     */
    public Thread pingThread;
    /**
     * Client for HTTP requests
     */
    public CloseableHttpClient httpClient;

    public String credits = Reference.MOD_NAME + " v" + Reference.VERSION + " by @bugfroggy";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Create our HTTP client
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setUserAgent("Minecraft Mod " + Reference.MOD_NAME + " - " + Reference.VERSION)
                .build();


        // Create and start the message buffer
        messageBuffer = new MessageBuffer();
        messageBuffer.start();

        // Create the resource pack folder & load default assets
        File resourcePackRoot;
        try {
            resourcePackRoot = ConfigUtil.createAssetsFolder();
            ConfigUtil.loadDefaultAssets();
        } catch (IOException e) {
            System.out.println("Error while creating texture folder & copying defaults!");
            e.printStackTrace();
            return;
        }

        // Load the config
        try {
            config = Configuration.load();
        } catch (JsonSyntaxException | IOException e) {
            System.out.println("Caught exception while loading the config. Trying to repair...");
            e.printStackTrace();
            try {
                config = new Configuration().save();
            } catch (IOException e1) {
                enabled = false;
                messageBuffer.add(messageBuffer.format(new TextComponentTranslation("animatedcrosshair.error.invalidconfig").getUnformattedText()));
                e1.printStackTrace();
            }
        }

        // Add the resource pack to minecraft's resource pack list
        resourcePack = new FolderResourcePack(resourcePackRoot);
        addResourcePack();

        MinecraftForge.EVENT_BUS.register(new AnimatedCrosshairEventHandler());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        messageBuffer = new MessageBuffer();
        messageBuffer.start();

        // Send a web request to the website, to
        // track stats & to check for what to do next
        new Thread(() -> {
            Response response = WebRequests.initRequest();
            WebRequests.basicResponseHandler(response);

            // Start sending ping requests
            AnimatedCrosshair.INSTANCE.pingThread = ThreadFactory.createPingThread();
            AnimatedCrosshair.INSTANCE.pingThread.start();
        }).start();

        // Start the frame incrementation thread
        if(AnimatedCrosshair.INSTANCE.framerateThread != null) framerateThread.interrupt();
        AnimatedCrosshair.INSTANCE.framerateThread = ThreadFactory.createFramerateThread(config.getCurrentProperties());
        AnimatedCrosshair.INSTANCE.framerateThread.start();

        ClientCommandHandler.instance.registerCommand(new CommandCrosshair());
    }

    /**
     * Calculate the coordinates of the specified frame in a
     * 256x256 grid, assuming each item is 16x16.
     * @param frame Frame to calculate
     * @return int array, 0 mapping to X and 1 mapping to Y
     */
    public int[] calculateCoords(int frame) {
        int[] array = new int[2];

        int row = frame % 16;

        int column = (int) Math.floor((double) frame / 16.0);

        array[0] = row * 16;
        array[1] = column * 16;

        return array;
    }

    /**
     * get whatever IP the player is currently on
     * @return Current IP, "singleplayer" if single player, or null if undetermined
     */
    public String getCurrentIP() {
        String ip;

        if(Minecraft.getMinecraft().isSingleplayer()) {
            ip = "singleplayer";
        } else {
            ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
            if (serverData != null) {
                ip = serverData.serverIP;
            } else {
                ip = "null";
            }
        }

        return ip;
    }

    /**
     * Add the resource pack object from this instance
     * into the list of Minecraft resource packs
     */
    public void addResourcePack() {
        // Add the custom resource pack we've created to the list of registered packs
        try {
            Field defaultResourcePacksField;
            try {
                // Try to get the field for the obfuscated "defaultResourcePacks" field
                defaultResourcePacksField = Minecraft.class.getDeclaredField("field_110449_ao");
            } catch(NoSuchFieldException e) {
                // Obfuscated name wasn't found. Let's try the deobfuscated name.
                defaultResourcePacksField = Minecraft.class.getDeclaredField("defaultResourcePacks");
            }

            defaultResourcePacksField.setAccessible(true);
            List<IResourcePack> defaultResourcePacks = (List<IResourcePack>) defaultResourcePacksField.get(Minecraft.getMinecraft());

            defaultResourcePacks.add(resourcePack);

            defaultResourcePacksField.set(Minecraft.getMinecraft(), defaultResourcePacks);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            System.out.println("Disabling the mod, as we can't add our custom resource pack.");
            System.out.println("Please report this to @bugfroggy, providing this error log and this list: " + Arrays.toString(Minecraft.class.getDeclaredFields()));
            enabled = false;
            e.printStackTrace();
        }

        // Refresh the resources of the game
        Minecraft.getMinecraft().refreshResources();
    }

    /**
     * Draw a provided crosshair on the provided screen at the provided coords
     * @param gui GUI to draw on
     * @param posX X position to draw at (scaling & centering done inside method)
     * @param posY Y position to draw at (scaling & centering done inside method)
     * @param name Name of the crosshair to draw
     * @param properties Properties to draw the crosshair with
     * @throws IOException Issue drawing the crosshair because its properties file isn't found
     */
    public void drawCrosshair(Gui gui, int posX, int posY, String name, Properties properties) throws IOException {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        double scale = properties.crosshairScale;
        Color colorModifier = properties.colorModifier;

        int[] coords = calculateCoords(properties.frame);

        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(Reference.MOD_ID, name + ".png"));
        GlStateManager.scale(scale, scale, scale);

        if(properties.negativeColor) GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);

        // Modify the color to be what's in the config, or white if no value
        if(colorModifier != null)
            GlStateManager.color((float) colorModifier.getRed() / 255.0F, (float) colorModifier.getGreen() / 255.0F, (float) colorModifier.getBlue() / 255.0F);
        else
            GlStateManager.color(1, 1, 1);

        gui.drawTexturedModalRect(
                (int) (posX / scale - 16 / 2),
                (int) (posY / scale - 16 / 2),
                coords[1],
                coords[0],
                16,
                16
        );

        GlStateManager.color(1, 1, 1);

        if(properties.negativeColor) GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
    }
}

