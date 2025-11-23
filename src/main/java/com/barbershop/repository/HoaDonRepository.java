package com.barbershop.repository;

import com.barbershop.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    // Doanh thu theo tháng trong tất cả các năm
    @Query("""
            SELECT 
                MONTH(hd.ngayThanhToan) AS thang,
                YEAR(hd.ngayThanhToan) AS nam,
                SUM(hd.tongTien) AS doanhThu
            FROM HoaDon hd
            GROUP BY YEAR(hd.ngayThanhToan), MONTH(hd.ngayThanhToan)
            ORDER BY YEAR(hd.ngayThanhToan), MONTH(hd.ngayThanhToan)
            """)
    List<Object[]> getDoanhThuTheoThang();

    // Doanh thu 1 năm cụ thể
    @Query("""
            SELECT 
                MONTH(hd.ngayThanhToan),
                SUM(hd.tongTien)
            FROM HoaDon hd
            WHERE YEAR(hd.ngayThanhToan) = :nam
            GROUP BY MONTH(hd.ngayThanhToan)
            ORDER BY MONTH(hd.ngayThanhToan)
            """)
    List<Object[]> getDoanhThuTrongNam(int nam);

    // Tổng doanh thu theo năm
    @Query("""
            SELECT 
                YEAR(hd.ngayThanhToan),
                SUM(hd.tongTien)
            FROM HoaDon hd
            GROUP BY YEAR(hd.ngayThanhToan)
            ORDER BY YEAR(hd.ngayThanhToan)
            """)
    List<Object[]> getDoanhThuTheoNam();
}
