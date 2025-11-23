package com.barbershop.controller;

import com.barbershop.repository.HoaDonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/admin/thongke")
public class ThongKeDoanhThuController {

    @Autowired
    private HoaDonRepository hoaDonRepo;

    // ============= TRANG THỐNG KÊ =============
    @GetMapping
    public String viewThongKe(
            @RequestParam(name = "nam", required = false) Integer nam,
            Model model) {

        if (nam == null) nam = Calendar.getInstance().get(Calendar.YEAR);

        List<Object[]> raw = hoaDonRepo.getDoanhThuTrongNam(nam);

        double[] doanhThu = new double[12];
        Arrays.fill(doanhThu, 0);

        for (Object[] row : raw) {
            int thang = (int) row[0];
            double tien = (double) row[1];
            doanhThu[thang - 1] = tien;
        }

        model.addAttribute("nam", nam);
        model.addAttribute("doanhThu", doanhThu);

        return "thongke-doanhthu";
    }

    // ============= API JSON (vẽ biểu đồ) =============
    @GetMapping("/chart-data")
    @ResponseBody
    public Map<String, Object> getChartData(@RequestParam("nam") int nam) {

        List<Object[]> raw = hoaDonRepo.getDoanhThuTrongNam(nam);

        double[] data = new double[12];
        Arrays.fill(data, 0);

        for (Object[] row : raw) {
            int month = (int) row[0];
            double amount = (double) row[1];
            data[month - 1] = amount;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("thang", List.of(1,2,3,4,5,6,7,8,9,10,11,12));
        map.put("doanhThu", data);

        return map;
    }
}
