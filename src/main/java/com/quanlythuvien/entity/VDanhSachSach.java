package com.quanlythuvien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Immutable;

/**
 * Ánh xạ View Oracle V_DANH_SACH_SACH (chỉ đọc).
 * Cột thực tế: MASACH, TENSACH, TENTHELOAI, TENTACGIA, TENNXB, NAMXUATBAN, TON_KHO.
 */
@Getter
@Entity
@Immutable
@Table(name = "V_DANH_SACH_SACH")
public class VDanhSachSach {

    @Id
    @Column(name = "MASACH")
    private String maSach;

    @Column(name = "TENSACH")
    private String tenSach;

    @Column(name = "TENTHELOAI")
    private String tenTheLoai;

    @Column(name = "TENTACGIA")
    private String tenTacGia;

    @Column(name = "TENNXB")
    private String tenNxb;

    @Column(name = "NAMXUATBAN")
    private Integer namXb;

    @Column(name = "TON_KHO")
    private Integer soLuong;
}
