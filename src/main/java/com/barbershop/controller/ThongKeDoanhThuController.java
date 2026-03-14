package com.barbershop.controller;

import com.barbershop.entity.HoaDon;
import com.barbershop.entity.LichHen;
import com.barbershop.entity.LichHenDichVu;
import com.barbershop.repository.HoaDonRepository;
import com.barbershop.repository.LichHenDichVuRepository;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

// iText 5
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.BaseFont; // 🔹 THÊM DÒNG NÀY

@Controller
@RequestMapping("/admin/thongke")
public class ThongKeDoanhThuController {

    @Autowired
    private LichHenDichVuRepository lhDvRepo;

    @Autowired
    private HoaDonRepository hoaDonRepo;

    // ============= TRANG THỐNG KÊ THEO NĂM (12 THÁNG) =============
    @GetMapping
    public String viewThongKe(
            @RequestParam(name = "nam", required = false) Integer nam,
            Model model) {

        if (nam == null)
            nam = Calendar.getInstance().get(Calendar.YEAR);

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

        return "admin/thongke-doanhthu";
    }

    // ============= API JSON (nếu cần vẽ biểu đồ theo năm) =============
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
        map.put("thang", List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
        map.put("doanhThu", data);

        return map;
    }

    // DTO đơn giản cho báo cáo chi tiết
    public static class StatRow {
        private String ten;
        private long soLan;
        private double doanhThu;

        public StatRow(String ten) {
            this.ten = ten;
        }

        public String getTen() {
            return ten;
        }

        public void setTen(String ten) {
            this.ten = ten;
        }

        public long getSoLan() {
            return soLan;
        }

        public void setSoLan(long soLan) {
            this.soLan = soLan;
        }

        public double getDoanhThu() {
            return doanhThu;
        }

        public void setDoanhThu(double doanhThu) {
            this.doanhThu = doanhThu;
        }
    }

    // =============== BÁO CÁO DOANH THU THEO THÁNG (CHI TIẾT + BIỂU ĐỒ)
    // ===============
    @GetMapping("/month")
    public String thongKeTheoThang(
            @RequestParam(name = "nam", required = false) Integer nam,
            @RequestParam(name = "thang", required = false) Integer thang,
            Model model) {

        LocalDate now = LocalDate.now();
        if (nam == null)
            nam = now.getYear();
        if (thang == null)
            thang = now.getMonthValue();

        // 1. Lấy tất cả hóa đơn trong tháng
        List<HoaDon> dsHoaDon = hoaDonRepo.findByMonthAndYear(nam, thang);

        double tongDoanhThu = 0d;
        int soHoaDon = dsHoaDon.size();

        // 2. Doanh thu theo ngày
        Map<Integer, Double> doanhThuTheoNgay = new TreeMap<>();

        // 3. Map thống kê chi tiết
        Map<String, StatRow> serviceStatMap = new HashMap<>();
        Map<String, StatRow> staffStatMap = new HashMap<>();
        Map<String, StatRow> customerStatMap = new HashMap<>();
        Map<String, StatRow> paymentStatMap = new HashMap<>();

        for (HoaDon hd : dsHoaDon) {
            if (hd.getNgayThanhToan() == null)
                continue;

            double tienHd = hd.getTongTien() != null ? hd.getTongTien() : 0d;
            tongDoanhThu += tienHd;

            // --- Doanh thu theo ngày ---
            int day = hd.getNgayThanhToan().getDayOfMonth();
            doanhThuTheoNgay.put(day,
                    doanhThuTheoNgay.getOrDefault(day, 0d) + tienHd);

            // --- Theo phương thức thanh toán ---
            String pt = hd.getPhuongThucTt() != null ? hd.getPhuongThucTt() : "Khác";
            StatRow ptRow = paymentStatMap.get(pt);
            if (ptRow == null) {
                ptRow = new StatRow(pt);
                paymentStatMap.put(pt, ptRow);
            }
            ptRow.setSoLan(ptRow.getSoLan() + 1);
            ptRow.setDoanhThu(ptRow.getDoanhThu() + tienHd);

            // Lấy lịch hẹn gắn với hóa đơn
            LichHen lh = hd.getLichHen();

            if (lh != null) {
                // --- Theo nhân viên ---
                if (lh.getNhanVien() != null) {
                    String tenNv = lh.getNhanVien().getHoTen();
                    if (tenNv == null)
                        tenNv = "Không rõ";

                    StatRow nvRow = staffStatMap.get(tenNv);
                    if (nvRow == null) {
                        nvRow = new StatRow(tenNv);
                        staffStatMap.put(tenNv, nvRow);
                    }
                    nvRow.setSoLan(nvRow.getSoLan() + 1);
                    nvRow.setDoanhThu(nvRow.getDoanhThu() + tienHd);
                }

                // --- Theo khách hàng ---
                if (lh.getKhachHang() != null) {
                    String tenKh = lh.getKhachHang().getHoTen();
                    if (tenKh == null)
                        tenKh = "Khách lẻ";

                    StatRow khRow = customerStatMap.get(tenKh);
                    if (khRow == null) {
                        khRow = new StatRow(tenKh);
                        customerStatMap.put(tenKh, khRow);
                    }
                    khRow.setSoLan(khRow.getSoLan() + 1);
                    khRow.setDoanhThu(khRow.getDoanhThu() + tienHd);
                }

                // --- Theo dịch vụ ---
                if (lh.getMaLh() != null) {
                    List<LichHenDichVu> dsDv = lhDvRepo.findByLichHen_MaLh(lh.getMaLh());
                    for (LichHenDichVu item : dsDv) {
                        if (item.getDichVu() == null)
                            continue;

                        String tenDv = item.getDichVu().getTenDv();
                        if (tenDv == null)
                            tenDv = "Dịch vụ khác";

                        double giaDv = item.getDichVu().getGia() != null ? item.getDichVu().getGia() : 0d;

                        StatRow dvRow = serviceStatMap.get(tenDv);
                        if (dvRow == null) {
                            dvRow = new StatRow(tenDv);
                            serviceStatMap.put(tenDv, dvRow);
                        }
                        dvRow.setSoLan(dvRow.getSoLan() + 1);
                        dvRow.setDoanhThu(dvRow.getDoanhThu() + giaDv);
                    }
                }
            }
        }

        // 🔥 BỔ SUNG ĐỦ 1..n NGÀY TRONG THÁNG (cả ngày không có hóa đơn = 0)
        int daysInMonth = LocalDate.of(nam, thang, 1).lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            doanhThuTheoNgay.putIfAbsent(d, 0d);
        }

        double doanhThuTrungBinh = soHoaDon > 0 ? tongDoanhThu / soHoaDon : 0d;

        // Chuyển map -> list và sắp xếp giảm dần theo doanh thu
        List<StatRow> topDichVu = new ArrayList<>(serviceStatMap.values());
        topDichVu.sort((a, b) -> Double.compare(b.getDoanhThu(), a.getDoanhThu()));

        List<StatRow> nhanVienStats = new ArrayList<>(staffStatMap.values());
        nhanVienStats.sort((a, b) -> Double.compare(b.getDoanhThu(), a.getDoanhThu()));

        List<StatRow> topKhachHang = new ArrayList<>(customerStatMap.values());
        topKhachHang.sort((a, b) -> Double.compare(b.getDoanhThu(), a.getDoanhThu()));
        if (topKhachHang.size() > 5) {
            topKhachHang = topKhachHang.subList(0, 5);
        }

        List<StatRow> paymentStats = new ArrayList<>(paymentStatMap.values());
        paymentStats.sort((a, b) -> Double.compare(b.getDoanhThu(), a.getDoanhThu()));

        // Lấy danh sách năm có dữ liệu để fill combobox
        List<Integer> dsNam = new ArrayList<>();
        for (Object[] row : hoaDonRepo.getDoanhThuTheoNam()) {
            dsNam.add(((Number) row[0]).intValue());
        }

        model.addAttribute("nam", nam);
        model.addAttribute("thang", thang);
        model.addAttribute("dsNam", dsNam);

        model.addAttribute("tongDoanhThu", tongDoanhThu);
        model.addAttribute("soHoaDon", soHoaDon);
        model.addAttribute("doanhThuTrungBinh", doanhThuTrungBinh);
        model.addAttribute("doanhThuTheoNgay", doanhThuTheoNgay);
        model.addAttribute("dsHoaDon", dsHoaDon);

        // Thống kê chi tiết
        model.addAttribute("topDichVu", topDichVu);
        model.addAttribute("nhanVienStats", nhanVienStats);
        model.addAttribute("topKhachHang", topKhachHang);
        model.addAttribute("paymentStats", paymentStats);

        return "admin/thongke-doanhthu-thang";
    }

    // =============== XUẤT BÁO CÁO DOANH THU THÁNG RA PDF (iText 5) ===============
    @GetMapping("/month/export-pdf")
    public void exportMonthPdf(
            @RequestParam("nam") Integer nam,
            @RequestParam("thang") Integer thang,
            HttpServletResponse response) throws IOException {

        LocalDate now = LocalDate.now();
        if (nam == null)
            nam = now.getYear();
        if (thang == null)
            thang = now.getMonthValue();

        // Lấy hóa đơn trong tháng
        List<HoaDon> dsHoaDon = hoaDonRepo.findByMonthAndYear(nam, thang);

        double tongDoanhThu = 0d;
        int soHoaDon = dsHoaDon.size();
        Map<Integer, Double> doanhThuTheoNgay = new TreeMap<>();

        for (HoaDon hd : dsHoaDon) {
            if (hd.getNgayThanhToan() == null)
                continue;

            double tienHd = hd.getTongTien() != null ? hd.getTongTien() : 0d;
            tongDoanhThu += tienHd;

            int day = hd.getNgayThanhToan().getDayOfMonth();
            doanhThuTheoNgay.put(day,
                    doanhThuTheoNgay.getOrDefault(day, 0d) + tienHd);
        }

        // 🔥 BỔ SUNG ĐỦ NGÀY CHO PDF
        int daysInMonth = LocalDate.of(nam, thang, 1).lengthOfMonth();
        for (int d = 1; d <= daysInMonth; d++) {
            doanhThuTheoNgay.putIfAbsent(d, 0d);
        }

        double doanhThuTrungBinh = soHoaDon > 0 ? tongDoanhThu / soHoaDon : 0d;

        // Thiết lập response
        response.setContentType("application/pdf");
        String fileName = String.format("baocao_doanhthu_%02d_%d.pdf", thang, nam);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, response.getOutputStream());
            doc.open();

            // 🔥 FONT ARIAL CÓ SẴN TRONG WINDOWS – HỖ TRỢ TIẾNG VIỆT
            BaseFont bf = BaseFont.createFont(
                    "C:/Windows/Fonts/arial.ttf",
                    BaseFont.IDENTITY_H,
                    BaseFont.EMBEDDED);
            Font titleFont = new Font(bf, 14, Font.BOLD);
            Font normalFont = new Font(bf, 11, Font.NORMAL);
            Font boldFont = new Font(bf, 11, Font.BOLD);

            // Tiêu đề
            doc.add(new Paragraph("BÁO CÁO DOANH THU THÁNG", titleFont));
            doc.add(new Paragraph(String.format("Tháng %02d/%d", thang, nam), normalFont));
            doc.add(new Paragraph(" ", normalFont));

            // Tổng quan
            doc.add(new Paragraph("TỔNG QUAN", boldFont));
            doc.add(new Paragraph(String.format("Tổng doanh thu: %,.0f VND", tongDoanhThu), normalFont));
            doc.add(new Paragraph(String.format("Số hóa đơn: %d", soHoaDon), normalFont));
            doc.add(new Paragraph(
                    String.format("Doanh thu trung bình / hóa đơn: %,.0f VND", doanhThuTrungBinh),
                    normalFont));
            doc.add(new Paragraph(" ", normalFont));

            // Bảng doanh thu theo ngày
            doc.add(new Paragraph("Doanh thu theo ngày trong tháng", boldFont));
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1f, 3f });

            PdfPCell head1 = new PdfPCell(new Phrase("Ngày", boldFont));
            PdfPCell head2 = new PdfPCell(new Phrase("Doanh thu (VND)", boldFont));
            table.addCell(head1);
            table.addCell(head2);

            if (doanhThuTheoNgay.isEmpty()) {
                PdfPCell cell = new PdfPCell(
                        new Phrase("Không có hóa đơn trong tháng này.", normalFont));
                cell.setColspan(2);
                table.addCell(cell);
            } else {
                for (Map.Entry<Integer, Double> entry : doanhThuTheoNgay.entrySet()) {
                    table.addCell(new PdfPCell(
                            new Phrase(String.valueOf(entry.getKey()), normalFont)));
                    table.addCell(new PdfPCell(
                            new Phrase(String.format("%,.0f", entry.getValue()), normalFont)));
                }
            }

            doc.add(table);

        } catch (DocumentException e) {
            throw new IOException(e);
        } finally {
            doc.close();
        }
    }
}
