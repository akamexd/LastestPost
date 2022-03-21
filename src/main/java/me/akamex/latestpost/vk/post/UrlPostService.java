package me.akamex.latestpost.vk.post;

import me.luckkyyz.luckapi.config.SettingConfig;
import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class UrlPostService implements PostService {

    /**
     * Время перерасчета последнего поста в миллисекундах (по дефолту 30 сек)
     */
    private final long updateTime;

    private String accessToken;
    private boolean useDomain = true;
    private String domain;
    private int id;

    private long lastTime = 0;
    private Post latestPost;

    public UrlPostService(SettingConfig config) {
        this(config, 30 * 1000);
    }

    public UrlPostService(SettingConfig config, long updateTime) {
        this.updateTime = updateTime;
        reinitialize(config);
    }

    @Override
    public void reinitialize(SettingConfig config) {
        ConfigurationSection vkGroupSection = config.getSection("vkGroup");
        accessToken = vkGroupSection.getString("accessToken");
        id = -vkGroupSection.getInt("id", 1);
        domain = vkGroupSection.getString("domain");
        if(domain == null || domain.equals("null")) {
            useDomain = false;
        }
    }

    @Override
    public CompletableFuture<Post> getLatestPost() {
        if(latestPost == null || (lastTime + updateTime) >= System.currentTimeMillis()) {
            return getForceLatestPost().thenApply(value -> {
                lastTime = System.currentTimeMillis();
                latestPost = value;
                return latestPost;
            });
        }

        return CompletableFuture.completedFuture(latestPost);
    }

    @Override
    public CompletableFuture<Post> getForceLatestPost() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = buildRequest();
                try(InputStream stream = url.openStream()) {
                    String response = IOUtils.toString(stream, StandardCharsets.UTF_8);
                    long biggestDate = 0;
                    int id = 0;
                    String text = null;

                    JSONObject jsonObject = new JSONObject(response).getJSONObject("response");
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    for(int i = 0; i < jsonArray.length(); i++) {
                        JSONObject post = jsonArray.getJSONObject(i);
                        long date = post.getLong("date");

                        if(date > biggestDate) {
                            biggestDate = date;
                            text = post.getString("text");
                            id = post.getInt("id");
                        }
                    }

                    if(text == null) {
                        return new Post(0, 0, "");
                    }

                    return new Post(id, biggestDate, text);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new Post(0, 0, "");
            }
        });
    }

    private static final int POST_COUNT = 5;
    private static final String VK_VERSION = "5.131";

    private URL buildRequest() throws MalformedURLException {
        if(accessToken == null) {
            throw new IllegalArgumentException("Not specified access token in config.yml");
        }

        if(useDomain) {
            return new URL("https://api.vk.com/method/wall.get?" +
                    "domain=" + domain +
                    "&access_token=" + accessToken +
                    "&count=" + POST_COUNT +
                    "&v=" + VK_VERSION);
        }
        if(id >= 0) {
            throw new IllegalArgumentException("Illegal group id. It can be less 0 only!");
        }

        return new URL("https://api.vk.com/method/wall.get?" +
                "owner_id=" + id +
                "&access_token=" + accessToken +
                "&count=" + POST_COUNT +
                "&v=" + VK_VERSION);
    }
}
