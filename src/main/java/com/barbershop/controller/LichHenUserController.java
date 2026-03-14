package com.barbershop.controller;

import com.barbershop.dto.TimeSlotDTO;
import com.barbershop.entity.*;
import com.barbershop.repository.*;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

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


    // ================= LẤY KHÁCH HÀNG =================

    private KhachHang getKhachHang(Account acc){
        return khRepo.findByAccount(acc);
    }


    // ================= DANH SÁCH LỊCH HẸN =================

    @GetMapping
public String list(Model model, HttpSession session){

    Account acc = (Account) session.getAttribute("user");

    if(acc == null){
        return "redirect:/login";
    }

    KhachHang kh = getKhachHang(acc);

    List<LichHen> list =
            lichHenRepo.findByKhachHangOrderByNgayDescGioAsc(kh.getMakh());

    // Map lưu dịch vụ theo lịch hẹn
    java.util.Map<Integer, List<DichVu>> mapDichVu = new java.util.HashMap<>();

    for(LichHen lh : list){

        List<DichVu> dvList =
                lhDvRepo.findByLichHen_MaLh(lh.getMaLh())
                        .stream()
                        .map(x -> x.getDichVu())
                        .collect(Collectors.toList());

        mapDichVu.put(lh.getMaLh(), dvList);
    }

    model.addAttribute("listLichHen", list);
    model.addAttribute("mapDichVu", mapDichVu);
    model.addAttribute("activePage","appointments");

    return "user/user-appointments";
}


    // ================= FORM THÊM =================

    @GetMapping("/add")
    public String addForm(Model model, HttpSession session){

        if(session.getAttribute("user") == null){
            return "redirect:/login";
        }

        model.addAttribute("listNhanVien", nvRepo.findAll());
        model.addAttribute("listDichVu", dichVuRepo.findAll());
        model.addAttribute("activePage","appointments");

        return "user/lichhen-add";
    }


    // ================= LƯU LỊCH =================

    @PostMapping("/save")
    public String save(
            @RequestParam LocalDate ngayHen,
            @RequestParam LocalTime gioHen,
            @RequestParam int manv,
            @RequestParam(name="dichVuChon",required=false) List<Integer> dvIds,
            HttpSession session){

        Account acc = (Account) session.getAttribute("user");

        if(acc == null){
            return "redirect:/login";
        }

        KhachHang kh = getKhachHang(acc);

        if(LocalDateTime.of(ngayHen,gioHen).isBefore(LocalDateTime.now())){

            session.setAttribute("errorMsg","Không thể đặt lịch trong quá khứ!");
            return "redirect:/user/lichhen/add";
        }

        LichHen lh = new LichHen();

        lh.setNgayHen(ngayHen);
        lh.setGioHen(gioHen);
        lh.setTrangThai("Chờ");
        lh.setKhachHang(kh);
        lh.setNhanVien(nvRepo.findById(manv).orElse(null));

        lichHenRepo.save(lh);

        if(dvIds != null){

            for(Integer dv : dvIds){

                LichHenDichVu item = new LichHenDichVu();

                item.setLichHen(lh);
                item.setDichVu(dichVuRepo.findById(dv).orElse(null));

                lhDvRepo.save(item);
            }
        }

        session.setAttribute("successMsg","Đặt lịch thành công");

        return "redirect:/user/lichhen";
    }


    // ================= FORM SỬA =================

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id, Model model, HttpSession session){

        Account acc = (Account) session.getAttribute("user");

        if(acc == null){
            return "redirect:/login";
        }

        LichHen lh = lichHenRepo.findById(id).orElse(null);

        if(lh == null){
            return "redirect:/user/lichhen";
        }

        List<Integer> selected =
                lhDvRepo.findByLichHen_MaLh(id)
                        .stream()
                        .map(x -> x.getDichVu().getMaDv())
                        .collect(Collectors.toList());

        model.addAttribute("lichHen", lh);
        model.addAttribute("listNhanVien", nvRepo.findAll());
        model.addAttribute("listDichVu", dichVuRepo.findAll());
        model.addAttribute("selectedDvIds", selected);
        model.addAttribute("activePage","appointments");

        return "user/lichhen-edit";
    }


    // ================= CẬP NHẬT =================

    @Transactional
    @PostMapping("/edit")
    public String update(
            @RequestParam int maLh,
            @RequestParam LocalDate ngayHen,
            @RequestParam LocalTime gioHen,
            @RequestParam int nhanVien,
            @RequestParam(name="dichVuChon",required=false) List<Integer> dvIds,
            HttpSession session){

        Account acc = (Account) session.getAttribute("user");

        if(acc == null){
            return "redirect:/login";
        }

        LichHen lh = lichHenRepo.findById(maLh).orElse(null);

        if(lh == null){
            return "redirect:/user/lichhen";
        }

        lh.setNgayHen(ngayHen);
        lh.setGioHen(gioHen);
        lh.setNhanVien(nvRepo.findById(nhanVien).orElse(null));

        lichHenRepo.save(lh);

        lhDvRepo.deleteByLichHen_MaLh(maLh);

        if(dvIds != null){

            for(Integer dv : dvIds){

                LichHenDichVu item = new LichHenDichVu();

                item.setLichHen(lh);
                item.setDichVu(dichVuRepo.findById(dv).orElse(null));

                lhDvRepo.save(item);
            }
        }

        session.setAttribute("successMsg","Cập nhật lịch hẹn thành công");

        return "redirect:/user/lichhen";
    }


    // ================= XÓA =================

    @Transactional
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable int id, HttpSession session){

        Account acc = (Account) session.getAttribute("user");

        if(acc == null){
            return "redirect:/login";
        }

        lhDvRepo.deleteByLichHen_MaLh(id);
        lichHenRepo.deleteById(id);

        session.setAttribute("successMsg","Đã xóa lịch hẹn");

        return "redirect:/user/lichhen";
    }
    // ================= API LẤY KHUNG GIỜ =================

@GetMapping("/api/timeslots")
@ResponseBody
public List<TimeSlotDTO> getTimeSlots(
        @RequestParam int manv,
        @RequestParam LocalDate ngay,
        @RequestParam(required = false) List<Integer> dvIds) {

    // Lấy tất cả lịch của nhân viên
    List<LichHen> lichList = lichHenRepo.findByNhanVien_Manv(manv);

    LocalTime start = LocalTime.of(8,0);
    LocalTime end = LocalTime.of(20,0);

    List<TimeSlotDTO> result = new java.util.ArrayList<>();

    while(start.isBefore(end)){

        boolean busy = false;

        for(LichHen lh : lichList){

            if(!lh.getNgayHen().equals(ngay)) continue;

            // tính tổng thời gian dịch vụ của lịch đó
            int duration = lhDvRepo.findByLichHen_MaLh(lh.getMaLh())
                    .stream()
                    .mapToInt(x -> x.getDichVu().getThoiGianThucHien())
                    .sum();

            LocalTime existStart = lh.getGioHen();
            LocalTime existEnd = existStart.plusMinutes(duration);

            // nếu slot nằm trong khoảng lịch đã có
            if(!start.isBefore(existStart) && start.isBefore(existEnd)){
                busy = true;
                break;
            }
        }

        result.add(new TimeSlotDTO(start.toString(), busy));

        start = start.plusMinutes(20);
    }

    return result;
}
}