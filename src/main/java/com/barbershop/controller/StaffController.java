package com.barbershop.controller;

import com.barbershop.entity.Account;
import com.barbershop.entity.NhanVien;
import com.barbershop.entity.LichHen;

import com.barbershop.repository.NhanVienRepository;
import com.barbershop.repository.LichHenRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private NhanVienRepository nhanVienRepo;

    @Autowired
    private LichHenRepository lichHenRepo;

    // =====================================================
    // LẤY NHÂN VIÊN TỪ SESSION
    // =====================================================
    private NhanVien getStaff(HttpSession session) {

        Account acc = (Account) session.getAttribute("user");

        if (acc == null) {
            return null;
        }

        return nhanVienRepo.findByAccount(acc);
    }

    // =====================================================
    // TRANG CHỦ STAFF
    // =====================================================
    @GetMapping("/home")
    public String home(HttpSession session, Model model) {

        NhanVien nv = getStaff(session);

        if (nv == null) {
            return "redirect:/login";
        }

        long waitingAppointments =
                lichHenRepo.countByStatusForStaff(nv.getManv(), "Chờ");

        long doneAppointments =
                lichHenRepo.countByStatusForStaff(nv.getManv(), "Hoàn thành");

        model.addAttribute("waitingAppointments", waitingAppointments);
        model.addAttribute("doneAppointments", doneAppointments);

        return "staff/staff-home";
    }

    // =====================================================
    // DANH SÁCH LỊCH HẸN CỦA STAFF
    // =====================================================
   @GetMapping("/appointments")
public String myAppointments(
        HttpSession session,
        Model model,
        @RequestParam(required = false) String keyword,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate ngayHen,

        @RequestParam(required = false) String trangThai
) {

    NhanVien nv = getStaff(session);

    if (nv == null) {
        return "redirect:/login";
    }

    List<LichHen> list = lichHenRepo.searchForStaff(
            nv.getManv(),
            keyword,
            ngayHen,
            trangThai
    );

    model.addAttribute("listLichHen", list);
    model.addAttribute("keyword", keyword);
    model.addAttribute("ngayHen", ngayHen);
    model.addAttribute("trangThai", trangThai);

    return "staff/staff-appointments";
}

    // =====================================================
    // CẬP NHẬT TRẠNG THÁI LỊCH
    // =====================================================
    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Integer id,
                               @RequestParam String status,
                               HttpSession session) {

        NhanVien nv = getStaff(session);

        if (nv == null) {
            return "redirect:/login";
        }

        LichHen lich = lichHenRepo.findById(id).orElse(null);

        if (lich != null) {

            // kiểm tra lịch thuộc nhân viên này
            if (lich.getNhanVien().getManv().equals(nv.getManv())) {

                lich.setTrangThai(status);

                lichHenRepo.save(lich);
            }
        }

        return "redirect:/staff/appointments";
    }

    // =====================================================
    // PROFILE STAFF
    // =====================================================
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {

        NhanVien nv = getStaff(session);

        if (nv == null) {
            return "redirect:/login";
        }

        model.addAttribute("nhanVien", nv);

        return "staff/profile-staff";
    }

    // =====================================================
    // TRANG EDIT PROFILE
    // =====================================================
    @GetMapping("/profile/edit")
    public String editProfile(HttpSession session, Model model) {

        NhanVien nv = getStaff(session);

        if (nv == null) {
            return "redirect:/login";
        }

        model.addAttribute("nhanVien", nv);

        return "staff/profile-staff-edit";
    }

    // =====================================================
    // UPDATE PROFILE
    // =====================================================
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute NhanVien nhanVien,
                                HttpSession session) {

        NhanVien nv = getStaff(session);

        if (nv == null) {
            return "redirect:/login";
        }

        // chỉ update thông tin cơ bản
        nv.setHoTen(nhanVien.getHoTen());
        nv.setSdt(nhanVien.getSdt());
        nv.setGioiTinh(nhanVien.getGioiTinh());
        nv.setChucVu(nhanVien.getChucVu());

        nhanVienRepo.save(nv);

        return "redirect:/staff/profile";
    }

}