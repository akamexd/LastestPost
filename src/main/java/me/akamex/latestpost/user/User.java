package me.akamex.latestpost.user;

import java.util.UUID;

public interface User {

    UUID getUuid();

    int getPostId();

    void setPostId(int id);

}
