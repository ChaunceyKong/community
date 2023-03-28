package com.kong.service;

import com.alibaba.fastjson.JSONObject;
import com.kong.dao.elasticsearch.DiscussPostRepository;
import com.kong.entity.DiscussPost;
import com.kong.entity.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Resource
    private RestHighLevelClient restHighLevelClient;

    /**
     * 将贴子提交到ES服务器
     * @param post
     */
    public void saveDiscussPost(DiscussPost post) {
        discussPostRepository.save(post);
    }

    /**
     * 从服务器中删除帖子
     * @param id
     */
    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    /**
     *
     * @param keyword 搜索的关键字
     * @param current 当前页
     * @param limit 每页显示多少条数据
     * @return
     * @throws IOException
     */
    public SearchResult searchDiscussPost(String keyword, int current, int limit) throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>"); // 前标签
        highlightBuilder.postTags("</span>"); // 后标签

        // 构造搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // 在discusspost索引的title和content字段中查询 keyword
                .query(QueryBuilders.multiMatchQuery(keyword,"title","content"))
                // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value))
                // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 一个可选项，用于控制允许搜索的事件：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(current) // 指定从哪条开始查询
                .size(limit) // 需要查出的总记录条数
                .highlighter(highlightBuilder); // 高亮

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;

        searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        List<DiscussPost> list = new ArrayList<>();
        long total = searchResponse.getHits().getTotalHits().value;
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost post = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                // 可能匹配多段，只取第一段就行 ...[0]
                post.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                post.setContent(contentField.getFragments()[0].toString());
            }
            list.add(post);
        }

        return new SearchResult(list, total);
    }

}
