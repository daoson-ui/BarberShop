package com.barbershop.repository;

import com.barbershop.entity.LichHen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface LichHenRepository extends JpaRepository<LichHen, Integer> {

    @Query("""
        SELECT lh.khachHang.makh, COUNT(lh)
        FROM LichHen lh
        GROUP BY lh.khachHang.makh
    """)
    List<Object[]> thongKeLuotDen();
    @Query("SELECT lh FROM LichHen lh " +
       "ORDER BY DATE(lh.ngayGioBatDau) DESC, lh.ngayGioKetThuc ASC")
    List<LichHen> findAllOrderByNgayDescGioAsc();
}
