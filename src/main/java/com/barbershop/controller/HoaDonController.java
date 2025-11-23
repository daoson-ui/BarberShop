package com.barbershop.controller;

import com.barbershop.entity.HoaDon;
import com.barbershop.entity.LichHenDichVu;
import com.barbershop.repository.HoaDonRepository;
import com.barbershop.repository.LichHenRepository;
import com.barbershop.repository.LichHenDichVuRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/hoadon")
public class HoaDonController {

    @Autowired
    private HoaDonRepository hoaDonRepo;

    @Autowired
    private LichHenRepository lichHenRepo;

    @Autowired
    private LichHenDichVuRepository lichHenDichVuRepo;

    // ==================== LIST ====================
    @GetMapping
    public String list(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        List<HoaDon> list = hoaDonRepo.findAll();

        // Map: maHd -> danh sách dịch vụ
        Map<Integer, List<LichHenDichVu>> mapDv = new HashMap<>();
        for (HoaDon hd : list) {
            mapDv.put(
                hd.getMaHd(),
                lichHenDichVuRepo.findByLichHen_MaLh(hd.getLichHen().getMaLh())
            );
        }

        model.addAttribute("listHoaDon", list);
        model.addAttribute("mapDichVu", mapDv);

        return "hoadon-list";
    }

    // ==================== ADD FORM ====================
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        model.addAttribute("hoaDon", new HoaDon());
        model.addAttribute("listLichHen", lichHenRepo.findAll());
        return "hoadon-add";
    }

    // ==================== ADD (POST) ====================
    @PostMapping("/add")
    public String add(@ModelAttribute HoaDon hd) {

        if (hd.getLichHen() == null || hd.getLichHen().getMaLh() == null) {
            return "redirect:/admin/hoadon?error=missing_lichhen";
        }

        Integer maLh = hd.getLichHen().getMaLh();
        List<LichHenDichVu> ds = lichHenDichVuRepo.findByLichHen_MaLh(maLh);

        hd.tinhTongTien(ds);
        hd.setNgayThanhToan(LocalDate.now());

        hoaDonRepo.save(hd);

        return "redirect:/admin/hoadon";
    }

    // ==================== EDIT FORM ====================
    @GetMapping("/edit/{maHd}")
    public String editForm(@PathVariable int maHd, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        HoaDon hd = hoaDonRepo.findById(maHd).orElse(null);

        model.addAttribute("hoaDon", hd);
        model.addAttribute("listLichHen", lichHenRepo.findAll());
        return "hoadon-edit";
    }

    // ==================== EDIT (POST) ====================
    @PostMapping("/edit")
    public String edit(@ModelAttribute HoaDon hd) {

        if (hd.getLichHen() == null || hd.getLichHen().getMaLh() == null) {
            return "redirect:/admin/hoadon?error=missing_lichhen";
        }

        Integer maLh = hd.getLichHen().getMaLh();
        List<LichHenDichVu> ds = lichHenDichVuRepo.findByLichHen_MaLh(maLh);

        hd.tinhTongTien(ds);
        hd.setNgayThanhToan(LocalDate.now());

        hoaDonRepo.save(hd);
        return "redirect:/admin/hoadon";
    }

    // ==================== DELETE ====================
    @GetMapping("/delete/{maHd}")
    public String delete(@PathVariable int maHd) {
        hoaDonRepo.deleteById(maHd);
        return "redirect:/admin/hoadon";
    }
}
