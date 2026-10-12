package com.quanlythuvien.repository;

import com.quanlythuvien.entity.DocGia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocGiaRepository extends JpaRepository<DocGia, String> {

    List<DocGia> findAllByOrderByHoTenAsc();
}
