package me.akamex.latestpost.user;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserService {

    CompletableFuture<User> getUser(UUID uuid);

    default CompletableFuture<User> getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    void invalidateUser(UUID uuid);

    default void invalidateUser(Player player) {
        invalidateUser(player.getUniqueId());
    }

    default void invalidateUser(User user) {
        invalidateUser(user.getUuid());
    }

}
