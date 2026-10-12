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
@Table(name = "SACH")
public class Sach {

    @Id
    @Column(name = "MASACH")
    private String maSach;

    @Column(name = "TENSACH", nullable = false)
    private String tenSach;

    @Column(name = "MATL")
    private String maTheLoai;

    @Column(name = "MATG")
    private String maTacGia;

    @Column(name = "MANXB")
    private String maNxb;

    @Column(name = "NAMXUATBAN")
    private Integer namXb;

    @Column(name = "SOLUONG")
    private Integer soLuong;
}
