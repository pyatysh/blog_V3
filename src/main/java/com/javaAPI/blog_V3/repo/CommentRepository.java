package com.javaAPI.blog_V3.repo;

import com.javaAPI.blog_V3.models.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Long> {
}
