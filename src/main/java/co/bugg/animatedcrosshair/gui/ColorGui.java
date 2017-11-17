package co.bugg.animatedcrosshair.gui;

import co.bugg.animatedcrosshair.AnimatedCrosshair;
import co.bugg.animatedcrosshair.Reference;
import co.bugg.animatedcrosshair.TickDelay;
import co.bugg.animatedcrosshair.config.ConfigUtil;
import co.bugg.animatedcrosshair.config.Properties;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.io.IOException;

/**
 * GUI to customize specific properties of a crosshair
 */
public class ColorGui extends GuiScreen {

    int sliderWidth = 150;
    int sliderHeight = 20;
    int sliderMargin = 5;

    /**
     * Crosshair name we're customizing
     */
    public String name;

    public int red = 255;
    public int green = 255;
    public int blue = 255;
    public boolean chroma = false;
    public boolean negativeColor = true;

    public ColorGui(String name) {
        super();
        this.name = name;

        Properties properties;
        try {
            properties = ConfigUtil.getProperties(name);

            // Only set these colors if color modifier isn't null.
            // Otherwise, use white values on the sliders
            if(properties.colorModifier != null) {
                red = properties.colorModifier.getRed();
                green = properties.colorModifier.getGreen();
                blue = properties.colorModifier.getBlue();
            }

            chroma = properties.chromaColor;
            negativeColor = properties.negativeColor;

        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            new TickDelay(() -> Minecraft.getMinecraft().displayGuiScreen(null), 0);
            AnimatedCrosshair.INSTANCE.messageBuffer.add(AnimatedCrosshair.INSTANCE.messageBuffer.format(new TextComponentTranslation("animatedcrosshair.error.readerror").getUnformattedText()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(fontRendererObj, Reference.MOD_NAME + " " + new TextComponentTranslation("animatedcrosshair.config.configuration").getUnformattedText(), width / 2, height / 2 - (sliderMargin + sliderHeight) * 3, 0xFFFFFF);
        drawCenteredString(fontRendererObj, AnimatedCrosshair.INSTANCE.credits, width / 2, height - 10, 0xFFFFFF);
    }

    @Override
    public void initGui() {
        super.initGui();

        int buttonId = 0;

        ColorGuiResponder responder = new ColorGuiResponder();
        ColorGuiFormatHelper formatHelper = new ColorGuiFormatHelper();

        buttonList.add(new GuiSlider(responder, buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), new TextComponentTranslation("animatedcrosshair.color.red").getUnformattedText(), 0F, 255F, red, formatHelper));
        buttonId++;
        buttonList.add(new GuiSlider(responder, buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), new TextComponentTranslation("animatedcrosshair.color.green").getUnformattedText(), 0F, 255F, green, formatHelper));
        buttonId++;
        buttonList.add(new GuiSlider(responder, buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), new TextComponentTranslation("animatedcrosshair.color.blue").getUnformattedText(), 0F, 255F, blue, formatHelper));
        buttonId++;
        // TODO
//        buttonList.add(new GuiButton(buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), sliderWidth, sliderHeight, new ChatComponentTranslation("animatedcrosshair.color.chroma").getUnformattedText() + ": " + (chroma ? new ChatComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new ChatComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText())));
//        buttonId++;
        buttonList.add(new GuiButton(buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), sliderWidth, sliderHeight, new TextComponentTranslation("animatedcrosshair.properties.negativecolor").getUnformattedText() + ": " + (negativeColor ? new TextComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new TextComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText())));
        buttonId++;
        buttonList.add(new GuiButton(buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), sliderWidth, sliderHeight, new TextComponentTranslation("animatedcrosshair.config.save").getUnformattedText()));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if(button.displayString.equalsIgnoreCase(new TextComponentTranslation("animatedcrosshair.config.save").getUnformattedText())) {
            Color colorObject = new Color(red, green, blue);

            // Set the color in our config properties object for this crosshair
            Properties properties = ConfigUtil.getProperties(name);
            properties.colorModifier = colorObject;
            properties.chromaColor = chroma;
            properties.negativeColor = negativeColor;

            // Save the properties to the config if they're supposed to be applied
            if(AnimatedCrosshair.INSTANCE.config.getCurrentCrosshairName().equalsIgnoreCase(name)) {
                AnimatedCrosshair.INSTANCE.config.setCurrentProperties(properties);
            }

            // Save the properties to their properties file
            AnimatedCrosshair.INSTANCE.config.saveProperties(name, properties);

            Minecraft.getMinecraft().displayGuiScreen(new TechnicalGui(name));

        } else if(button.displayString.contains(new TextComponentTranslation("animatedcrosshair.color.chroma").getUnformattedText())) {
            // Swap the "Chroma Color" value
            chroma = !chroma;
            button.displayString = new TextComponentTranslation("animatedcrosshair.color.chroma").getUnformattedText() + ": " + (chroma ? new TextComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new TextComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText());
        } else if(button.displayString.contains(new TextComponentTranslation("animatedcrosshair.properties.negativecolor").getUnformattedText())) {
            // Swap the "Negative Color" value
            negativeColor = !negativeColor;
            button.displayString = new TextComponentTranslation("animatedcrosshair.properties.negativecolor").getUnformattedText() + ": " + (negativeColor ? new TextComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new TextComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText());
        }
    }

    public class ColorGuiFormatHelper implements GuiSlider.FormatHelper {
        final String red = new TextComponentTranslation("animatedcrosshair.color.red").getUnformattedText();
        final String green = new TextComponentTranslation("animatedcrosshair.color.green").getUnformattedText();
        final String blue = new TextComponentTranslation("animatedcrosshair.color.blue").getUnformattedText();

        /**
         * Text that should be displayed on the slider
         * @param id ID of the slider
         * @param name Name of the slider
         * @param value Value of the slider
         * @return String to display
         */
        @Override
        public String getText(int id, String name, float value) {
            return name + ": " + (int) value;
        }
    }

    @ParametersAreNonnullByDefault
    public class ColorGuiResponder implements GuiPageButtonList.GuiResponder {
        /**
         * Called every time the value of a boolean button changes
         * Unused in this Minecraft mod
         * @param id ID of the button
         * @param value Value of the button
         */
        @Override
        public void setEntryValue(int id, boolean value) {

        }

        /**
         * Called every tick that the mouse button is down on a slider
         * @param id ID of the slider/button
         * @param value value of the slider/button
         */
        @Override
        public void setEntryValue(int id, float value) {
            if(buttonList.get(id).displayString.contains(new TextComponentTranslation("animatedcrosshair.color.red").getUnformattedText())) {
                red = (int) value;
            } else if(buttonList.get(id).displayString.contains(new TextComponentTranslation("animatedcrosshair.color.green").getUnformattedText())) {
                green = (int) value;
            } else if(buttonList.get(id).displayString.contains(new TextComponentTranslation("animatedcrosshair.color.blue").getUnformattedText())) {
                blue = (int) value;
            }
        }

        /**
         * Called every time the value of a text box changes
         * Unused in this Minecraft mod
         * @param id ID of the text box
         * @param value Value of the text box
         */
        @Override
        public void setEntryValue(int id, String value) {

        }
    }
}
