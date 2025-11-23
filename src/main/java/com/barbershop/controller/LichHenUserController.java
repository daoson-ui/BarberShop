package com.barbershop.controller;

import com.barbershop.entity.LichHen;
import com.barbershop.entity.LichHenDichVu;
import com.barbershop.repository.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user/lichhen")
public class LichHenUserController {

    @Autowired
    private LichHenRepository lichHenRepo;

    @Autowired
    private KhachHangRepository khRepo;

    @Autowired
    private NhanVienRepository nvRepo;

    @Autowired
    private DichVuRepository dichVuRepo;

    @Autowired
    private LichHenDichVuRepository lhDvRepo;

    // ========================= LIST =========================
    @GetMapping
    public String list(Model model, HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        List<LichHen> list = lichHenRepo.findAll();

        Map<Integer, List<LichHenDichVu>> mapDv = new HashMap<>();
        for (LichHen lh : list) {
            mapDv.put(
                lh.getMaLh(),
                lhDvRepo.findByLichHen_MaLh(lh.getMaLh())
            );
        }

        model.addAttribute("listLichHen", list);
        model.addAttribute("mapDichVu", mapDv);

        return "lichhen-user-list";
    }

    // ========================= ADD FORM =========================
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        model.addAttribute("lichHen", new LichHen());
        model.addAttribute("listKhachHang", khRepo.findAll());
        model.addAttribute("listNhanVien", nvRepo.findAll());
        model.addAttribute("listDichVu", dichVuRepo.findAll());

        return "lichhen-add";
    }

    // ========================= ADD SAVE =========================
    @PostMapping("/add")
    public String save(@ModelAttribute LichHen lh,
                       @RequestParam(required = false, name = "dichVuIds") List<Integer> dvIds) {

        // Lưu lịch hẹn
        lh = lichHenRepo.save(lh);

        // Lưu dịch vụ
        if (dvIds != null) {
            for (Integer maDv : dvIds) {
                LichHenDichVu obj = new LichHenDichVu();
                obj.setLichHen(lh);
                obj.setDichVu(dichVuRepo.findById(maDv).orElse(null));
                obj.setSoLuong(1);
                lhDvRepo.save(obj);
            }
        }

        return "redirect:/user/lichhen";
    }

    // ========================= EDIT FORM =========================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model, HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        LichHen lichHen = lichHenRepo.findById(id).orElse(null);

        List<Integer> selectedDvIds = lhDvRepo.findByLichHen_MaLh(id)
                .stream()
                .map(x -> x.getDichVu().getMaDv())
                .toList();

        model.addAttribute("lichHen", lichHen);
        model.addAttribute("listKhachHang", khRepo.findAll());
        model.addAttribute("listNhanVien", nvRepo.findAll());
        model.addAttribute("listDichVu", dichVuRepo.findAll());
        model.addAttribute("selectedDvIds", selectedDvIds);

        return "lichhen-edit";
    }

    // ========================= EDIT SAVE =========================
    @PostMapping("/edit")
    public String update(@ModelAttribute LichHen lh,
                         @RequestParam(required = false, name = "dichVuIds") List<Integer> dvIds) {

        // Lưu thay đổi lịch hẹn
        lichHenRepo.save(lh);

        // Xóa dịch vụ cũ
        lhDvRepo.deleteByLichHen_MaLh(lh.getMaLh());

        // Lưu dịch vụ mới
        if (dvIds != null) {
            for (Integer maDv : dvIds) {
                LichHenDichVu obj = new LichHenDichVu();
                obj.setLichHen(lh);
                obj.setDichVu(dichVuRepo.findById(maDv).orElse(null));
                obj.setSoLuong(1);
                lhDvRepo.save(obj);
            }
        }

        return "redirect:/user/lichhen";
    }

    // ========================= DELETE =========================
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id) {

        lhDvRepo.deleteByLichHen_MaLh(id);
        lichHenRepo.deleteById(id);

        return "redirect:/user/lichhen";
    }
}
