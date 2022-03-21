package me.akamex.latestpost.vk.post;

public class Post {

    private final int id;
    private final long date;
    private final String text;

    public Post(int id, long date, String text) {
        this.id = id;
        this.date = date;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public String getText() {
        return text;
    }
}
