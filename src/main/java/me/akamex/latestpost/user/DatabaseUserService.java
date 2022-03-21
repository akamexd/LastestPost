package me.akamex.latestpost.user;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.luckkyyz.luckapi.database.QueryExecutors;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DatabaseUserService implements UserService {

    private QueryExecutors executors;

    private final LoadingCache<UUID, CompletableFuture<User>> cache;

    public DatabaseUserService(QueryExecutors executors) {
        this(executors, 30, 30, TimeUnit.SECONDS);
    }

    public DatabaseUserService(QueryExecutors executors, int initialCapacity, long duration, TimeUnit unit) {
        reinitialize(executors);

        DatabaseUserService userService = this;
        cache = CacheBuilder.newBuilder()
                .initialCapacity(initialCapacity)
                .expireAfterWrite(duration, unit)
                .expireAfterAccess(duration, unit)
                .build(new CacheLoader<UUID, CompletableFuture<User>>() {
                    @Override
                    public CompletableFuture<User> load(UUID uuid) {
                        CompletableFuture<User> future = new CompletableFuture<>();
                        executors.async().result("SELECT * FROM latestPosts WHERE uuid = ?", result -> {
                            if(result.next()) {
                                int id = result.getInt("id");
                                future.complete(new UserImpl(userService, uuid, id));
                                return;
                            }
                            future.complete(new UserImpl(userService, uuid, 0));
                        }, uuid.toString());
                        return future;
                    }
                });

        executors.sync().update("CREATE TABLE IF NOT EXISTS `latestPosts` (" +
                "`uuid` VARCHAR(36) NOT NULL PRIMARY KEY, " +
                "`id` BIGINT NOT NULL" +
                ")");


    }

    public void reinitialize(QueryExecutors executors) {
        this.executors = executors;
    }

    void setUserId(User user) {
        executors.async().update("INSERT INTO latestPosts VALUES (?, ?) ON DUPLICATE KEY UPDATE id = ?", user.getUuid().toString(), user.getPostId(), user.getPostId());
    }

    @Override
    public CompletableFuture<User> getUser(UUID uuid) {
        try {
            return cache.get(uuid);
        } catch (ExecutionException exception) {
            exception.printStackTrace();
            return CompletableFuture.completedFuture(new UserImpl(this, uuid, 0));
        }
    }

    @Override
    public void invalidateUser(UUID uuid) {
        cache.invalidate(uuid);
    }
}
