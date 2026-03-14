package com.barbershop.controller;

import com.barbershop.entity.*;
import com.barbershop.repository.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private KhachHangRepository khRepo;

    @Autowired
    private LichHenRepository lichHenRepo;

    @Autowired
    private LichHenDichVuRepository lhDvRepo;
    @Autowired
    private DichVuRepository dichVuRepo;

    // ===================== USER HOME ======================
    @GetMapping("/home")
    public String userHome(HttpSession session, Model model) {

        Account acc = (Account) session.getAttribute("user");

        // Chưa đăng nhập
        if (acc == null) {
            return "redirect:/login";
        }

        // Nếu login admin/staff nhưng vào link user
        if (acc.getRole().toUpperCase().contains("ADMIN")) {
            return "redirect:/admin/home";
        }

        if (acc.getRole().toUpperCase().contains("STAFF")) {
            return "redirect:/staff/home";
        }

        // Lấy thông tin khách hàng
        KhachHang kh = khRepo.findByAccount(acc);

        if (kh == null) {
            return "redirect:/login";
        }

        model.addAttribute("kh", kh);

        // ⭐ dùng cho navbar active
        model.addAttribute("activePage", "home");

        // Lấy danh sách dịch vụ
        List<DichVu> dichVuList = dichVuRepo.findAll();
        System.out.println("So dich vu: " + dichVuList.size());
        model.addAttribute("dichVuList", dichVuList); 

        // Thông báo từ session
        Object success = session.getAttribute("successMsg");
        if (success != null) {
            model.addAttribute("successMsg", success);
            session.removeAttribute("successMsg");
        }

        Object error = session.getAttribute("errorMsg");
        if (error != null) {
            model.addAttribute("errorMsg", error);
            session.removeAttribute("errorMsg");
        }

        // Danh sách lịch hẹn
        List<LichHen> list =
                lichHenRepo.findByKhachHangOrderByNgayDescGioAsc(kh.getMakh());

        // Map dịch vụ
        Map<Integer, List<LichHenDichVu>> mapDv = new HashMap<>();

        for (LichHen lh : list) {
            mapDv.put(
                    lh.getMaLh(),
                    lhDvRepo.findByLichHen_MaLh(lh.getMaLh())
            );
        }

        model.addAttribute("listLichHen", list);
        model.addAttribute("mapDichVu", mapDv);

        return "user/user-home";
    }

    // ===================== PROFILE ======================
    @GetMapping("/profile")
    public String userProfile(HttpSession session, Model model) {

        Account acc = (Account) session.getAttribute("user");

        if (acc == null) {
            return "redirect:/login";
        }

        if (acc.getRole().toUpperCase().contains("ADMIN")) {
            model.addAttribute("errorMsg", "Admin không có hồ sơ cá nhân.");
            return "user/profile-user";
        }

        KhachHang kh = khRepo.findByAccount(acc);

        model.addAttribute("kh", kh);
        model.addAttribute("activePage", "profile");

        return "user/profile-user";
    }

    // ================== UPDATE PROFILE ====================
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String hoTen,
            @RequestParam String sdt,
            @RequestParam(required = false) String gioiTinh,
            @RequestParam(required = false) String ngaySinh,
            HttpSession session,
            Model model) {

        Account acc = (Account) session.getAttribute("user");

        if (acc == null) {
            return "redirect:/login";
        }

        KhachHang kh = khRepo.findByAccount(acc);

        if (kh == null) {
            model.addAttribute("errorMsg", "Không tìm thấy thông tin khách hàng!");
            return "user/profile-user";
        }

        // Check trùng số điện thoại
        KhachHang check = khRepo.findBySdt(sdt);

        if (check != null && !check.getMakh().equals(kh.getMakh())) {
            model.addAttribute("kh", kh);
            model.addAttribute("errorMsg", "❌ Số điện thoại đã được sử dụng!");
            return "user/profile-user";
        }

        // Cập nhật thông tin
        kh.setHoTen(hoTen);
        kh.setSdt(sdt);
        kh.setGioiTinh(gioiTinh);

        if (ngaySinh != null && !ngaySinh.isEmpty()) {
            kh.setNgaySinh(LocalDate.parse(ngaySinh));
        }

        khRepo.save(kh);

        model.addAttribute("kh", kh);
        model.addAttribute("successMsg", "✔ Cập nhật thông tin thành công!");

        return "user/profile-user";
    }

    // ================== CHECK PHONE AJAX ====================
    @GetMapping("/checkPhone")
    @ResponseBody
    public boolean checkPhone(@RequestParam String sdt, HttpSession session) {

        Account acc = (Account) session.getAttribute("user");

        if (acc == null)
            return false;

        KhachHang kh = khRepo.findByAccount(acc);

        if (kh == null)
            return false;

        KhachHang check = khRepo.findBySdt(sdt);

        if (check == null)
            return false;

        return !check.getMakh().equals(kh.getMakh());
    }
}