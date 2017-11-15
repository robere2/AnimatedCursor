package co.bugg.animatedcrosshair.http;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Template class for responses from https://aws.bugg.co/mods/animatedcrosshair
 */
public class Response implements Serializable {
    public boolean ok;
    public ArrayList<HashMap<Action, String>> actions = new ArrayList<>();

    public static Response fromJson(String json) {
        Gson gson = new Gson();

        return gson.fromJson(json, Response.class);
    }

    public Response(boolean ok) {
        this.ok = ok;
    }

    public Response(boolean ok, ArrayList<HashMap<Action, String>> actions) {
        this(ok);
        this.actions = actions;
    }

    public enum Action implements Serializable {
        SHUTDOWN,
        SEND_MESSAGE,
        SYSTEM_OUT,
        SET_PING_INTERVAL
    }
}