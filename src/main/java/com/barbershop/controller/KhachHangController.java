package com.barbershop.controller;

import com.barbershop.entity.KhachHang;
import com.barbershop.repository.KhachHangRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/khachhang")
public class KhachHangController {

    @Autowired
    private KhachHangRepository khachHangRepo;

    // ===================== DANH SÁCH + TÌM KIẾM =====================
    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        List<KhachHang> list;

        if (keyword != null && !keyword.trim().isEmpty()) {
            list = khachHangRepo.search(keyword.trim());
        } else {
            list = khachHangRepo.findAll();
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("listKhachHang", list);

        return "khachhang-list";
    }

    // ===================== FORM THÊM =====================
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        model.addAttribute("khachHang", new KhachHang());
        return "khachhang-add";
    }

    // ===================== XỬ LÝ THÊM =====================
    @PostMapping("/add")
    public String add(@ModelAttribute KhachHang kh) {
        khachHangRepo.save(kh);
        return "redirect:/admin/khachhang";
    }

    // ===================== FORM SỬA =====================
    @GetMapping("/edit/{makh}")
    public String editForm(@PathVariable("makh") int makh, Model model, HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        KhachHang kh = khachHangRepo.findById(makh).orElse(null);
        model.addAttribute("khachHang", kh);

        return "khachhang-edit";
    }

    // ===================== XỬ LÝ SỬA =====================
    @PostMapping("/edit")
    public String edit(@ModelAttribute KhachHang kh) {
        khachHangRepo.save(kh);
        return "redirect:/admin/khachhang";
    }

    // ===================== XÓA =====================
    @GetMapping("/delete/{makh}")
    public String delete(@PathVariable("makh") int makh) {
        khachHangRepo.deleteById(makh);
        return "redirect:/admin/khachhang";
    }
}
