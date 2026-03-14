package com.barbershop.controller;

import com.barbershop.entity.*;
import com.barbershop.repository.*;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;
import java.util.*;

@Controller
@RequestMapping("/admin/lichhen")
public class LichHenAdminController {

    @Autowired
    private LichHenRepository lichHenRepo;

    @Autowired
    private LichHenDichVuRepository lhDvRepo;

    @Autowired
    private KhachHangRepository khRepo;

    @Autowired
    private NhanVienRepository nvRepo;

    @Autowired
    private DichVuRepository dichVuRepo;

    // =================== UTIL ===================
    private List<LocalTime> generateTimeSlots() {

        List<LocalTime> list = new ArrayList<>();

        LocalTime start = LocalTime.of(8,0);
        LocalTime end = LocalTime.of(20,0);

        while(!start.isAfter(end)){
            list.add(start);
            start = start.plusMinutes(20);
        }

        return list;
    }

    private int getTotalDuration(List<Integer> dvIds){

        if(dvIds == null || dvIds.isEmpty())
            return 20;

        return dvIds.stream()
                .map(id -> dichVuRepo.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .mapToInt(DichVu::getThoiGianThucHien)
                .sum();
    }

    // ========================= LIST =========================

    @GetMapping
    public String list(
            @RequestParam(required=false) String keyword,

            @RequestParam(required=false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate ngayHen,

            @RequestParam(required=false) String trangThai,

            Model model,
            HttpSession session){

        if(session.getAttribute("user")==null)
            return "redirect:/login";

        if(keyword!=null && keyword.trim().isEmpty())
            keyword=null;

        if(trangThai!=null && trangThai.trim().isEmpty())
            trangThai=null;

        List<LichHen> list = lichHenRepo.searchForAdmin(
                keyword,
                ngayHen,
                trangThai
        );

        Map<Integer,List<LichHenDichVu>> mapDv = new HashMap<>();

        for(LichHen lh : list){

            mapDv.put(
                    lh.getMaLh(),
                    lhDvRepo.findByLichHen_MaLh(lh.getMaLh())
            );
        }

        model.addAttribute("listLichHen",list);
        model.addAttribute("mapDichVu",mapDv);

        model.addAttribute("keyword",keyword);
        model.addAttribute("ngayHen",ngayHen);
        model.addAttribute("trangThai",trangThai);

        return "admin/lichhen-admin-list";
    }
    // ========================= FORM ADD =========================

    @GetMapping("/add")
    public String addForm(Model model,HttpSession session){

        if(session.getAttribute("user")==null)
            return "redirect:/login";

        model.addAttribute("listKhachHang",khRepo.findAll());
        model.addAttribute("listNhanVien",nvRepo.findAll());
        model.addAttribute("listDichVu",dichVuRepo.findAll());

        return "admin/lichhen-admin-add";
    }

    // ========================= API TIMESLOTS =========================

    @GetMapping("/api/timeslots")
    @ResponseBody
    public List<Map<String,Object>> apiTimeSlots(
            @RequestParam("manv") int manv,
            @RequestParam("ngay") String ngay,
            @RequestParam(name="dvIds",required=false) List<Integer> dvIds,
            @RequestParam(name="excludeId",required=false) Integer excludeId){

        LocalDate date = LocalDate.parse(ngay);

        if((dvIds==null || dvIds.isEmpty()) && excludeId!=null){

            dvIds = lhDvRepo.findByLichHen_MaLh(excludeId)
                    .stream()
                    .map(x -> x.getDichVu().getMaDv())
                    .toList();
        }

        if(dvIds==null)
            dvIds = new ArrayList<>();

        int duration = getTotalDuration(dvIds);

        if(duration==0)
            duration=20;

        List<LocalTime> slots = generateTimeSlots();

        List<LichHen> booked = lichHenRepo.findByNhanVien_Manv(manv)
                .stream()
                .filter(lh -> date.equals(lh.getNgayHen()))
                .toList();

        List<Map<String,Object>> result = new ArrayList<>();

        for(LocalTime slot : slots){

            LocalTime slotEnd = slot.plusMinutes(duration);

            boolean busy=false;

            for(LichHen lh : booked){

                if(excludeId!=null && Objects.equals(lh.getMaLh(),excludeId))
                    continue;

                int usedMinutes = lhDvRepo.findByLichHen_MaLh(lh.getMaLh())
                        .stream()
                        .mapToInt(x->x.getDichVu().getThoiGianThucHien())
                        .sum();

                if(usedMinutes==0)
                    usedMinutes=20;

                LocalTime start = lh.getGioHen();
                LocalTime end = start.plusMinutes(usedMinutes);

                boolean overlap = slot.isBefore(end) && slotEnd.isAfter(start);

                if(overlap){
                    busy=true;
                    break;
                }
            }

            Map<String,Object> row = new HashMap<>();

            row.put("time",slot.toString());
            row.put("busy",busy);

            result.add(row);
        }

        return result;
    }

    // ========================= ADD =========================

    @PostMapping("/add")
    @Transactional
    public String add(
            @RequestParam LocalDate ngayHen,
            @RequestParam LocalTime gioHen,
            @RequestParam Integer makh,
            @RequestParam int manv,
            @RequestParam(name="dichVuChon",required=false) List<Integer> dvIds,
            HttpSession session,
            RedirectAttributes ra){

        if(session.getAttribute("user")==null)
            return "redirect:/login";

        if(LocalDateTime.of(ngayHen,gioHen).isBefore(LocalDateTime.now())){

            ra.addFlashAttribute("errorMsg","❌ Không thể tạo lịch trong quá khứ!");
            return "redirect:/admin/lichhen/add";
        }

        KhachHang kh = khRepo.findById(makh).orElse(null);
        NhanVien nv = nvRepo.findById(manv).orElse(null);

        if(kh == null || nv == null){

            ra.addFlashAttribute("errorMsg","❌ Khách hàng hoặc nhân viên không hợp lệ!");
            return "redirect:/admin/lichhen/add";
        }

        LichHen lh = new LichHen();

        lh.setNgayHen(ngayHen);
        lh.setGioHen(gioHen);
        lh.setKhachHang(kh);
        lh.setNhanVien(nv);

        lichHenRepo.save(lh);

        if(dvIds != null){

            for(Integer dvId : dvIds){

                DichVu dv = dichVuRepo.findById(dvId).orElse(null);

                if(dv == null) continue;

                LichHenDichVu item = new LichHenDichVu();

                item.setLichHen(lh);
                item.setDichVu(dv);

                lhDvRepo.save(item);
            }
        }

        ra.addFlashAttribute("successMsg",
                "✔ Đã tạo lịch hẹn cho khách hàng " + kh.getHoTen());

        return "redirect:/admin/lichhen";
    }


    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable int id,
                        Model model,
                        HttpSession session){

        if(session.getAttribute("user")==null)
            return "redirect:/login";

        LichHen lh = lichHenRepo.findById(id).orElse(null);

        if(lh==null)
            return "redirect:/admin/lichhen";

        List<LichHenDichVu> dvList =
                lhDvRepo.findByLichHen_MaLh(id);

        List<Integer> selectedIds =
                dvList.stream()
                        .map(x -> x.getDichVu().getMaDv())
                        .toList();

        model.addAttribute("lichHen",lh);
        model.addAttribute("listNhanVien",nvRepo.findAll());
        model.addAttribute("listDichVu",dichVuRepo.findAll());
        model.addAttribute("selectedDvIds",selectedIds);

        return "admin/lichhen-admin-edit";
    } 

    @PostMapping("/edit")
    @Transactional
    public String edit(
            @RequestParam int maLh,
            @RequestParam LocalDate ngayHen,
            @RequestParam LocalTime gioHen,
            @RequestParam int nhanVien,
            @RequestParam(name="dichVuChon",required=false) List<Integer> dvIds,
            HttpSession session,
            RedirectAttributes ra){

        if(session.getAttribute("user")==null)
            return "redirect:/login";

        LichHen lh = lichHenRepo.findById(maLh).orElse(null);

        if(lh==null){

            ra.addFlashAttribute("errorMsg","❌ Lịch hẹn không tồn tại");
            return "redirect:/admin/lichhen";
        }

        if(LocalDateTime.of(ngayHen,gioHen).isBefore(LocalDateTime.now())){

            ra.addFlashAttribute("errorMsg","❌ Không thể chỉnh sửa về quá khứ!");
            return "redirect:/admin/lichhen/edit/"+maLh;
        }

        NhanVien nv = nvRepo.findById(nhanVien).orElse(null);

        if(nv==null){

            ra.addFlashAttribute("errorMsg","❌ Nhân viên không hợp lệ!");
            return "redirect:/admin/lichhen/edit/"+maLh;
        }

        lh.setNgayHen(ngayHen);
        lh.setGioHen(gioHen);
        lh.setNhanVien(nv);

        lichHenRepo.save(lh);

        // Xóa dịch vụ cũ
        lhDvRepo.deleteByLichHen_MaLh(maLh);

        // Thêm lại dịch vụ mới
        if(dvIds!=null){

            for(Integer dvId:dvIds){

                DichVu dv=dichVuRepo.findById(dvId).orElse(null);

                if(dv==null) continue;

                LichHenDichVu item=new LichHenDichVu();

                item.setLichHen(lh);
                item.setDichVu(dv);

                lhDvRepo.save(item);
            }
        }

        ra.addFlashAttribute("successMsg",
                "✔ Đã cập nhật lịch hẹn thành công");

        return "redirect:/admin/lichhen";
    }
    // ========================= DELETE =========================

    @GetMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable int id,
                         HttpSession session,
                         RedirectAttributes ra){

        LichHen lh = lichHenRepo.findById(id).orElse(null);

        if(lh == null){

            ra.addFlashAttribute("errorMsg","❌ Lịch hẹn không tồn tại!");
            return "redirect:/admin/lichhen";
        }

    lhDvRepo.deleteByLichHen_MaLh(id);
        lichHenRepo.deleteById(id);

        ra.addFlashAttribute("successMsg","✔ Đã xóa lịch hẹn thành công!");

        return "redirect:/admin/lichhen";
    }
}