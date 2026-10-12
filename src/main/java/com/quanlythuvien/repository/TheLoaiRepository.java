package com.quanlythuvien.repository;

import com.quanlythuvien.entity.TheLoai;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheLoaiRepository extends JpaRepository<TheLoai, String> {

    boolean existsByTenTheLoaiIgnoreCase(String tenTheLoai);

    boolean existsByTenTheLoaiIgnoreCaseAndMaTheLoaiNot(String tenTheLoai, String maTheLoai);
}
