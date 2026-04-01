package com.admin.controller;

import com.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Map<String, Long> stats = userService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("recentUsers", userService.getAllUsers()
                .stream().limit(5).toList());
        model.addAttribute("currentUser", principal.getName());
        return "dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
