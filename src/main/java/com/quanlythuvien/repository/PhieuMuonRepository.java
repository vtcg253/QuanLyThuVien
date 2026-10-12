package com.quanlythuvien.repository;

import com.quanlythuvien.entity.PhieuMuon;
import com.quanlythuvien.entity.TrangThaiChiTietMuon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PhieuMuonRepository extends JpaRepository<PhieuMuon, String> {

    boolean existsByMaDocGia(String maDocGia);

    boolean existsByMaNhanVien(String maNhanVien);

    @Query("""
            SELECT COUNT(DISTINCT p.maPhieu) FROM PhieuMuon p
            JOIN p.chiTiet c
            WHERE c.trangThai = :trangThai AND p.hanTra < :homNay
            """)
    long countPhieuQuaHan(@Param("homNay") LocalDate homNay,
                          @Param("trangThai") TrangThaiChiTietMuon trangThai);

    @Query("""
            SELECT DISTINCT p FROM PhieuMuon p
            LEFT JOIN FETCH p.docGia
            LEFT JOIN FETCH p.nhanVien
            LEFT JOIN FETCH p.chiTiet c
            LEFT JOIN FETCH c.sach
            ORDER BY p.ngayMuon DESC, p.maPhieu DESC
            """)
    List<PhieuMuon> findAllWithDetails();

    @Query("""
            SELECT p FROM PhieuMuon p
            LEFT JOIN FETCH p.docGia
            LEFT JOIN FETCH p.nhanVien
            LEFT JOIN FETCH p.chiTiet c
            LEFT JOIN FETCH c.sach
            WHERE p.maPhieu = :maPhieu
            """)
    Optional<PhieuMuon> findByIdWithDetails(@Param("maPhieu") String maPhieu);
}
