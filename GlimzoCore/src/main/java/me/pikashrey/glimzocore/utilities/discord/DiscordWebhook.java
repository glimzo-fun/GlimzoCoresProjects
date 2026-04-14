package me.pikashrey.glimzocore.utilities.discord;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    private final String webhookUrl;
    private String content  = "";
    private String username = "GlimzoCore";
    private String avatarUrl = null;
    private String embedTitle = null;
    private String embedDescription = null;
    private int    embedColor = 0x5865F2; // Discord blurple

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public DiscordWebhook setContent(String content) {
        this.content = content != null ? content : "";
        return this;
    }

    public DiscordWebhook setUsername(String username) {
        this.username = username;
        return this;
    }

    public DiscordWebhook setAvatarUrl(String url) {
        this.avatarUrl = url;
        return this;
    }

    public DiscordWebhook setEmbed(String title, String description, int color) {
        this.embedTitle       = title;
        this.embedDescription = description;
        this.embedColor       = color;
        return this;
    }

    public boolean send() {
        if (webhookUrl == null || webhookUrl.isEmpty()) return false;
        try {
            String json = buildJson();
            URL url = new URL(webhookUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("User-Agent", "GlimzoCore/1.0");
            con.setDoOutput(true);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            con.disconnect();
            return code >= 200 && code < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private String buildJson() {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"username\":\"").append(escape(username)).append("\"");
        if (avatarUrl != null)
            sb.append(",\"avatar_url\":\"").append(escape(avatarUrl)).append("\"");
        if (!content.isEmpty())
            sb.append(",\"content\":\"").append(escape(content)).append("\"");
        if (embedTitle != null || embedDescription != null) {
            sb.append(",\"embeds\":[{");
            if (embedTitle != null)
                sb.append("\"title\":\"").append(escape(embedTitle)).append("\",");
            if (embedDescription != null)
                sb.append("\"description\":\"").append(escape(embedDescription)).append("\",");
            sb.append("\"color\":").append(embedColor);
            sb.append("}]");
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }
}

