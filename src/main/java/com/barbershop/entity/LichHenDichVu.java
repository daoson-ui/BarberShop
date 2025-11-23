package com.barbershop.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lich_hen_dich_vu")
@IdClass(LichHenDichVuId.class)
public class LichHenDichVu {

    @Id
    @ManyToOne
    @JoinColumn(name = "ma_lh")
    private LichHen lichHen;

    @Id
    @ManyToOne
    @JoinColumn(name = "ma_dv")
    private DichVu dichVu;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "ghi_chu")
    private String ghiChu;

    public LichHen getLichHen() {
        return lichHen;
    }

    public void setLichHen(LichHen lichHen) {
        this.lichHen = lichHen;
    }

    public DichVu getDichVu() {
        return dichVu;
    }

    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
