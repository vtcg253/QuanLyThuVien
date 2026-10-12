package com.quanlythuvien.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "PHIEUMUON")
public class PhieuMuon {

    @Id
    @Column(name = "MAPM")
    private String maPhieu;

    @Column(name = "MADG", nullable = false)
    private String maDocGia;

    @Column(name = "MANV", nullable = false)
    private String maNhanVien;

    @Column(name = "NGAYMUON", nullable = false)
    private LocalDate ngayMuon;

    @Column(name = "NGAYHANTRA", nullable = false)
    private LocalDate hanTra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MADG", insertable = false, updatable = false)
    private DocGia docGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANV", insertable = false, updatable = false)
    private NhanVien nhanVien;

    @OneToMany(mappedBy = "phieuMuon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CtPhieuMuon> chiTiet = new ArrayList<>();

    public void themChiTiet(CtPhieuMuon ct) {
        chiTiet.add(ct);
        ct.setPhieuMuon(this);
        ct.setMaPhieu(this.maPhieu);
    }

    public boolean daTraHet() {
        return chiTiet != null && !chiTiet.isEmpty()
                && chiTiet.stream().noneMatch(CtPhieuMuon::dangMuon);
    }

    public String trangThai() {
        if (daTraHet()) {
            if (chiTiet.stream().anyMatch(CtPhieuMuon::daMat)) {
                return "Có sách mất";
            }
            return "Đã trả";
        }
        if (hanTra != null && hanTra.isBefore(LocalDate.now())) {
            return "Quá hạn";
        }
        return "Đang mượn";
    }
}
