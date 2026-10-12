package com.quanlythuvien.repository;

import com.quanlythuvien.entity.NhaXuatBan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhaXuatBanRepository extends JpaRepository<NhaXuatBan, String> {

    boolean existsByTenNxbIgnoreCase(String tenNxb);

    boolean existsByTenNxbIgnoreCaseAndMaNxbNot(String tenNxb, String maNxb);
}
