package com.quanlythuvien.service;

import com.quanlythuvien.dto.DocGiaForm;
import com.quanlythuvien.entity.DocGia;
import com.quanlythuvien.exception.BusinessException;
import com.quanlythuvien.repository.DocGiaRepository;
import com.quanlythuvien.repository.PhieuMuonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DocGiaService {

    private static final Pattern MA_DOC_GIA_PATTERN = Pattern.compile("^DG(\\d+)$");

    private final DocGiaRepository docGiaRepository;
    private final PhieuMuonRepository phieuMuonRepository;

    @Transactional(readOnly = true)
    public List<DocGia> danhSach() {
        return docGiaRepository.findAllByOrderByHoTenAsc();
    }

    @Transactional(readOnly = true)
    public DocGia timTheoMa(String maDocGia) {
        return docGiaRepository.findById(maDocGia)
                .orElseThrow(() -> new BusinessException("Không tìm thấy độc giả " + maDocGia + "."));
    }

    @Transactional
    public DocGia luu(DocGiaForm form) {
        String hoTen = form.getHoTen() == null ? "" : form.getHoTen().trim();
        if (!StringUtils.hasText(hoTen)) {
            throw new BusinessException("Vui lòng nhập họ tên độc giả.");
        }
        if (hoTen.length() > 100) {
            throw new BusinessException("Họ tên độc giả không được vượt quá 100 ký tự.");
        }
        if (form.getDienThoai() != null && form.getDienThoai().length() > 15) {
            throw new BusinessException("Số điện thoại không được vượt quá 15 ký tự.");
        }
        if (form.getDiaChi() != null && form.getDiaChi().length() > 200) {
            throw new BusinessException("Địa chỉ không được vượt quá 200 ký tự.");
        }

        DocGia docGia = StringUtils.hasText(form.getMaDocGia())
                ? timTheoMa(form.getMaDocGia())
                : new DocGia();
        if (!StringUtils.hasText(form.getMaDocGia())) {
            docGia.setMaDocGia(taoMaMoi());
        }
        docGia.setHoTen(hoTen);
        docGia.setNgaySinh(form.getNgaySinh());
        docGia.setDienThoai(form.getDienThoai() == null ? null : form.getDienThoai().trim());
        docGia.setDiaChi(form.getDiaChi() == null ? null : form.getDiaChi().trim());
        return docGiaRepository.save(docGia);
    }

    @Transactional
    public void xoa(String maDocGia) {
        if (!docGiaRepository.existsById(maDocGia)) {
            throw new BusinessException("Không tìm thấy độc giả " + maDocGia + ".");
        }
        if (phieuMuonRepository.existsByMaDocGia(maDocGia)) {
            throw new BusinessException("Không thể xóa độc giả đã có phiếu mượn.");
        }
        docGiaRepository.deleteById(maDocGia);
    }

    private String taoMaMoi() {
        int maLonNhat = docGiaRepository.findAll().stream()
                .map(DocGia::getMaDocGia)
                .map(MA_DOC_GIA_PATTERN::matcher)
                .filter(Matcher::matches)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0);
        String maMoi;
        do {
            maLonNhat++;
            maMoi = "DG" + String.format("%02d", maLonNhat);
        } while (docGiaRepository.existsById(maMoi));
        return maMoi;
    }
}
