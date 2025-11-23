package com.barbershop.repository;

import com.barbershop.entity.LichHenDichVu;
import com.barbershop.entity.LichHenDichVuId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LichHenDichVuRepository extends JpaRepository<LichHenDichVu, LichHenDichVuId> {

    List<LichHenDichVu> findByLichHen_MaLh(Integer maLh);

    void deleteByLichHen_MaLh(Integer maLh);

    // === LỊCH SỬ DỊCH VỤ THEO KHÁCH HÀNG ===
    @Query("""
        SELECT dv.tenDv, dv.gia, lh.ngayGioBatDau, lh.ngayGioKetThuc
        FROM LichHenDichVu ldv
            JOIN ldv.lichHen lh
            JOIN ldv.dichVu dv
        WHERE lh.khachHang.makh = :idKhach
        ORDER BY lh.ngayGioBatDau DESC
    """)
    List<Object[]> lichSuDichVu(Integer idKhach);
}
