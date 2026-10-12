package com.quanlythuvien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "NHANVIEN")
public class NhanVien {

    @Id
    @Column(name = "MANV")
    private String maNhanVien;

    @Column(name = "HOTEN")
    private String hoTen;

    @Column(name = "NGAYSINH")
    private LocalDate ngaySinh;

    @Column(name = "SDT")
    private String dienThoai;

    @Column(name = "DIACHI")
    private String diaChi;
}
