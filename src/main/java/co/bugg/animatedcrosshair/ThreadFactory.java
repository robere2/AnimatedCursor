package co.bugg.animatedcrosshair;

import co.bugg.animatedcrosshair.config.Properties;
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
     * Creates a thread that will send a "ping" request according to AnimatedCrosshair#pingInterval
     * @return Thread
     */
    public static Thread createPingThread() {
        return new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    String ip = AnimatedCrosshair.INSTANCE.getCurrentIP();

                    Response response = WebRequests.pingRequest(ip);
                    WebRequests.basicResponseHandler(response);

                    Thread.sleep(AnimatedCrosshair.INSTANCE.pingInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /**
     * Creates a thread that will increment the current
     * frame number according to the framerate
     * @param properties Properties file to increment and base timing off of
     * @return Thread
     */
    public static Thread createFramerateThread(Properties properties) {
        return createBaseThread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    // Reset the frame counter if necessary
                    if (properties.frame < properties.frameCount - 1) {
                        properties.frame++;
                    } else {
                        properties.frame = 0;
                    }


                    // Sleep until the next frame needs to be rendered
                    Thread.sleep((int) (1000.0F / properties.frameRate));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /**
     * Basic thread builder, which holds universal modifications
     * to our threads
     * @param run Runnable that the thread runs
     * @return A new thread
     */
    private static Thread createBaseThread(Runnable run) {
        return new Thread() {
            @Override
            public void run() {
                super.run();
                run.run();
            }


        };
    }
}
