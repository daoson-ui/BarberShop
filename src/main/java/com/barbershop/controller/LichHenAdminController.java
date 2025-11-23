package com.barbershop.controller;

import com.barbershop.entity.LichHen;
import com.barbershop.entity.LichHenDichVu;
import com.barbershop.repository.LichHenRepository;
import com.barbershop.repository.LichHenDichVuRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/lichhen")
public class LichHenAdminController {

    @Autowired
    private LichHenRepository lichHenRepo;

    @Autowired
    private LichHenDichVuRepository lhDvRepo;

    // ========================= ADMIN LIST ONLY =========================
    @GetMapping
    public String list(Model model, HttpSession session) {

        // Kiểm tra đăng nhập
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        // Lấy toàn bộ lịch hẹn + SẮP XẾP:
        // 1) Ngày mới nhất trước
        // 2) Nếu trùng ngày → giờ kết thúc sớm hơn lên trước
        List<LichHen> list = lichHenRepo.findAllOrderByNgayDescGioAsc();

        // Map dịch vụ theo từng lịch hẹn
        Map<Integer, List<LichHenDichVu>> mapDv = new HashMap<>();
        for (LichHen lh : list) {
            mapDv.put(
                lh.getMaLh(),
                lhDvRepo.findByLichHen_MaLh(lh.getMaLh())
            );
        }

        // Gửi sang view
        model.addAttribute("listLichHen", list);
        model.addAttribute("mapDichVu", mapDv);

        return "lichhen-admin-list";  // Trang dành riêng cho Admin
    }
}
