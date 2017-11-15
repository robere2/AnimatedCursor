package co.bugg.animatedcrosshair.http;

import co.bugg.animatedcrosshair.AnimatedCrosshair;
import co.bugg.animatedcrosshair.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.ForgeVersion;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Class built for sending web requests to my website
 */
public class WebRequests {

    /**
     * Send an "init" request, marking client start
     * Should be executed in its own thread!
     * @return Response from the website
     */
    public static Response initRequest() {
        try {

            HashMap<String, String> params = new HashMap<>();

            params.put("version", Reference.VERSION);
            params.put("forge", reflectForgeVersion());
            params.put("mc", reflectMcVersion());
            params.put("mcp", reflectMcpVersion());
            params.put("java", System.getProperty("java.version"));
            params.put("os", System.getProperty("os.name") + " - " + System.getProperty("os.version"));
            params.put("uuid", Minecraft.getMinecraft().getSession().getPlayerID());
            params.put("username", Minecraft.getMinecraft().getSession().getUsername());

            return baseRequest("init", params);

        } catch (NoSuchMethodException | InvocationTargetException | NoSuchFieldException | IllegalAccessException e) {
            System.out.println("Failed to send diagnostic data!");
            e.printStackTrace();
        }
        return new Response(false);
    }

    /**
     * Send a "join" request, marking having joined a server
     * Should be executed in its own thread!
     * @param ip IP that the client is connecting to
     * @return Response from the website
     */
    public static Response joinRequest(String ip) {
        HashMap<String, String> params = new HashMap<>();

        params.put("version", Reference.VERSION);
        params.put("uuid", Minecraft.getMinecraft().getSession().getPlayerID());
        params.put("ip", ip);

        return baseRequest("join", params);
    }

    /**
     * Send a "leave" request, marking having disconnected from a server
     * Should be executed in its own thread!
     * @return Response from the website
     */
    public static Response leaveRequest() {
        HashMap<String, String> params = new HashMap<>();

        params.put("version", Reference.VERSION);
        params.put("uuid", Minecraft.getMinecraft().getSession().getPlayerID());

        return baseRequest("leave", params);
    }

    /**
     * Used to send a request to my website every so often,
     * so I know how many active clients are using the mod
     * at any given moment.
     * Should be executed in its own thread!
     * @param ip IP that the client is connected to
     * @return Response from the website
     */
    public static Response pingRequest(String ip) {
        HashMap<String, String> params = new HashMap<>();

        params.put("version", Reference.VERSION);
        params.put("uuid", Minecraft.getMinecraft().getSession().getPlayerID());
        params.put("ip", ip);

        return baseRequest("ping", params);
    }

    /**
     * Basic web request to the AnimatedCrosshair web request API
     * Should be executed in its own thread!
     * @param endpoint Endpoint to request to
     * @param params GET parameters to request with
     * @return Response from the website
     */
    public static Response baseRequest(String endpoint, HashMap<String, String> params) {

        try {
            URIBuilder builder = new URIBuilder("https://aws.bugg.co/mods/animatedcrosshair/" + endpoint);

            // Add all the parameters
            for(Map.Entry<String, String> entry : params.entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }

            // Execute the request
            HttpGet get = new HttpGet(builder.toString());

            try (CloseableHttpResponse httpResponse = AnimatedCrosshair.INSTANCE.httpClient.execute(get)) {
                int responseCode = httpResponse.getStatusLine().getStatusCode();

                // If the response code is a successful one
                if (200 <= responseCode && responseCode < 300) {
                    // Get the response from the buffer & read it into a Response object
                    StringWriter writer = new StringWriter();

                    InputStream stream = httpResponse.getEntity().getContent();
                    IOUtils.copy(stream, writer, StandardCharsets.UTF_8);

                    stream.close();

                    return Response.fromJson(writer.toString());
                }
            }
        } catch (URISyntaxException | IOException e) {
            System.out.println("Failed to send diagnostic data!");
            e.printStackTrace();
        }

        // The request probably wasn't successful.
        return new Response(false);
    }

    /**
     * Basic Response handling used in many locations.
     * @param response Response to handle
     */
    public static void basicResponseHandler(Response response) {
        if(response.ok && response.actions != null && response.actions.size() > 0) {
            for(HashMap<Response.Action, String> actionSection : response.actions) {
                for (Map.Entry<Response.Action, String> action : actionSection.entrySet()) {
                    if (action.getKey() == Response.Action.SEND_MESSAGE) {

                        IChatComponent message = AnimatedCrosshair.INSTANCE.messageBuffer.format(action.getValue());
                        AnimatedCrosshair.INSTANCE.messageBuffer.add(message);
                    } else if (action.getKey() == Response.Action.SHUTDOWN) {
                        System.out.println("Shutdown requested from the API - " + action.getValue());
                        AnimatedCrosshair.INSTANCE.enabled = false;
                    } else if (action.getKey() == Response.Action.SYSTEM_OUT) {
                        System.out.println("[API SYSTEM_OUT] " + action.getValue());
                    } else {
                        System.out.println("Unknown API response: " + action.getKey() + " - " + action.getValue());
                    }
                }
            }
        }
    }

    /**
     * Reflectively get the user's Minecraft version
     * @return Minecraft version
     * @throws NoSuchFieldException mcVersion field doesn't exist
     * @throws IllegalAccessException Can't access mcVersion field
     */
    public static String reflectMcVersion() throws NoSuchFieldException, IllegalAccessException {
        return (String) ForgeVersion.class.getField("mcVersion").get(new ForgeVersion());
    }

    /**
     * Reflectively get user's Forge version
     * @return Forge version
     * @throws NoSuchMethodException getVersion method doesn't exist
     * @throws InvocationTargetException Something went wrong with method invocation
     * @throws IllegalAccessException Can't access getVersion method
     */
    public static String reflectForgeVersion() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (String) ForgeVersion.class.getMethod("getVersion").invoke(new ForgeVersion());
    }

    /**
     * Reflectively get user's MCP version
     * @return MCP version
     * @throws NoSuchFieldException mcpVersion field doesn't exist
     * @throws IllegalAccessException Can't access mcpVersion field
     */
    public static String reflectMcpVersion() throws NoSuchFieldException, IllegalAccessException {
        return (String) ForgeVersion.class.getField("mcpVersion").get(new ForgeVersion());
    }
}
