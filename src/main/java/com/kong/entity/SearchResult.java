package com.kong.entity;

import java.util.List;

/**
 * 自定义实体
 * 用于暂存es中查询到的列表和总行数
 */
public class SearchResult {
    private List<DiscussPost> posts;
    private long total;

    public SearchResult(List<DiscussPost> posts, long total) {
        this.posts = posts;
        this.total = total;
    }

    public List<DiscussPost> getPosts() {
        return posts;
    }

    public void setPosts(List<DiscussPost> posts) {
        this.posts = posts;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
