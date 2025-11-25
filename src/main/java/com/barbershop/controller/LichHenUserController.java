package com.barbershop.controller;

import com.barbershop.entity.*;
import com.barbershop.repository.*;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/user/lichhen")
public class LichHenUserController {

    @Autowired private LichHenRepository lichHenRepo;
    @Autowired private KhachHangRepository khRepo;
    @Autowired private NhanVienRepository nvRepo;
    @Autowired private DichVuRepository dichVuRepo;
    @Autowired private LichHenDichVuRepository lhDvRepo;

    // ========================= UTIL =========================

    private KhachHang getKhachHang(Account acc) {
        return khRepo.findByAccount(acc);
    }

    /** Tạo slot 08:00 → 20:00 mỗi 20 phút */
    private List<LocalTime> generateTimeSlots() {
        List<LocalTime> list = new ArrayList<>();
        LocalTime t = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(20, 0);

        while (!t.isAfter(end)) {
            list.add(t);
            t = t.plusMinutes(20);
        }
        return list;
    }

    /** Tổng thời gian dịch vụ */
    private int getTotalDuration(List<Integer> dvIds) {
        if (dvIds == null || dvIds.isEmpty()) return 20;

        return dvIds.stream()
                .map(id -> dichVuRepo.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .mapToInt(DichVu::getThoiGianThucHien)
                .sum();
    }

    /** KIỂM TRA TRÙNG LỊCH – ĐÃ FIX CHUẨN */
    private boolean isOverlap(LocalDate day, LocalTime start,
                              int manv, int duration, Integer excludeId) {

        LocalTime end = start.plusMinutes(duration);

        List<LichHen> booked = lichHenRepo.findByNhanVien_Manv(manv).stream()
                .filter(lh -> lh.getNgayHen().equals(day))
                .toList();

        for (LichHen lh : booked) {

            // ⭐ BỎ QUA lịch đang CHỈNH SỬA → luôn RẢNH
            if (excludeId != null && lh.getMaLh() == excludeId)
                continue;

            int usedMinutes = lhDvRepo.findByLichHen_MaLh(lh.getMaLh())
                    .stream()
                    .mapToInt(x -> x.getDichVu().getThoiGianThucHien())
                    .sum();

            if (usedMinutes == 0) usedMinutes = 20;

            LocalTime s = lh.getGioHen();
            LocalTime e = s.plusMinutes(usedMinutes);

            // ⭐ CÔNG THỨC OVERLAP CHUẨN
            boolean overlap = start.isBefore(e) && end.isAfter(s);

            if (overlap)
                return true;
        }

        return false;
    }

    // ========================= LIST =========================
    @GetMapping
    public String list(Model model, HttpSession session) {

        Account acc = (Account) session.getAttribute("user");
        if (acc == null) return "redirect:/login";

        KhachHang kh = getKhachHang(acc);

        model.addAttribute("errorMsg", session.getAttribute("errorMsg"));
        model.addAttribute("successMsg", session.getAttribute("successMsg"));
        session.removeAttribute("errorMsg");
        session.removeAttribute("successMsg");

        List<LichHen> list = lichHenRepo.findByKhachHangOrderByNgayDescGioAsc(kh.getMakh());
        model.addAttribute("listLichHen", list);

        Map<Integer, List<LichHenDichVu>> mapDv = new HashMap<>();
        for (LichHen lh : list) {
            mapDv.put(lh.getMaLh(), lhDvRepo.findByLichHen_MaLh(lh.getMaLh()));
        }
        model.addAttribute("mapDichVu", mapDv);

        return "lichhen-user-list";
    }

    // ========================= ADD FORM =========================
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {

        if (session.getAttribute("user") == null)
            return "redirect:/login";

        model.addAttribute("errorMsg", session.getAttribute("errorAddMsg"));
        session.removeAttribute("errorAddMsg");

        model.addAttribute("listNhanVien", nvRepo.findAll());
        model.addAttribute("listDichVu", dichVuRepo.findAll());

        return "lichhen-add";
    }

    // ========================= API LẤY GIỜ RẢNH/BẬN =========================
    @GetMapping("/api/timeslots")
    @ResponseBody
    public List<Map<String, Object>> apiTimeSlots(
            @RequestParam int manv,
            @RequestParam String ngay,
            @RequestParam(name = "dvIds[]", required = false) List<Integer> dvIds,
            @RequestParam(name = "excludeId", required = false) Integer excludeId
    ) {

        LocalDate d = LocalDate.parse(ngay);

        // Nếu dvIds rỗng và đang sửa lịch → load DV cũ
        if ((dvIds == null || dvIds.isEmpty()) && excludeId != null) {
            dvIds = lhDvRepo.findByLichHen_MaLh(excludeId)
                    .stream()
                    .map(x -> x.getDichVu().getMaDv())
                    .toList();
        }

        if (dvIds == null) dvIds = new ArrayList<>();

        int duration = getTotalDuration(dvIds);

        List<Map<String, Object>> result = new ArrayList<>();

        for (LocalTime slot : generateTimeSlots()) {

            boolean busy = isOverlap(d, slot, manv, duration, excludeId);

            Map<String, Object> row = new HashMap<>();
            row.put("time", slot.toString());
            row.put("busy", busy);

            result.add(row);
        }

        return result;
    }

    // ========================= SAVE =========================
    @PostMapping("/save")
    public String save(
            @RequestParam LocalDate ngayHen,
            @RequestParam LocalTime gioHen,
            @RequestParam int manv,
            @RequestParam(name = "dichVuChon", required = false) List<Integer> dvIds,
            HttpSession session) {

        Account acc = (Account) session.getAttribute("user");
        if (acc == null) return "redirect:/login";

        KhachHang kh = getKhachHang(acc);

        if (LocalDateTime.of(ngayHen, gioHen).isBefore(LocalDateTime.now())) {
            session.setAttribute("errorAddMsg", "❌ Không thể đặt lịch trong quá khứ!");
            return "redirect:/user/lichhen/add";
        }

        int duration = getTotalDuration(dvIds);

        if (isOverlap(ngayHen, gioHen, manv, duration, null)) {
            session.setAttribute("errorAddMsg", "❌ Khung giờ này đã có người đặt!");
            return "redirect:/user/lichhen/add";
        }

        LichHen lh = new LichHen();
        lh.setNgayHen(ngayHen);
        lh.setGioHen(gioHen);
        lh.setKhachHang(kh);
        lh.setNhanVien(nvRepo.findById(manv).orElse(null));
        lichHenRepo.save(lh);

        if (dvIds != null) {
            for (Integer dv : dvIds) {
                LichHenDichVu link = new LichHenDichVu();
                link.setLichHen(lh);
                link.setDichVu(dichVuRepo.findById(dv).orElse(null));
                lhDvRepo.save(link);
            }
        }

        session.setAttribute("successMsg", "✔ Đặt lịch thành công!");
        return "redirect:/user/lichhen";
    }

    // ========================= EDIT FORM =========================
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model, HttpSession session) {

        Account acc = (Account) session.getAttribute("user");
        if (acc == null) return "redirect:/login";

        LichHen lh = lichHenRepo.findById(id).orElse(null);
        if (lh == null || lh.getKhachHang().getAccount().getId() != acc.getId())
            return "redirect:/user/lichhen";

        List<Integer> selected = lhDvRepo.findByLichHen_MaLh(id)
                .stream()
                .map(x -> x.getDichVu().getMaDv())
                .toList();

        model.addAttribute("lichHen", lh);
        model.addAttribute("listNhanVien", nvRepo.findAll());
        model.addAttribute("listDichVu", dichVuRepo.findAll());
        model.addAttribute("selectedDvIds", selected);

        return "lichhen-edit";
    }

    // ========================= UPDATE =========================
    @Transactional
    @PostMapping("/edit")
    public String update(
            @RequestParam int maLh,
            @RequestParam LocalDate ngayHen,
            @RequestParam LocalTime gioHen,
            @RequestParam int nhanVien,
            @RequestParam(name = "dichVuChon", required = false) List<Integer> dvIds,
            HttpSession session) {

        Account acc = (Account) session.getAttribute("user");
        if (acc == null) return "redirect:/login";

        LichHen lh = lichHenRepo.findById(maLh).orElse(null);
        if (lh == null) return "redirect:/user/lichhen";

        int duration = getTotalDuration(dvIds);

        // ⭐ excludeId = maLh để giờ đang sửa LUÔN rảnh
        if (isOverlap(ngayHen, gioHen, nhanVien, duration, maLh)) {
            session.setAttribute("errorMsg", "❌ Khung giờ bị trùng!");
            return "redirect:/user/lichhen/edit/" + maLh;
        }

        lh.setNgayHen(ngayHen);
        lh.setGioHen(gioHen);
        lh.setNhanVien(nvRepo.findById(nhanVien).orElse(null));
        lichHenRepo.save(lh);

        lhDvRepo.deleteByLichHen_MaLh(maLh);

        if (dvIds != null) {
            for (Integer dv : dvIds) {
                LichHenDichVu link = new LichHenDichVu();
                link.setLichHen(lh);
                link.setDichVu(dichVuRepo.findById(dv).orElse(null));
                lhDvRepo.save(link);
            }
        }

        session.setAttribute("successMsg", "✔ Cập nhật thành công!");
        return "redirect:/user/lichhen";
    }

    // ========================= DELETE =========================
    @Transactional
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id, HttpSession session) {

        Account acc = (Account) session.getAttribute("user");
        if (acc == null) return "redirect:/login";

        LichHen lh = lichHenRepo.findById(id).orElse(null);

        if (lh == null || lh.getKhachHang().getAccount().getId() != acc.getId())
            return "redirect:/user/lichhen";

        lhDvRepo.deleteByLichHen_MaLh(id);
        lichHenRepo.deleteById(id);

        session.setAttribute("successMsg", "✔ Đã xóa lịch hẹn!");
        return "redirect:/user/lichhen";
    }
}
