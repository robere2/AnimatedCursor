package co.bugg.animatedcrosshair.gui;

import co.bugg.animatedcrosshair.AnimatedCrosshair;
import co.bugg.animatedcrosshair.Reference;
import co.bugg.animatedcrosshair.ThreadFactory;
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

    /**
     * Temporary properties for this crosshair
     * modification window. Saved to the config/file
     * if the "save" button is pressed.
     */
    Properties properties;

    /**
     * Thread that modifies the current frame number
     * in the properties object.
     * @see co.bugg.animatedcrosshair.ThreadFactory#createFramerateThread(Properties)
     */
    Thread crosshairFrameThread;

    public ColorGui(String name) {
        super();
        this.name = name;

        try {
            properties = ConfigUtil.getProperties(name);

            if(properties.colorModifier == null) properties.colorModifier = new Color(255, 255, 255);

            crosshairFrameThread = ThreadFactory.createFramerateThread(properties);
            crosshairFrameThread.start();

        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            new TickDelay(() -> Minecraft.getMinecraft().displayGuiScreen(null), 0);
            AnimatedCrosshair.INSTANCE.messageBuffer.add(AnimatedCrosshair.INSTANCE.messageBuffer.format(new TextComponentTranslation("animatedcrosshair.error.readerror").getUnformattedText()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // Draw the sample crosshair
        try {
            AnimatedCrosshair.INSTANCE.drawCrosshair(this, width / 2, (int) (height / 2 - (sliderMargin + sliderHeight) * 3.5), name, properties);
        } catch (IOException e) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            AnimatedCrosshair.INSTANCE.messageBuffer.add(AnimatedCrosshair.INSTANCE.messageBuffer.format(new TextComponentTranslation("animatedcrosshair.error.readerror").getUnformattedText()));
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(fontRenderer, Reference.MOD_NAME + " " + new TextComponentTranslation("animatedcrosshair.config.configuration").getUnformattedText(), width / 2, height / 2 - (sliderMargin + sliderHeight) * 3, 0xFFFFFF);
        drawCenteredString(fontRenderer, AnimatedCrosshair.INSTANCE.credits, width / 2, height - 10, 0xFFFFFF);
    }

    @Override
    public void initGui() {
        super.initGui();

        int buttonId = 0;

        ColorGuiResponder responder = new ColorGuiResponder();
        ColorGuiFormatHelper formatHelper = new ColorGuiFormatHelper();

        buttonList.add(new GuiSlider(responder, buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), new TextComponentTranslation("animatedcrosshair.color.red").getUnformattedText(), 0F, 255F, properties.colorModifier.getRed(), formatHelper));
        buttonId++;
        buttonList.add(new GuiSlider(responder, buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), new TextComponentTranslation("animatedcrosshair.color.green").getUnformattedText(), 0F, 255F, properties.colorModifier.getGreen(), formatHelper));
        buttonId++;
        buttonList.add(new GuiSlider(responder, buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), new TextComponentTranslation("animatedcrosshair.color.blue").getUnformattedText(), 0F, 255F, properties.colorModifier.getBlue(), formatHelper));
        buttonId++;
        // TODO
//        buttonList.add(new GuiButton(buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), sliderWidth, sliderHeight, new ChatComponentTranslation("animatedcrosshair.color.chroma").getUnformattedText() + ": " + (chroma ? new ChatComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new ChatComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText())));
//        buttonId++;
        buttonList.add(new GuiButton(buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), sliderWidth, sliderHeight, new TextComponentTranslation("animatedcrosshair.properties.negativecolor").getUnformattedText() + ": " + (properties.negativeColor ? new TextComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new TextComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText())));
        buttonId++;
        buttonList.add(new GuiButton(buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), sliderWidth, sliderHeight, new TextComponentTranslation("animatedcrosshair.config.save").getUnformattedText()));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if(button.displayString.equalsIgnoreCase(new TextComponentTranslation("animatedcrosshair.config.save").getUnformattedText())) {

            // Save the properties to the config if they're supposed to be applied
            if(AnimatedCrosshair.INSTANCE.config.getCurrentCrosshairName().equalsIgnoreCase(name)) {
                AnimatedCrosshair.INSTANCE.config.setCurrentProperties(properties);
            }

            // Save the properties to their properties file
            AnimatedCrosshair.INSTANCE.config.saveProperties(name, properties);

            Minecraft.getMinecraft().displayGuiScreen(new TechnicalGui(name));

        } else if(button.displayString.contains(new TextComponentTranslation("animatedcrosshair.color.chroma").getUnformattedText())) {
            // Swap the "Chroma Color" value
            properties.chromaColor = !properties.chromaColor;
            button.displayString = new TextComponentTranslation("animatedcrosshair.color.chroma").getUnformattedText() + ": " + (properties.chromaColor ? new TextComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new TextComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText());
        } else if(button.displayString.contains(new TextComponentTranslation("animatedcrosshair.properties.negativecolor").getUnformattedText())) {
            // Swap the "Negative Color" value
            properties.negativeColor = !properties.negativeColor;
            button.displayString = new TextComponentTranslation("animatedcrosshair.properties.negativecolor").getUnformattedText() + ": " + (properties.negativeColor ? new TextComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new TextComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText());
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if(crosshairFrameThread != null) crosshairFrameThread.interrupt();
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
                properties.colorModifier = new Color((int) value, properties.colorModifier.getGreen(), properties.colorModifier.getBlue());
            } else if(buttonList.get(id).displayString.contains(new TextComponentTranslation("animatedcrosshair.color.green").getUnformattedText())) {
                properties.colorModifier = new Color(properties.colorModifier.getRed(), (int) value, properties.colorModifier.getBlue());
            } else if(buttonList.get(id).displayString.contains(new TextComponentTranslation("animatedcrosshair.color.blue").getUnformattedText())) {
                properties.colorModifier = new Color(properties.colorModifier.getRed(), properties.colorModifier.getGreen(), (int) value);
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
