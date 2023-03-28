package com.kong.dao;

import com.kong.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    /**
     * 查询帖子
     * userId 在首页不需要使用，后面个人主页显示已发布帖子使用
     * sql动态拼接 userId
     * @param userId 用户id
     * @param offset 分页行号
     * @param limit 每页数量
     * @return 返回帖子集合
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    /**
     * 获取帖子总数
     * @param userId 用户id，用于动态sql
     * @return 返回行数
     */
    // @Param 注解用于给参数取别名
    // 如果该方法只有一个参数，并且在动态sql<if>里使用，则必须加该注解
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 新增帖子
     * @param discussPost
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 查看帖子详情
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 更新帖子评论数量
     * @param id
     * @param commentCount
     * @return
     */
    int updateCommentCount(int id, int commentCount);

    /**
     * 修改帖子类型
     */
    int updateType(int id, int type);

    /**
     * 修改帖子状态
     */
    int updateStatus(int id, int status);

    /**
     * 修改帖子分数
     */
    int updateScore(int id, double score);

}
















