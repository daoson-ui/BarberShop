-- ================================
-- TẠO DATABASE
-- ================================
DROP DATABASE IF EXISTS barbershop;
CREATE DATABASE barbershop;
USE barbershop;

-- ================================
-- BẢNG ACCOUNT
-- ================================
CREATE TABLE account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(30) NOT NULL
);

-- DATA MẪU
INSERT INTO account(username, password, role) VALUES
('admin', '123', 'ROLE_ADMIN'),
('user1', '123', 'ROLE_USER');

-- ================================
-- BẢNG KHÁCH HÀNG
-- ================================
CREATE TABLE khach_hang (
    makh INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten VARCHAR(100),
    gioi_tinh VARCHAR(10),
    ngay_sinh DATE,
    sdt VARCHAR(20)
);

-- DATA MẪU
INSERT INTO khach_hang(ho_ten, gioi_tinh, ngay_sinh, sdt) VALUES
('Nguyen Van A', 'Nam', '1995-01-10', '0901234567'),
('Tran Thi B', 'Nu', '1998-03-22', '0909876543');

-- ================================
-- BẢNG CA LÀM
-- ================================
CREATE TABLE ca_lam (
    ma_ca INT AUTO_INCREMENT PRIMARY KEY,
    ngay_lam_viec DATE,
    gio_bat_dau TIME,
    gio_ket_thuc TIME
);

INSERT INTO ca_lam(ngay_lam_viec, gio_bat_dau, gio_ket_thuc) VALUES
('2025-01-01', '08:00:00', '12:00:00'),
('2025-01-01', '13:00:00', '17:00:00');

-- ================================
-- BẢNG NHÂN VIÊN
-- ================================
CREATE TABLE nhan_vien (
    manv INT AUTO_INCREMENT PRIMARY KEY,
    ho_ten VARCHAR(100),
    sdt VARCHAR(20),
    gioi_tinh VARCHAR(10),
    ngay_sinh DATE,
    chuc_vu VARCHAR(50),
    ngay_vao_lam DATE,
    ma_ca INT,
    FOREIGN KEY (ma_ca) REFERENCES ca_lam(ma_ca)
);

INSERT INTO nhan_vien(ho_ten, sdt, gioi_tinh, ngay_sinh, chuc_vu, ngay_vao_lam, ma_ca)
VALUES
('Hoang Tuan', '0911111111', 'Nam', '1990-05-10', 'Tho cat toc', '2023-02-01', 1),
('Pham Kien', '0922222222', 'Nam', '1992-07-12', 'Tho cat toc', '2023-04-10', 2);

-- ================================
-- BẢNG DỊCH VỤ
-- ================================
CREATE TABLE dich_vu (
    ma_dv INT AUTO_INCREMENT PRIMARY KEY,
    ten_dv VARCHAR(100),
    gia DECIMAL(10,2),
    thoi_gian_thuc_hien INT
);

INSERT INTO dich_vu(ten_dv, gia, thoi_gian_thuc_hien) VALUES
('Cat toc', 50000, 20),
('Goi dau', 30000, 10),
('Nhuom toc', 200000, 60);

-- ================================
-- BẢNG LỊCH HẸN
-- ================================
CREATE TABLE lich_hen (
    ma_lh INT AUTO_INCREMENT PRIMARY KEY,
    ngay_gio_bat_dau DATETIME,
    ngay_gio_ket_thuc DATETIME,
    makh INT,
    manv INT,
    FOREIGN KEY (makh) REFERENCES khach_hang(makh),
    FOREIGN KEY (manv) REFERENCES nhan_vien(manv)
);

INSERT INTO lich_hen(ngay_gio_bat_dau, ngay_gio_ket_thuc, makh, manv)
VALUES
('2025-01-03 09:00:00', '2025-01-03 09:30:00', 1, 1),
('2025-01-03 10:00:00', '2025-01-03 11:00:00', 2, 2);

-- ================================
-- BẢNG LỊCH HẸN – DỊCH VỤ (Nhiều – Nhiều)
-- ================================
CREATE TABLE lich_hen_dich_vu (
    ma_lh INT,
    ma_dv INT,
    so_luong INT DEFAULT 1,
    ghi_chu VARCHAR(200),
    PRIMARY KEY (ma_lh, ma_dv),
    FOREIGN KEY (ma_lh) REFERENCES lich_hen(ma_lh),
    FOREIGN KEY (ma_dv) REFERENCES dich_vu(ma_dv)
);

INSERT INTO lich_hen_dich_vu(ma_lh, ma_dv, so_luong, ghi_chu) VALUES
(1, 1, 1, 'Cat toc nhanh'),
(2, 3, 1, 'Nhuom mau xanh');

-- ================================
-- BẢNG HÓA ĐƠN
-- ================================
CREATE TABLE hoa_don (
    ma_hd INT AUTO_INCREMENT PRIMARY KEY,
    ngay_thanh_toan DATE,
    tong_tien DECIMAL(10,2),
    phuong_thuc_tt VARCHAR(50),
    ma_lh INT,
    FOREIGN KEY (ma_lh) REFERENCES lich_hen(ma_lh)
);

INSERT INTO hoa_don(ngay_thanh_toan, tong_tien, phuong_thuc_tt, ma_lh)
VALUES
('2025-01-03', 50000, 'Tien mat', 1),
('2025-01-03', 200000, 'Chuyen khoan', 2);
