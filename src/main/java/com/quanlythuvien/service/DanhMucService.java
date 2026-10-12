package com.quanlythuvien.service;

import com.quanlythuvien.entity.DocGia;
import com.quanlythuvien.entity.NhaXuatBan;
import com.quanlythuvien.entity.NhanVien;
import com.quanlythuvien.entity.TacGia;
import com.quanlythuvien.entity.TheLoai;
import com.quanlythuvien.repository.DocGiaRepository;
import com.quanlythuvien.repository.NhaXuatBanRepository;
import com.quanlythuvien.repository.NhanVienRepository;
import com.quanlythuvien.repository.TacGiaRepository;
import com.quanlythuvien.repository.TheLoaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DanhMucService {

    private final TheLoaiRepository theLoaiRepository;
    private final TacGiaRepository tacGiaRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final DocGiaRepository docGiaRepository;
    private final NhanVienRepository nhanVienRepository;

    @Transactional(readOnly = true)
    public List<TheLoai> theLoai() {
        return theLoaiRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TacGia> tacGia() {
        return tacGiaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<NhaXuatBan> nhaXuatBan() {
        return nhaXuatBanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DocGia> docGia() {
        return docGiaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<NhanVien> nhanVien() {
        return nhanVienRepository.findAll();
    }
}
