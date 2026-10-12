package com.quanlythuvien.dto;

import java.time.LocalDate;

public record NhanVienItem(
        String maNhanVien,
        String hoTen,
        LocalDate ngaySinh,
        String dienThoai,
        String diaChi,
        String tenDangNhap,
        String vaiTro,
        boolean coTaiKhoan
) {
}
