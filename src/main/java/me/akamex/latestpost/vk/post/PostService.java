package me.akamex.latestpost.vk.post;

import me.luckkyyz.luckapi.config.SettingConfig;

import java.util.concurrent.CompletableFuture;

public interface PostService {

    void reinitialize(SettingConfig config);

    /**
     * @return возвращает последний кэшированный пост
     */
    CompletableFuture<Post> getLatestPost();

    /**
     * @return получает последнюю новость (без использования кэша, всегда чекает новую новость)
     */
    CompletableFuture<Post> getForceLatestPost();

}
