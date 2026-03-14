package com.barbershop.controller;

import com.barbershop.entity.DichVu;
import com.barbershop.repository.DichVuRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/dichvu")
public class DichVuController {

    @Autowired
    private DichVuRepository dichVuRepo;

    private final String UPLOAD_DIR = "src/main/resources/static/images/dichvu/";

    // ===================== CHECK LOGIN =====================

    private boolean checkLogin(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    // ===================== DANH SÁCH DỊCH VỤ =====================

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Model model,
            HttpSession session) {

        if (!checkLogin(session)) {
            return "redirect:/login";
        }

        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }

        List<DichVu> list = dichVuRepo.search(keyword, minPrice, maxPrice);

        model.addAttribute("listDichVu", list);
        model.addAttribute("keyword", keyword);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "admin/dichvu-list";
    }

    // ===================== FORM THÊM =====================

    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {

        if (!checkLogin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("dichVu", new DichVu());

        return "admin/dichvu-add";
    }

    // ===================== XỬ LÝ THÊM =====================

    @PostMapping("/add")
    public String add(@ModelAttribute DichVu dv,
                      @RequestParam("file") MultipartFile file) {

        try {
            String fileName = uploadFile(file);
            if (fileName != null) {
                dv.setHinhAnh(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        dichVuRepo.save(dv);

        return "redirect:/admin/dichvu";
    }

    // ===================== FORM SỬA =====================

    @GetMapping("/edit/{maDv}")
    public String editForm(@PathVariable int maDv,
                           Model model,
                           HttpSession session) {

        if (!checkLogin(session)) {
            return "redirect:/login";
        }

        DichVu dv = dichVuRepo.findById(maDv).orElse(null);

        if (dv == null) {
            return "redirect:/admin/dichvu";
        }

        model.addAttribute("dichVu", dv);

        return "admin/dichvu-edit";
    }

    // ===================== XỬ LÝ SỬA =====================

    @PostMapping("/edit")
    public String edit(@ModelAttribute DichVu dv,
                       @RequestParam("file") MultipartFile file) {

        try {

            if (!file.isEmpty()) {

                String fileName = uploadFile(file);
                dv.setHinhAnh(fileName);

            } else {

                DichVu old = dichVuRepo.findById(dv.getMaDv()).orElse(null);

                if (old != null) {
                    dv.setHinhAnh(old.getHinhAnh());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        dichVuRepo.save(dv);

        return "redirect:/admin/dichvu";
    }

    // ===================== XÓA =====================

    @GetMapping("/delete/{maDv}")
    public String delete(@PathVariable int maDv) {

        dichVuRepo.deleteById(maDv);

        return "redirect:/admin/dichvu";
    }

    // ===================== HÀM UPLOAD FILE =====================

    private String uploadFile(MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            return null;
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalName = file.getOriginalFilename();
        String fileName = UUID.randomUUID() + "_" + originalName;

        Path filePath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}