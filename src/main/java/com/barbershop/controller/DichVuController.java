package com.barbershop.controller;

import com.barbershop.entity.DichVu;
import com.barbershop.repository.DichVuRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/dichvu")
public class DichVuController {

    @Autowired
    private DichVuRepository dichVuRepo;

    // ===================== DANH SÁCH =====================
    @GetMapping
    public String list(Model model, HttpSession session) {
        if (session.getAttribute("user") == null)
            return "redirect:/login";

        List<DichVu> list = dichVuRepo.findAll();
        model.addAttribute("listDichVu", list);

        return "dichvu-list";
    }

    // ===================== FORM THÊM =====================
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null)
            return "redirect:/login";

        model.addAttribute("dichVu", new DichVu());
        return "dichvu-add";
    }

    // ===================== XỬ LÝ THÊM =====================
    @PostMapping("/add")
    public String add(@ModelAttribute DichVu dv) {
        dichVuRepo.save(dv);
        return "redirect:/admin/dichvu";
    }

    // ===================== FORM SỬA =====================
    @GetMapping("/edit/{maDv}")
    public String editForm(@PathVariable("maDv") int maDv,
                           Model model,
                           HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        DichVu dv = dichVuRepo.findById(maDv).orElse(null);
        model.addAttribute("dichVu", dv);

        return "dichvu-edit";
    }

    // ===================== XỬ LÝ SỬA =====================
    @PostMapping("/edit")
    public String edit(@ModelAttribute DichVu dv) {
        dichVuRepo.save(dv);
        return "redirect:/admin/dichvu";
    }

    // ===================== XÓA =====================
    @GetMapping("/delete/{maDv}")
    public String delete(@PathVariable("maDv") int maDv) {
        dichVuRepo.deleteById(maDv);
        return "redirect:/admin/dichvu";
    }
}
