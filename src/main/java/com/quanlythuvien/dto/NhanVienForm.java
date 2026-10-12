package com.quanlythuvien.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class NhanVienForm {

    private String maNhanVien;
    private String hoTen;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngaySinh;

    private String dienThoai;
    private String diaChi;
    private String tenDangNhap;
    private String matKhau;
    private String xacNhanMatKhau;
}
