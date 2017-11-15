package co.bugg.animatedcrosshair;

import co.bugg.animatedcrosshair.http.Response;
import co.bugg.animatedcrosshair.http.WebRequests;

/**
 * Factory that creates various threads used by the mod
 */
public class ThreadFactory {

    /**
     * Class is not instantiatable
     */
    private ThreadFactory() {
        throw new AssertionError();
    }

    /**
     * Creates a thread that will send a "ping" request every interval milliseconds
     * @param interval number of milliseconds between requests
     * @return Thread
     */
    public static Thread createPingThread(int interval) {
        return new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    String ip = AnimatedCrosshair.INSTANCE.getCurrentIP();

                    Response response = WebRequests.pingRequest(ip);
                    WebRequests.basicResponseHandler(response);

                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /**
     * Creates a thread that will increment the current
     * frame number according to the framerate
     * @return Thread
     */
    public static Thread createFramerateThread() {
        return new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    // Only run if the mod is enabled.
                    if(AnimatedCrosshair.INSTANCE.enabled) {
                        // Reset the frame counter if necessary
                        if (AnimatedCrosshair.INSTANCE.frame < AnimatedCrosshair.INSTANCE.config.getCurrentProperties().frameCount - 1) {
                            AnimatedCrosshair.INSTANCE.frame++;
                        } else {
                            AnimatedCrosshair.INSTANCE.frame = 0;
                        }
                    }

                    // Sleep until the next frame needs to be rendered
                    Thread.sleep((int) (1000.0F / AnimatedCrosshair.INSTANCE.config.getCurrentProperties().frameRate));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
}
