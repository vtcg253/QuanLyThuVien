package com.quanlythuvien.repository;

import com.quanlythuvien.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {

    Optional<TaiKhoan> findByTenDangNhapIgnoreCase(String tenDangNhap);

    boolean existsByTenDangNhapIgnoreCase(String tenDangNhap);

    boolean existsByTenDangNhapIgnoreCaseAndMaNhanVienNot(String tenDangNhap, String maNhanVien);

    List<TaiKhoan> findAllByOrderByTenDangNhapAsc();
}
