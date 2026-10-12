package com.quanlythuvien.repository;

import com.quanlythuvien.entity.TacGia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TacGiaRepository extends JpaRepository<TacGia, String> {

    boolean existsByTenTacGiaIgnoreCase(String tenTacGia);

    boolean existsByTenTacGiaIgnoreCaseAndMaTacGiaNot(String tenTacGia, String maTacGia);
}
