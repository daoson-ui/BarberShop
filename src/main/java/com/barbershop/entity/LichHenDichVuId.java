package com.barbershop.entity;

import java.io.Serializable;
import java.util.Objects;

public class LichHenDichVuId implements Serializable {

    private Integer lichHen;
    private Integer dichVu;

    public LichHenDichVuId() {}

    public LichHenDichVuId(Integer lichHen, Integer dichVu) {
        this.lichHen = lichHen;
        this.dichVu = dichVu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LichHenDichVuId)) return false;
        LichHenDichVuId that = (LichHenDichVuId) o;
        return Objects.equals(lichHen, that.lichHen) &&
               Objects.equals(dichVu, that.dichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lichHen, dichVu);
    }
}
