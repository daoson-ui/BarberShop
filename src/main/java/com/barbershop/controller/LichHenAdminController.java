package com.barbershop.controller;

import com.barbershop.entity.LichHen;
import com.barbershop.entity.LichHenDichVu;
import com.barbershop.repository.LichHenRepository;
import com.barbershop.repository.LichHenDichVuRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin/lichhen")
public class LichHenAdminController {

    @Autowired
    private LichHenRepository lichHenRepo;

    @Autowired
    private LichHenDichVuRepository lhDvRepo;


    // ========================= LIST =========================
    @GetMapping
    public String list(Model model, HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        // Load thông báo rồi xóa
        Object success = session.getAttribute("successMsg");
        Object error = session.getAttribute("errorMsg");

        if (success != null) {
            model.addAttribute("successMsg", success);
            session.removeAttribute("successMsg");
        }
        if (error != null) {
            model.addAttribute("errorMsg", error);
            session.removeAttribute("errorMsg");
        }

        List<LichHen> list = lichHenRepo.findAllOrderByNgayDescGioAsc();

        Map<Integer, List<LichHenDichVu>> mapDv = new HashMap<>();
        for (LichHen lh : list) {
            mapDv.put(lh.getMaLh(),
                    lhDvRepo.findByLichHen_MaLh(lh.getMaLh()));
        }

        model.addAttribute("listLichHen", list);
        model.addAttribute("mapDichVu", mapDv);

        return "lichhen-admin-list";
    }


    // ========================= DELETE =========================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id, HttpSession session) {

        LichHen lh = lichHenRepo.findById(id).orElse(null);

        if (lh == null) {
            session.setAttribute("errorMsg", "❌ Lịch hẹn không tồn tại!");
            return "redirect:/admin/lichhen";
        }

        // Xóa dịch vụ trước
        lhDvRepo.deleteByLichHen_MaLh(id);

        // Xóa lịch hẹn
        lichHenRepo.deleteById(id);

        session.setAttribute("successMsg", "✔ Đã xóa lịch hẹn thành công!");

        return "redirect:/admin/lichhen";
    }
}
