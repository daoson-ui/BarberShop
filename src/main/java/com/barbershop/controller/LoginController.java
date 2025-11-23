package com.barbershop.controller;

import com.barbershop.entity.Account;
import com.barbershop.repository.AccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private AccountRepository accountRepository;

    // Hiển thị trang login
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Xử lý đăng nhập
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {

        Account acc = accountRepository.findByUsername(username);

        if (acc == null || !acc.getPassword().equals(password)) {
            return "redirect:/login?error";
        }

        // Lưu thông tin user lên session
        session.setAttribute("user", acc);

        // Chuyển hướng theo role
        if (acc.getRole().equals("ROLE_ADMIN")) {
            return "redirect:/admin/home";
        } else {
            return "redirect:/user/home";
        }
    }

    // Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // xóa session
        return "redirect:/login";
    }
}
