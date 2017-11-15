package co.bugg.animatedcrosshair;

import co.bugg.animatedcrosshair.command.CommandCrosshair;
import co.bugg.animatedcrosshair.config.ConfigUtil;
import co.bugg.animatedcrosshair.config.Configuration;
import co.bugg.animatedcrosshair.http.Response;
import co.bugg.animatedcrosshair.http.WebRequests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.FolderResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION,
        clientSideOnly = true
)
public class AnimatedCrosshair {

    /**
     * Instance of this mod
     */
    @Mod.Instance
    public static AnimatedCrosshair INSTANCE = new AnimatedCrosshair();
    /**
     * Current frame being rendered
     */
    public int frame;
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

        File resourcePackRoot;
        try {
            resourcePackRoot = ConfigUtil.createAssetsFolder();
            ConfigUtil.loadDefaultAssets();
        } catch (IOException e) {
            System.out.println("Error while creating texture folder & copying defaults!");
            e.printStackTrace();
            return;
        }

        try {
            config = Configuration.load();
        } catch (IOException e) {
            System.out.println("Caught exception while loading the config");
            e.printStackTrace();
        }

        resourcePack = new FolderResourcePack(resourcePackRoot);

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
            AnimatedCrosshair.INSTANCE.pingThread = ThreadFactory.createPingThread(pingInterval);
            AnimatedCrosshair.INSTANCE.pingThread.start();
        }).start();

        // Start the frame incrementation thread
        AnimatedCrosshair.INSTANCE.framerateThread = ThreadFactory.createFramerateThread();
        AnimatedCrosshair.INSTANCE.framerateThread.start();

        // Add the custom resource pack we've created to the list of registered packs
        try {
            Field defaultResourcePacksField = Minecraft.class.getDeclaredField("defaultResourcePacks");
            defaultResourcePacksField.setAccessible(true);
            List<IResourcePack> defaultResourcePacks = (List<IResourcePack>) defaultResourcePacksField.get(Minecraft.getMinecraft());

            defaultResourcePacks.add(resourcePack);

            defaultResourcePacksField.set(Minecraft.getMinecraft(), defaultResourcePacks);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

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
}
