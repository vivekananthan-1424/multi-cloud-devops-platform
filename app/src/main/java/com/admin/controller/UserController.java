package com.admin.controller;

import com.admin.dto.UserDto;
import com.admin.model.Role;
import com.admin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ─── List Users ───────────────────────────────────────────────────────────

    @GetMapping
    public String listUsers(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("users", userService.searchUsers(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("users", userService.getAllUsers());
        }
        return "users/list";
    }

    // ─── Create User ──────────────────────────────────────────────────────────

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("roles", Role.values());
        return "users/form";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@Valid @ModelAttribute UserDto userDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        try {
            userService.createUser(userDto);
            redirectAttrs.addFlashAttribute("successMessage", "User created successfully!");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    // ─── Edit User ────────────────────────────────────────────────────────────

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String showEditForm(@PathVariable Long id, Model model) {
        var user = userService.getUserById(id);
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        model.addAttribute("userDto", dto);
        model.addAttribute("roles", Role.values());
        model.addAttribute("editMode", true);
        return "users/form";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute UserDto userDto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("editMode", true);
            return "users/form";
        }
        try {
            userService.updateUser(id, userDto);
            redirectAttrs.addFlashAttribute("successMessage", "User updated successfully!");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    // ─── Delete User ──────────────────────────────────────────────────────────

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            userService.deleteUser(id);
            redirectAttrs.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    // ─── Toggle Status ────────────────────────────────────────────────────────

    @PostMapping("/toggle/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            userService.toggleUserStatus(id);
            redirectAttrs.addFlashAttribute("successMessage", "User status updated!");
        } catch (RuntimeException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }
}
