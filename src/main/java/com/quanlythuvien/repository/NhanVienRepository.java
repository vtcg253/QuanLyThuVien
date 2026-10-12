package com.quanlythuvien.repository;

import com.quanlythuvien.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NhanVienRepository extends JpaRepository<NhanVien, String> {

    List<NhanVien> findAllByOrderByHoTenAsc();
}
