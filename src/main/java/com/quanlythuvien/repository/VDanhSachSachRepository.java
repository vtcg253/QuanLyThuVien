package com.quanlythuvien.repository;

import com.quanlythuvien.entity.VDanhSachSach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VDanhSachSachRepository extends JpaRepository<VDanhSachSach, String> {

    @Query("""
            SELECT v FROM VDanhSachSach v
            WHERE UPPER(v.maSach) LIKE UPPER(CONCAT('%', :q, '%'))
               OR UPPER(v.tenSach) LIKE UPPER(CONCAT('%', :q, '%'))
               OR UPPER(v.tenTheLoai) LIKE UPPER(CONCAT('%', :q, '%'))
               OR UPPER(v.tenTacGia) LIKE UPPER(CONCAT('%', :q, '%'))
               OR UPPER(v.tenNxb) LIKE UPPER(CONCAT('%', :q, '%'))
            ORDER BY v.tenSach
            """)
    List<VDanhSachSach> search(@Param("q") String q);

    @Query("""
            SELECT v.maSach AS maSach, v.tenSach AS tenSach, COALESCE(SUM(c.soLuong), 0) AS soLuongMuon
            FROM VDanhSachSach v
            LEFT JOIN CtPhieuMuon c ON c.maSach = v.maSach
            GROUP BY v.maSach, v.tenSach
            ORDER BY COALESCE(SUM(c.soLuong), 0) DESC, v.tenSach
            """)
    List<ThongKeMuonRow> thongKeSoLuongMuonTheoSach();
}
