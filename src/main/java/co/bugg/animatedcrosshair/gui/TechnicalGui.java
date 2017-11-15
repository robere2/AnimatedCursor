package co.bugg.animatedcrosshair.gui;

import co.bugg.animatedcrosshair.AnimatedCrosshair;
import co.bugg.animatedcrosshair.Reference;
import co.bugg.animatedcrosshair.config.ConfigUtil;
import co.bugg.animatedcrosshair.config.Properties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.util.ChatComponentTranslation;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * GUI to customize specific properties of a crosshair
 */
public class TechnicalGui extends GuiScreen {

    int sliderWidth = 150;
    int sliderHeight = 20;
    int sliderMargin = 5;

    /**
     * Crosshair name we're customizing
     */
    public String name;
    /**
     * @see Properties
     */
    public boolean negativeColor;
    /**
     * @see Properties
     */
    public float scale;
    /**
     * @see Properties
     */
    public float frameRate;
    /**
     * @see Properties
     */
    public int frameCount;

    public TechnicalGui(String name) {
        super();
        this.name = name;

        Properties properties;
        try {
            properties = ConfigUtil.getProperties(name);

            frameCount = properties.frameCount;
            scale = properties.crosshairScale;
            frameRate = properties.frameRate;
            negativeColor = properties.negativeColor;
        } catch (IOException e) {
            e.printStackTrace();
            Minecraft.getMinecraft().displayGuiScreen(null);
            AnimatedCrosshair.INSTANCE.messageBuffer.add(AnimatedCrosshair.INSTANCE.messageBuffer.format("For some reason I can't access your properties file" +
                    "right now, so you can't edit it! Either edit it with a text editor in .minecraft/" + ConfigUtil.assetsRoot + " or contact @bugfroggy, giving him" +
                    "your Minecraft logs to report this issue."));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(fontRendererObj, Reference.MOD_NAME + " " + new ChatComponentTranslation("animatedcrosshair.config.configuration").getUnformattedText(), width / 2, height / 2 - (sliderMargin + sliderHeight) * 3, 0xFFFFFF);
        drawCenteredString(fontRendererObj, AnimatedCrosshair.INSTANCE.credits, width / 2, height - 10, 0xFFFFFF);
        drawCenteredString(fontRendererObj, new ChatComponentTranslation("animatedcrosshair.config.precisevalues").getUnformattedText(), width / 2, (int) (height / 2 + (sliderMargin + sliderHeight) * 1.6), 0xFFFFFF);
        drawCenteredString(fontRendererObj, ".minecraft/" + ConfigUtil.assetsRoot, width / 2, (int) (height / 2 + (sliderMargin + sliderHeight) * 2), 0xFFFFFF);
    }

    @Override
    public void initGui() {
        super.initGui();

        int buttonId = 0;

        TechnicalGuiResponder responder = new TechnicalGuiResponder();
        TechnicalGuiFormatHelper formatHelper = new TechnicalGuiFormatHelper();

        buttonList.add(new GuiSlider(responder, buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), new ChatComponentTranslation("animatedcrosshair.properties.scale").getUnformattedText(), 0.1F, 10.0F, scale, formatHelper));
        buttonId++;
        buttonList.add(new GuiSlider(responder, buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), new ChatComponentTranslation("animatedcrosshair.properties.framerate").getUnformattedText(), 0F, 100F, frameRate, formatHelper));
        buttonId++;
        buttonList.add(new GuiSlider(responder, buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), new ChatComponentTranslation("animatedcrosshair.properties.framecount").getUnformattedText(), 1F, 256F, frameCount, formatHelper));
        buttonId++;
        buttonList.add(new GuiButton(buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 2), sliderWidth, sliderHeight, new ChatComponentTranslation("animatedcrosshair.properties.negativecolor").getUnformattedText() + ": " + (negativeColor ? new ChatComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new ChatComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText())));
        buttonId++;
        buttonList.add(new GuiButton(buttonId, width / 2 - sliderWidth / 2, height / 2 - sliderHeight / 2 + (sliderHeight + sliderMargin) * (buttonId - 1), sliderWidth, sliderHeight, new ChatComponentTranslation("animatedcrosshair.config.save").getUnformattedText()));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if(button.displayString.equalsIgnoreCase(new ChatComponentTranslation("animatedcrosshair.config.save").getUnformattedText())) {
            // Convert the properties into an object
            Properties properties = new Properties();
            properties.negativeColor = negativeColor;
            properties.frameRate = frameRate;
            properties.frameCount = frameCount;
            properties.crosshairScale = scale;

            // Save the properties to the config if they're supposed to be applied
            if(AnimatedCrosshair.INSTANCE.config.getCurrentCrosshairName().equalsIgnoreCase(name)) {
                AnimatedCrosshair.INSTANCE.config.setCurrentProperties(properties);
            }

            // Save the properties to their properties file
            AnimatedCrosshair.INSTANCE.config.saveProperties(name, properties);

            Minecraft.getMinecraft().displayGuiScreen(new ConfigGui(name));

        } else if(button.displayString.contains(new ChatComponentTranslation("animatedcrosshair.properties.negativecolor").getUnformattedText())) {
            // Swap the "Negative Color" value
            negativeColor = !negativeColor;
            button.displayString = new ChatComponentTranslation("animatedcrosshair.properties.negativecolor").getUnformattedText() + ": " + (negativeColor ? new ChatComponentTranslation("animatedcrosshair.config.enabled").getUnformattedText() : new ChatComponentTranslation("animatedcrosshair.config.disabled").getUnformattedText());
        }
    }

    public class TechnicalGuiFormatHelper implements GuiSlider.FormatHelper {
        final String scale = new ChatComponentTranslation("animatedcrosshair.properties.scale").getUnformattedText();
        final String framerate = new ChatComponentTranslation("animatedcrosshair.properties.framerate").getUnformattedText();
        final String framecount = new ChatComponentTranslation("animatedcrosshair.properties.framecount").getUnformattedText();

        /**
         * Text that should be displayed on the slider
         * @param id ID of the slider
         * @param name Name of the slider
         * @param value Value of the slider
         * @return String to display
         */
        @Override
        public String getText(int id, String name, float value) {

            if(name.equalsIgnoreCase(scale)) {
                return name + ": " + new DecimalFormat("#.##").format(value);
            } else if(name.equalsIgnoreCase(framerate)) {
                return name + ": " + new DecimalFormat(value < 10 ? "#.#" : "#").format(value);
            } else if(name.equalsIgnoreCase(framecount)) {
                return name + ": " + (int) value;
            } else {
                return name + ": " + new DecimalFormat("#.##").format(value);
            }
        }
    }

    public class TechnicalGuiResponder implements GuiPageButtonList.GuiResponder {
        /**
         * Called every time the value of a boolean button changes
         * Unused in this Minecraft mod
         * @param p_175321_1_ ID of the button
         * @param p_175321_2_ Value of the button
         */
        @Override
        public void func_175321_a(int p_175321_1_, boolean p_175321_2_) {
        }

        /**
         * Called every tick that the mouse button is down on a slider
         * @param id ID of the slider/button
         * @param value value of the slider/button
         */
        @Override
        public void onTick(int id, float value) {
            if(buttonList.get(id).displayString.contains(new ChatComponentTranslation("animatedcrosshair.properties.scale").getUnformattedText())) {
                scale = value;
            } else if(buttonList.get(id).displayString.contains(new ChatComponentTranslation("animatedcrosshair.properties.framerate").getUnformattedText())) {
                frameRate = value;
            } else if(buttonList.get(id).displayString.contains(new ChatComponentTranslation("animatedcrosshair.properties.framecount").getUnformattedText())) {
                frameCount = (int) value;
            }
        }

        /**
         * Called every time the value of a text box changes
         * Unused in this Minecraft mod
         * @param p_175319_1_ ID of the text box
         * @param p_175319_2_ Value of the text box
         */
        @Override
        public void func_175319_a(int p_175319_1_, String p_175319_2_) {
        }
    }
}
