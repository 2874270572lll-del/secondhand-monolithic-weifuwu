package com.zjgsu.lll.secondhand.controller;

import com.zjgsu.lll.secondhand.common.Result;
import com.zjgsu.lll.secondhand.entity.User;
import com.zjgsu.lll.secondhand.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<List<User>> getAllUsers() {
        return Result.success(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        return Result.success(userService.getUserByUsername(username));
    }

    @PostMapping
    public Result<User> createUser(@Valid @RequestBody User user) {
        return Result.success(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        return Result.success(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        return Result.success(userService.login(username, password));
    }
}
