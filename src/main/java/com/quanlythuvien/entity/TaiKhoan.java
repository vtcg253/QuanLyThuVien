package com.quanlythuvien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TAIKHOAN")
public class TaiKhoan {

    @Id
    @Column(name = "MANV")
    private String maNhanVien;

    @Column(name = "TENDANGNHAP", nullable = false, unique = true)
    private String tenDangNhap;

    @Column(name = "MATKHAU", nullable = false)
    private String matKhau;

    @Column(name = "VAITRO", nullable = false)
    private String vaiTro;

    @Column(name = "KICHHOAT", nullable = false)
    private String kichHoat;
}
