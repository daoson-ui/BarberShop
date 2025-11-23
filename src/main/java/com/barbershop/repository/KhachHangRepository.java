package com.barbershop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.barbershop.entity.KhachHang;
import com.barbershop.entity.NhanVien;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    @Query("SELECT k FROM KhachHang k " +
            "WHERE LOWER(k.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR k.sdt LIKE CONCAT('%', :keyword, '%')")
    List<KhachHang> search(String keyword);
}
