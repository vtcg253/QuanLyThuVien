package com.quanlythuvien.repository;

import com.quanlythuvien.entity.Sach;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SachRepository extends JpaRepository<Sach, String> {

    long countByMaTacGia(String maTacGia);

    long countByMaTheLoai(String maTheLoai);

    long countByMaNxb(String maNxb);
}
