package com.phegondev.usersmanagementsystem.controller;

import com.phegondev.usersmanagementsystem.dto.ReqRes;
import com.phegondev.usersmanagementsystem.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public ReqRes createComment(@RequestBody ReqRes request) {
        return commentService.createComment(request);
    }

    @GetMapping("/task/{taskId}")
    public ReqRes getCommentsByTaskId(@PathVariable Integer taskId) {
        return commentService.getCommentsByTaskId(taskId);
    }

    @PutMapping("/{commentId}")
    public ReqRes updateComment(@PathVariable Integer commentId, @RequestBody ReqRes request) {
        return commentService.updateComment(commentId, request);
    }

    @DeleteMapping("/{commentId}")
    public ReqRes deleteComment(@PathVariable Integer commentId) {
        return commentService.deleteComment(commentId);
    }
}
