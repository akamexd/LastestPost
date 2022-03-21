package me.akamex.latestpost.user;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.UUID;

class UserImpl implements User {

    private final UUID uuid;
    /**
     * Айди последнего сообщения
     */
    private int id;

    private final DatabaseUserService userService;

    UserImpl(DatabaseUserService userService, UUID uuid, int id) {
        this.userService = userService;
        this.uuid = uuid;
        this.id = id;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public int getPostId() {
        return id;
    }

    @Override
    public void setPostId(int id) {
        if(this.id == id) {
            return;
        }

        this.id = id;
        userService.setUserId(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserImpl user = (UserImpl) o;
        return new EqualsBuilder()
                .append(id, user.id)
                .append(uuid, user.uuid)
                .append(userService, user.userService)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(uuid)
                .append(id)
                .append(userService)
                .toHashCode();
    }
}
