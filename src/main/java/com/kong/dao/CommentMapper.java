package com.kong.dao;

import com.kong.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 通过实体类型查询每页评论
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    /**
     * 查询评论总数
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCountByEntity(int entityType, int entityId);

    /**
     * 新增评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);

    /**
     * 根据id查询评论
     * @param id
     * @return
     */
    Comment selectCommentById(int id);
}
