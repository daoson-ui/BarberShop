package com.barbershop.repository;

import com.barbershop.entity.LichHen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface LichHenRepository extends JpaRepository<LichHen, Integer> {

    // =====================================================
    // THỐNG KÊ LƯỢT ĐẾN CỦA KHÁCH
    // =====================================================
    @Query("""
        SELECT lh.khachHang.makh, COUNT(lh)
        FROM LichHen lh
        GROUP BY lh.khachHang.makh
    """)
    List<Object[]> thongKeLuotDen();


    // =====================================================
    // DANH SÁCH LỊCH HẸN
    // =====================================================

    // tất cả lịch
    @Query("""
        SELECT lh
        FROM LichHen lh
        ORDER BY lh.ngayHen DESC, lh.gioHen ASC
    """)
    List<LichHen> findAllOrderByNgayDescGioAsc();


    // lịch của khách
    @Query("""
        SELECT lh
        FROM LichHen lh
        WHERE lh.khachHang.makh = :makh
        ORDER BY lh.ngayHen DESC, lh.gioHen ASC
    """)
    List<LichHen> findByKhachHangOrderByNgayDescGioAsc(@Param("makh") Integer makh);


    // lịch của nhân viên
    List<LichHen> findByNhanVien_Manv(Integer manv);


    // =====================================================
    // KIỂM TRA TRÙNG LỊCH
    // =====================================================

    // trùng lịch nhân viên
    @Query("""
        SELECT COUNT(lh)
        FROM LichHen lh
        WHERE lh.nhanVien.manv = :manv
        AND lh.maLh <> :excludeId
        AND lh.ngayHen = :ngayHen
        AND lh.gioHen = :gioHen
    """)
    int countOverlapForNhanVien(
            @Param("manv") Integer manv,
            @Param("ngayHen") LocalDate ngayHen,
            @Param("gioHen") LocalTime gioHen,
            @Param("excludeId") Integer excludeId
    );


    // trùng lịch khách
    @Query("""
        SELECT COUNT(lh)
        FROM LichHen lh
        WHERE lh.khachHang.makh = :makh
        AND lh.maLh <> :excludeId
        AND lh.ngayHen = :ngayHen
        AND lh.gioHen = :gioHen
    """)
    int countOverlapForKhach(
            @Param("makh") Integer makh,
            @Param("ngayHen") LocalDate ngayHen,
            @Param("gioHen") LocalTime gioHen,
            @Param("excludeId") Integer excludeId
    );


    // trùng lịch với khách khác
    @Query("""
        SELECT COUNT(lh)
        FROM LichHen lh
        WHERE lh.nhanVien.manv = :manv
        AND lh.khachHang.makh <> :makh
        AND lh.maLh <> :excludeId
        AND lh.ngayHen = :ngayHen
        AND lh.gioHen = :gioHen
    """)
    int countOverlapOtherKhach(
            @Param("manv") Integer manv,
            @Param("makh") Integer makh,
            @Param("ngayHen") LocalDate ngayHen,
            @Param("gioHen") LocalTime gioHen,
            @Param("excludeId") Integer excludeId
    );


    // =====================================================
    // KIỂM TRA TRÙNG LỊCH THEO KHOẢNG THỜI GIAN
    // =====================================================
    @Query(value = """
        SELECT *
        FROM lich_hen lh
        WHERE lh.manv = :manv
        AND lh.ngay_hen = :ngayHen
        AND (
            :gioBatDau < ADDTIME(lh.gio_hen, SEC_TO_TIME(:duration * 60))
            AND
            ADDTIME(:gioBatDau, SEC_TO_TIME(:duration * 60)) > lh.gio_hen
        )
    """, nativeQuery = true)
    List<LichHen> checkOverlapRange(
            @Param("manv") Integer manv,
            @Param("ngayHen") LocalDate ngayHen,
            @Param("gioBatDau") LocalTime gioBatDau,
            @Param("duration") Integer duration
    );


    // =====================================================
    // THỐNG KÊ HÔM NAY
    // =====================================================
    @Query("""
        SELECT COUNT(lh)
        FROM LichHen lh
        WHERE lh.ngayHen = :today
    """)
    long countToday(@Param("today") LocalDate today);


    // lịch hôm nay của staff
    @Query("""
        SELECT COUNT(lh)
        FROM LichHen lh
        WHERE lh.nhanVien.manv = :manv
        AND lh.ngayHen = :today
    """)
    long countTodayForStaff(
            @Param("manv") Integer manv,
            @Param("today") LocalDate today
    );


    // đếm lịch theo trạng thái của staff
    @Query("""
        SELECT COUNT(lh)
        FROM LichHen lh
        WHERE lh.nhanVien.manv = :manv
        AND lh.trangThai = :status
    """)
    long countByStatusForStaff(
            @Param("manv") Integer manv,
            @Param("status") String status
    );


    // =====================================================
    // SEARCH LỊCH HẸN CHO ADMIN
    // =====================================================
    @Query("""
    SELECT lh
    FROM LichHen lh
    WHERE
        (
            :keyword IS NULL OR :keyword = ''
            OR LOWER(lh.khachHang.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR lh.khachHang.sdt LIKE CONCAT('%', :keyword, '%')
            OR LOWER(lh.nhanVien.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    AND (
        :ngayHen IS NULL
        OR lh.ngayHen = :ngayHen
    )
    AND (
        :trangThai IS NULL OR :trangThai = ''
        OR lh.trangThai = :trangThai
    )
    ORDER BY lh.ngayHen DESC, lh.gioHen ASC
""")
List<LichHen> searchForAdmin(
        @Param("keyword") String keyword,
        @Param("ngayHen") LocalDate ngayHen,
        @Param("trangThai") String trangThai
);
    // =====================================================
// SEARCH LỊCH HẸN CHO STAFF
// =====================================================
@Query("""
    SELECT lh
    FROM LichHen lh
    WHERE lh.nhanVien.manv = :manv
    AND (
        :keyword IS NULL OR :keyword = ''
        OR LOWER(lh.khachHang.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
    AND (:ngayHen IS NULL OR lh.ngayHen = :ngayHen)
    AND (
        :trangThai IS NULL OR :trangThai = ''
        OR lh.trangThai = :trangThai
    )
    ORDER BY lh.ngayHen DESC, lh.gioHen ASC
""")
List<LichHen> searchForStaff(
        @Param("manv") Integer manv,
        @Param("keyword") String keyword,
        @Param("ngayHen") LocalDate ngayHen,
        @Param("trangThai") String trangThai
);
}