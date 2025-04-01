package com.phegondev.usersmanagementsystem.service;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.entity.Comment;
import com.phegondev.usersmanagementsystem.entity.OurUsers;
import com.phegondev.usersmanagementsystem.entity.Task;
import com.phegondev.usersmanagementsystem.repository.CommentRepo;
import com.phegondev.usersmanagementsystem.repository.TaskRepo;
import com.phegondev.usersmanagementsystem.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepo commentRepo;

    @Autowired
    private TaskRepo taskRepo;

    @Autowired
    private UsersRepo userRepo;

    public ReqRes createComment(ReqRes request) {
        ReqRes response = new ReqRes();
        try {
            Optional<OurUsers> userOpt = userRepo.findById(request.getCommentUserId());
            Optional<Task> taskOpt = taskRepo.findById(request.getCommentTaskId());

            if (userOpt.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("User not found");
                return response;
            }
            if (taskOpt.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("Task not found");
                return response;
            }

            Comment comment = new Comment();
            comment.setContent(request.getCommentContent());
            comment.setUser(userOpt.get());
            comment.setTask(taskOpt.get());

            Comment savedComment = commentRepo.save(comment);
            response.setCommentId(savedComment.getId());
            response.setCommentContent(savedComment.getContent());
            response.setCommentTimestamp(savedComment.getTimestamp());
            response.setStatusCode(200);
            response.setMessage("Comment created successfully");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error creating comment: " + e.getMessage());
        }
        return response;
    }

    public ReqRes getCommentsByTaskId(Integer taskId) {
        ReqRes response = new ReqRes();
        try {
            List<Comment> comments = commentRepo.findByTaskId(taskId);
            response.setCommentss(comments);
            response.setStatusCode(200);
            response.setMessage("Comments retrieved successfully");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error retrieving comments: " + e.getMessage());
        }
        return response;
    }

    public ReqRes updateComment(Integer commentId, ReqRes request) {
        ReqRes response = new ReqRes();
        try {
            Optional<Comment> commentOpt = commentRepo.findById(commentId);
            if (commentOpt.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("Comment not found");
                return response;
            }

            Comment comment = commentOpt.get();
            if (request.getCommentContent() != null) {
                comment.setContent(request.getCommentContent());
            }

            Comment updatedComment = commentRepo.save(comment);
            response.setCommentId(updatedComment.getId());
            response.setCommentContent(updatedComment.getContent());
            response.setCommentTimestamp(updatedComment.getTimestamp());
            response.setStatusCode(200);
            response.setMessage("Comment updated successfully");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error updating comment: " + e.getMessage());
        }
        return response;
    }

    public ReqRes deleteComment(Integer commentId) {
        ReqRes response = new ReqRes();
        try {
            Optional<Comment> commentOpt = commentRepo.findById(commentId);
            if (commentOpt.isEmpty()) {
                response.setStatusCode(404);
                response.setMessage("Comment not found");
                return response;
            }

            commentRepo.delete(commentOpt.get());
            response.setStatusCode(200);
            response.setMessage("Comment deleted successfully");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error deleting comment: " + e.getMessage());
        }
        return response;
    }
}
