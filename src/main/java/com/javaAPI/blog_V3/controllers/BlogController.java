package com.javaAPI.blog_V3.controllers;

import com.javaAPI.blog_V3.models.Comment;
import com.javaAPI.blog_V3.models.Image;
import com.javaAPI.blog_V3.models.Post;
import com.javaAPI.blog_V3.models.User;
import com.javaAPI.blog_V3.service.BlogService;
import com.javaAPI.blog_V3.service.CommentService;
import com.javaAPI.blog_V3.service.ImageService;
import com.javaAPI.blog_V3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private UserService userService;

    @GetMapping("/blog")
    public String blogMain(Model model){
        Iterable<Post> posts = blogService.findAll();
        model.addAttribute("posts", posts);
        return "blog-main";
    }

    @GetMapping("/blog/add")
    public String blogAdd(Model model){
        return "blog-add";
    }

//    @PostMapping("/blog/add")
//    public String blogPostAdd(@RequestParam String title, @RequestParam String anons, @RequestParam String fullTextPost, Model model){
//        ArrayList<Comment> comments = new ArrayList<>();
//        Post post = new Post(title, anons, fullTextPost, comments);
//        blogService.blogPostSave(post);
//        return "redirect:/blog";
//    }

    @PostMapping("/blog/add")
    public String blogPostAdd(HttpServletRequest request,
                              @RequestParam String title,
                              @RequestParam String anons,
                              @RequestParam String fullTextPost,
                              Model model){

        String username = request.getSession().getAttribute("user").toString();
        User author = userService.userFindUsername(username);

        ArrayList<Comment> comments = new ArrayList<>();

        Post post = new Post(title, anons, fullTextPost, comments, author);
        blogService.blogPostSave(post);
        return "redirect:/blog";
    }

    @GetMapping("/blog/{id}")
    public String blogPostDetails(@PathVariable(value = "id") long id, Model model){
        Optional<Post> postContainer = blogService.blogPostFind(id);
        if(postContainer.isEmpty()){
            return "redirect:/blog";
        }
        Post post = postContainer.get();
        model.addAttribute("post", post);
        return "blog-details";
    }

    @GetMapping("/blog/{id}/edit")
    public String blogPostEdit(@PathVariable(value = "id") long id, Model model){
        Optional<Post> postContainer = blogService.blogPostFind(id);
        if(postContainer.isEmpty()){
            return "redirect:/blog";
        }
        Post post = postContainer.get();
        model.addAttribute("post", post);
        return "blog-edit";
    }

    @PostMapping("/blog/{id}/edit")
    public String blogPostUpdate(@PathVariable(value = "id") long id, @RequestParam String title, @RequestParam String anons, @RequestParam String fullTextPost, Model model){
        Post post = blogService.blogPostFind(id).orElseThrow();
        post.setTitle(title);
        post.setAnons(anons);
        post.setFullTextPost(fullTextPost);
        blogService.blogPostSave(post);

        return "redirect:/blog";
    }

    @PostMapping("/blog/{id}/remove")
    public String blogPostDelete(@PathVariable(value = "id") long id, Model model){
        Post post = blogService.blogPostFind(id).orElseThrow();
        blogService.blogPostDelete(post);
        return "redirect:/blog";

    }

    @PostMapping("blog/{postId}/comments")
    public String blogPostAddCom(@PathVariable(value = "postId") long postId,
                                 @RequestPart String fullTextCom,
                                 @RequestPart("file") MultipartFile file,
                                 Model model) throws IOException {

        Optional<Post> postContainer = blogService.blogPostFind(postId);
        if(postContainer.isEmpty()){
            return "blog-details";
        }

        Comment comment = new Comment();
        comment.setFullTextCom(fullTextCom);

        if (!file.isEmpty()) {
            Image imageInBytes = imageService.uploadImg(file);
            comment.setImgToComment(Collections.singletonList(imageInBytes));
        }
            commentService.blogComSave(comment);
            Post post = postContainer.get();
            List<Comment> blogComments = post.getComments();
            blogComments.add(comment);
            blogService.blogPostSave(post);

        return "redirect:/blog";

    }

    @GetMapping("/blog/{postId}/comments")
    public String blogShowCom(@PathVariable(value = "postId") long postId,
                              Model model) throws IOException {

        Optional<Post> postContainer = blogService.blogPostFind(postId);
        if(postContainer.isEmpty()){
            return "blog-details";
        }

        model.addAttribute("post", postContainer.get());

        return "redirect:/blog";

    }

    @PostMapping("/blog/search")
    public String filterPost(@RequestParam String searchWords, Model model){

//    @PostMapping("/blog/search?{searchWords}")
//    public String filterPost(@PathVariable(value = "searchWords") String searchWords, Model model){

        Iterable<Post> foundPosts;
        if (searchWords != null && !searchWords.isEmpty()) {
            foundPosts = blogService.postsSearch(searchWords);
        } else {
            foundPosts = blogService.findAll();
        }
        model.addAttribute("foundPosts", foundPosts);
        model.addAttribute("searchWords", searchWords);

        return "blog-main";
    }

    @PostMapping("/saveImageForXXE")
    public String uploadImageXEE(@RequestParam("imageFile") MultipartFile imageFile) throws Exception {
        String returnValue = "home";
        imageService.saveImageXXE(imageFile);
        return "redirect:/";
    }

}
