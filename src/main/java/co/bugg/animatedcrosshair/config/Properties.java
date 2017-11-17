package co.bugg.animatedcrosshair.config;

import java.awt.*;
import java.io.Serializable;

/**
 * Properties that a crosshair can have
 */
public class Properties implements Serializable {
    /**
     * Zero-indexed number of frames in the crosshair
     */
    public int frameCount;
    /**
     * Number of frames that should be rendered per second
     */
    public float frameRate;
    /**
     * Whether the color of the crosshair should go
     * negative on white backgrounds
     */
    public boolean negativeColor;
    /**
     * Scale of the crosshair
     */
    public float crosshairScale;
    /**
     * Color modifier that is applied when rendering the crosshair
     */
    public Color colorModifier;
    /**
     * TODO
     * Whether or not the crosshair should cycle through the
     * colors of the rainbow
     */
    public boolean chromaColor;
}
