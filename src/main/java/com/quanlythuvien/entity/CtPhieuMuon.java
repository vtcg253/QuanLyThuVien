package com.quanlythuvien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "CT_PHIEUMUON")
@IdClass(CtPhieuMuonId.class)
public class CtPhieuMuon {

    @Id
    @Column(name = "MAPM")
    private String maPhieu;

    @Id
    @Column(name = "MASACH")
    private String maSach;

    @Column(name = "SOLUONG", nullable = false)
    private Integer soLuong;

    @Column(name = "NGAYTRA")
    private LocalDate ngayTra;

    @Column(name = "SOLUONGMAT", nullable = false)
    private Integer soLuongMat = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "TRANGTHAI", nullable = false, length = 20)
    private TrangThaiChiTietMuon trangThai = TrangThaiChiTietMuon.BORROWED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MAPM", insertable = false, updatable = false)
    private PhieuMuon phieuMuon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MASACH", insertable = false, updatable = false)
    private Sach sach;

    public boolean daTra() {
        return trangThai == TrangThaiChiTietMuon.RETURNED;
    }

    public boolean dangMuon() {
        return trangThai == TrangThaiChiTietMuon.BORROWED;
    }

    public boolean daMat() {
        return trangThai == TrangThaiChiTietMuon.LOST || soLuongMat > 0;
    }

    public int soLuongConDangMuon() {
        return Math.max(0, (soLuong == null ? 0 : soLuong) - (soLuongMat == null ? 0 : soLuongMat));
    }
}
