package co.bugg.animatedcrosshair.config;

import co.bugg.animatedcrosshair.AnimatedCrosshair;
import co.bugg.animatedcrosshair.ThreadFactory;
import com.google.gson.Gson;
import net.minecraft.util.text.TextComponentTranslation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Custom AnimatedCrosshair configuration
 */
public class Configuration implements Serializable {
    public static final String defaultCrosshairName = "zoom_square";

    /**
     * Where the configuration file is stored
     */
    private static final File configFile = new File("./crosshair/config.json");

    /**
     * The name of the crosshair that should be rendered
     */
    private String currentCrosshairName;
    /**
     * The properties that this crosshair should be rendered with
     */
    private Properties currentProperties;

    public Configuration() throws IOException {
        setCurrentCrosshairName(defaultCrosshairName);
        loadProperties();
    }

    /**
     * Getter for currentCrosshairName
     * @return currentCrosshairName
     */
    public String getCurrentCrosshairName() {
        return currentCrosshairName;
    }

    /**
     * Setter for currentCrosshairName
     * @param name New value
     * @return This configuration
     */
    public Configuration setCurrentCrosshairName(String name) throws FileNotFoundException {
        if(!new File(ConfigUtil.assetsRoot + name + ".properties").exists()) throw new FileNotFoundException("No properties file found!");
        if(!new File(ConfigUtil.assetsRoot + name + ".png").exists()) throw new FileNotFoundException("No image file found!");

        currentCrosshairName = name;
        return this;
    }

    /**
     * Getter for currentProperties
     * @return currentProperties
     */
    public Properties getCurrentProperties() {
        return currentProperties;
    }

    /**
     * Setter for currentProperties
     * @param currentProperties New value
     * @return This configuration
     */
    public Configuration setCurrentProperties(Properties currentProperties) {
        this.currentProperties = currentProperties;

        AnimatedCrosshair.INSTANCE.framerateThread = ThreadFactory.createFramerateThread(currentProperties);
        AnimatedCrosshair.INSTANCE.framerateThread.start();

        return this;
    }

    /**
     * Save the configuration to its file
     * @return This configuration
     * @throws IOException Error writing to file
     */
    public Configuration save() throws IOException {
        final Gson gson = new Gson();

        if(!configFile.exists() && !configFile.createNewFile()) {
            throw new IOException("Failed to create the configuration file");
        } else {
            Files.write(Paths.get(configFile.toURI()), gson.toJson(this).getBytes());
        }

        return this;
    }

    /**
     * Load the configuration file & save it to make sure
     * @return The configuration object for the respective file
     * @throws IOException Error reading or writing to file
     */
    public static Configuration load() throws IOException {

        if(!configFile.exists()) {
            return new Configuration()
                    .setCurrentCrosshairName(defaultCrosshairName)
                    .loadProperties()
                    .save();

        } else {
            final Gson gson = new Gson();

            final String configJson = new String(Files.readAllBytes(Paths.get(configFile.toURI())), StandardCharsets.UTF_8);
            Configuration config = gson.fromJson(configJson, Configuration.class);

            // Check if the PNG file from the config doesnt exist
            if(!new File(ConfigUtil.assetsRoot + config.getCurrentCrosshairName() + ".png").exists()) {
                config.setCurrentCrosshairName(defaultCrosshairName);
            }

            return config.loadProperties().save();
        }
    }

    /**
     * Load the properties file for the current crosshair
     * @return This configuration
     * @throws IOException Error reading the properties file
     */
    public Configuration loadProperties() throws IOException {
        try {
            setCurrentProperties(ConfigUtil.getProperties(getCurrentCrosshairName()));
        } catch(FileNotFoundException e) {
            AnimatedCrosshair.INSTANCE.messageBuffer.add(AnimatedCrosshair.INSTANCE.messageBuffer.format(new TextComponentTranslation("animatedcrosshair.error.invalidproperties").getUnformattedText()));
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Alternate version of Configuration#saveProperties, seen below, without variables
     * This configurations variables are passed to the method.
     * @return This configuration.
     */
    public Configuration saveProperties() {
        try {
            saveProperties(getCurrentCrosshairName(), getCurrentProperties());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * Save the current properties to the properties file for the current crosshair
     * @return This configuration
     * @throws IOException Failed to write the properties for some reason
     */
    public Configuration saveProperties(String name, Properties properties) throws IOException {
        final File propertiesFile = new File(ConfigUtil.assetsRoot + name + ".properties");

        if(!propertiesFile.exists() && !propertiesFile.createNewFile())
            throw new IOException("Failed to create the proeprties file!");

        final Gson gson = new Gson();

        Files.write(propertiesFile.toPath(), gson.toJson(properties).getBytes());

        return this;
    }
}
