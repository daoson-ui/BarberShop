package com.barbershop.controller;

import com.barbershop.entity.NhanVien;
import com.barbershop.repository.CaLamRepository;
import com.barbershop.repository.NhanVienRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/nhanvien")
public class NhanVienController {

    @Autowired
    private NhanVienRepository nhanVienRepo;

    @Autowired
    private CaLamRepository caLamRepo;

    // =================== DANH SÁCH + TÌM KIẾM ====================
    @GetMapping
    public String list(Model model,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       HttpSession session) {

        if (session.getAttribute("user") == null) return "redirect:/login";

        List<NhanVien> list;

        if (keyword != null && !keyword.trim().isEmpty()) {
            list = nhanVienRepo.search(keyword.trim());
        } else {
            list = nhanVienRepo.findAll();
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("listNhanVien", list);
        return "nhanvien-list";
    }

    // =================== FORM THÊM ====================
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        model.addAttribute("nhanVien", new NhanVien());
        model.addAttribute("listCaLam", caLamRepo.findAll());
        return "nhanvien-add";
    }

    // =================== XỬ LÝ THÊM ====================
    @PostMapping("/add")
    public String add(@ModelAttribute NhanVien nv) {
        nhanVienRepo.save(nv);
        return "redirect:/admin/nhanvien";
    }

    // =================== FORM SỬA ====================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable("id") int id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        NhanVien nv = nhanVienRepo.findById(id).orElse(null);

        model.addAttribute("nhanVien", nv);
        model.addAttribute("listCaLam", caLamRepo.findAll());
        return "nhanvien-edit";
    }

    // =================== XỬ LÝ SỬA ====================
    @PostMapping("/edit")
    public String edit(@ModelAttribute NhanVien nv) {
        nhanVienRepo.save(nv);
        return "redirect:/admin/nhanvien";
    }

    // =================== XÓA ====================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") int id) {
        nhanVienRepo.deleteById(id);
        return "redirect:/admin/nhanvien";
    }
}
