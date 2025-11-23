package com.barbershop.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lich_hen")
public class LichHen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_lh")
    private Integer maLh;

    @Column(name = "ngay_gio_bat_dau")
    private LocalDateTime ngayGioBatDau;

    @Column(name = "ngay_gio_ket_thuc")
    private LocalDateTime ngayGioKetThuc;

    @ManyToOne
    @JoinColumn(name = "makh")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "manv")
    private NhanVien nhanVien;

    @OneToMany(mappedBy = "lichHen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LichHenDichVu> dichVus;

    // ========== GETTER – SETTER ==========

    public Integer getMaLh() {
        return maLh;
    }

    public void setMaLh(Integer maLh) {
        this.maLh = maLh;
    }

    public LocalDateTime getNgayGioBatDau() {
        return ngayGioBatDau;
    }

    public void setNgayGioBatDau(LocalDateTime ngayGioBatDau) {
        this.ngayGioBatDau = ngayGioBatDau;
    }

    public LocalDateTime getNgayGioKetThuc() {
        return ngayGioKetThuc;
    }

    public void setNgayGioKetThuc(LocalDateTime ngayGioKetThuc) {
        this.ngayGioKetThuc = ngayGioKetThuc;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public List<LichHenDichVu> getDichVus() {
        return dichVus;
    }

    public void setDichVus(List<LichHenDichVu> dichVus) {
        this.dichVus = dichVus;
    }
}
