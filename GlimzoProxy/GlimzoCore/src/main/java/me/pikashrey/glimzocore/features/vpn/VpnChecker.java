package me.pikashrey.glimzocore.features.vpn;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.pikashrey.glimzocore.GlimzoCore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Checks whether an IP belongs to a VPN, proxy, or hosting provider
 * via ip-api.com (free, no key, 45 req/min).
 *
 * Always called from an async thread (AsyncPlayerPreLoginEvent).
 * Fails open on timeout or API error so legitimate players are never
 * blocked by an external service going down.
 */
public class VpnChecker {

    public enum Result { CLEAN, VPN_OR_PROXY, API_ERROR }

    private final GlimzoCore plugin;

    public VpnChecker(GlimzoCore plugin) {
        this.plugin = plugin;
    }

    public Result check(String ip) {
        if (!plugin.getConfig().getBoolean("vpn.enabled", true)) return Result.CLEAN;

        if (isLocal(ip)) return Result.CLEAN;

        int timeout = plugin.getConfig().getInt("vpn.timeout-ms", 3000);

        try {
            URL url = new URL("http://ip-api.com/json/" + ip + "?fields=status,proxy,hosting");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "GlimzoCore/1.0 (Minecraft Server Plugin)");

            if (conn.getResponseCode() != 200) {
                plugin.log("&e[VPN] ip-api.com returned HTTP " + conn.getResponseCode() + " for " + ip + " - fail open.");
                return Result.API_ERROR;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
            }

            JsonObject json = new JsonParser().parse(sb.toString()).getAsJsonObject();
            if (!"success".equals(json.has("status") ? json.get("status").getAsString() : "fail"))
                return Result.CLEAN; // reserved/private range

            boolean proxy   = json.has("proxy")   && json.get("proxy").getAsBoolean();
            boolean hosting = json.has("hosting")  && json.get("hosting").getAsBoolean();
            return (proxy || hosting) ? Result.VPN_OR_PROXY : Result.CLEAN;

        } catch (Exception e) {
            plugin.log("&e[VPN] Check failed for " + ip + ": " + e.getMessage() + " - fail open.");
            return Result.API_ERROR;
        }
    }

    private boolean isLocal(String ip) {
        return ip.startsWith("127.") || ip.startsWith("10.") || ip.startsWith("192.168.")
            || (ip.startsWith("172.") && isPrivate172(ip))
            || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
    }

    private boolean isPrivate172(String ip) {
        try {
            int second = Integer.parseInt(ip.split("\\.")[1]);
            return second >= 16 && second <= 31;
        } catch (Exception e) { return false; }
    }
}
