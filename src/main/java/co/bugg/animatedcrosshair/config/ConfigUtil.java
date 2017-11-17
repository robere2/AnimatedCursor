package co.bugg.animatedcrosshair.config;

import co.bugg.animatedcrosshair.AnimatedCrosshair;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Class containing configuration utilities
 */
public class ConfigUtil {

    /**
     * Root of where the assets (crosshair files) are stored
     */
    public static String assetsRoot = "crosshair/assets/animatedcrosshair/";

    /**
     * Creates the folder where crosshair assets are put
     * @return Root folder for the "resource pack"
     * @throws IOException Error creating the folder
     */
    public static File createAssetsFolder() throws IOException {
        // Make sure the root folder exists
        final File resourcePackRoot = new File("crosshair");
        if(!resourcePackRoot.isDirectory() && !resourcePackRoot.mkdir()) {
            throw new IOException("Couldn't create directory " + resourcePackRoot.getAbsolutePath());
        }

        // Make sure the mcmeta file exists
        final File resourcePackMcMeta = new File("crosshair/pack.mcmeta");
        if(!resourcePackMcMeta.exists() && !resourcePackMcMeta.createNewFile()) {
            throw new IOException("Couldn't create file " + resourcePackMcMeta.getAbsolutePath());
        }

        // Write the default pack mcmeta content to the file
        final FileWriter writer = new FileWriter(resourcePackMcMeta, false);
        writer.write("{\n" +
                "            \"pack\": {\n" +
                "            \"pack_format\": 1,\n" +
                "                    \"description\": \"Resource pack containing your AnimatedCrosshair texture.\"\n" +
                "        }\n" +
                "        }");
        writer.close();

        // Make sure the assets folder exists
        final File resourcePackAssets = new File("crosshair/assets");
        if(!resourcePackAssets.isDirectory() && !resourcePackAssets.mkdir()) {
            throw new IOException("Couldn't create directory " + resourcePackAssets.getAbsolutePath());
        }

        // Make sure the mod ID folder exists
        final File resourcePackAnimatedCrosshair = new File("crosshair/assets/animatedcrosshair");
        if(!resourcePackAnimatedCrosshair.isDirectory() && !resourcePackAnimatedCrosshair.mkdir()) {
            throw new IOException("Couldn't create directory " + resourcePackAnimatedCrosshair.getAbsolutePath());
        }

        return resourcePackRoot;
    }

    /**
     * Load the default assets if they aren't already in the assets folder
     * @throws IOException Error loading an asset
     */
    public static void loadDefaultAssets() throws IOException {
        final ArrayList<String> defaultCrosshairs = new ArrayList<>();

        defaultCrosshairs.add("zoom_square");
        defaultCrosshairs.add("chroma_plus");
        defaultCrosshairs.add("default");
        defaultCrosshairs.add("middle_circle");
        defaultCrosshairs.add("shapeshift_box");
        defaultCrosshairs.add("spinning_plus");
        defaultCrosshairs.add("spinning_plus_fat");
        defaultCrosshairs.add("triangle_illusion");

        final String resourceRoot = "/default/";

        for(String crosshair : defaultCrosshairs) {
            final File crosshairFile = new File(assetsRoot + crosshair + ".png");
            final File propertiesFile = new File(assetsRoot + crosshair + ".properties");

            if(!crosshairFile.exists()) {
                final URL resource = AnimatedCrosshair.INSTANCE.getClass().getResource(resourceRoot + crosshair + ".png");
                FileUtils.copyURLToFile(resource, crosshairFile);
            }

            if(!propertiesFile.exists()) {
                final URL resource = AnimatedCrosshair.INSTANCE.getClass().getResource(resourceRoot + crosshair + ".properties");
                FileUtils.copyURLToFile(resource, propertiesFile);
            }
        }
    }

    /**
     * Get a properties object for the provided crosshair name
     * @param name Name of the crosshair to get properties for
     * @return Properties object
     * @throws IOException Issue reading the properties file
     */
    public static Properties getProperties(String name) throws IOException {
        final File propertiesFile = new File(ConfigUtil.assetsRoot + name + ".properties");
        if(propertiesFile.exists()) {

            final Gson gson = new Gson();

            final String propertiesJson = new String(Files.readAllBytes(Paths.get(propertiesFile.toURI())), StandardCharsets.UTF_8);

            return gson.fromJson(propertiesJson, Properties.class);
        } else {
            throw new FileNotFoundException("There is no properties file for this crosshair!");
        }
    }
}
