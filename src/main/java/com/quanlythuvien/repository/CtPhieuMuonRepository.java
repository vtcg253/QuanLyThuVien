package com.quanlythuvien.repository;

import com.quanlythuvien.entity.CtPhieuMuon;
import com.quanlythuvien.entity.CtPhieuMuonId;
import com.quanlythuvien.entity.TrangThaiChiTietMuon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CtPhieuMuonRepository extends JpaRepository<CtPhieuMuon, CtPhieuMuonId> {

    @Query("""
            SELECT COUNT(c) FROM CtPhieuMuon c
            WHERE c.maSach = :maSach AND c.trangThai = :trangThai
            """)
    long countDangMuonByMaSach(@Param("maSach") String maSach,
                                @Param("trangThai") TrangThaiChiTietMuon trangThai);
}
