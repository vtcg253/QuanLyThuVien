package com.quanlythuvien.service;

import com.quanlythuvien.dto.NhanVienForm;
import com.quanlythuvien.dto.NhanVienItem;
import com.quanlythuvien.entity.NhanVien;
import com.quanlythuvien.entity.TaiKhoan;
import com.quanlythuvien.exception.BusinessException;
import com.quanlythuvien.repository.NhanVienRepository;
import com.quanlythuvien.repository.PhieuMuonRepository;
import com.quanlythuvien.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NhanVienService {

    private static final Pattern MA_NHAN_VIEN_PATTERN = Pattern.compile("^NV(\\d+)$", Pattern.CASE_INSENSITIVE);
    private final NhanVienRepository nhanVienRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final PhieuMuonRepository phieuMuonRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<NhanVienItem> danhSach() {
        Map<String, TaiKhoan> taiKhoanTheoNhanVien = taiKhoanRepository.findAllByOrderByTenDangNhapAsc().stream()
                .collect(Collectors.toMap(TaiKhoan::getMaNhanVien, taiKhoan -> taiKhoan));
        return nhanVienRepository.findAllByOrderByHoTenAsc().stream()
                .map(nhanVien -> {
                    TaiKhoan taiKhoan = taiKhoanTheoNhanVien.get(nhanVien.getMaNhanVien());
                    return new NhanVienItem(
                            nhanVien.getMaNhanVien(),
                            nhanVien.getHoTen(),
                            nhanVien.getNgaySinh(),
                            nhanVien.getDienThoai(),
                            nhanVien.getDiaChi(),
                            taiKhoan == null ? null : taiKhoan.getTenDangNhap(),
                            taiKhoan == null ? null : taiKhoan.getVaiTro(),
                            taiKhoan != null
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public NhanVienForm timForm(String maNhanVien) {
        NhanVien nhanVien = nhanVienRepository.findById(maNhanVien)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên " + maNhanVien + "."));
        NhanVienForm form = new NhanVienForm();
        form.setMaNhanVien(nhanVien.getMaNhanVien());
        form.setHoTen(nhanVien.getHoTen());
        form.setNgaySinh(nhanVien.getNgaySinh());
        form.setDienThoai(nhanVien.getDienThoai());
        form.setDiaChi(nhanVien.getDiaChi());
        taiKhoanRepository.findById(maNhanVien).ifPresent(taiKhoan -> {
            form.setTenDangNhap(taiKhoan.getTenDangNhap());
        });
        return form;
    }

    @Transactional(readOnly = true)
    public NhanVienForm timFormByUsername(String tenDangNhap) {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapIgnoreCase(tenDangNhap)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản đang đăng nhập."));
        return timForm(taiKhoan.getMaNhanVien());
    }

    @Transactional
    public NhanVienItem luu(NhanVienForm form) {
        String hoTen = trim(form.getHoTen());
        if (!StringUtils.hasText(hoTen) || hoTen.length() > 100) {
            throw new BusinessException("Vui lòng nhập họ tên nhân viên (tối đa 100 ký tự).");
        }
        kiemTraDoDai(form.getDienThoai(), 15, "Số điện thoại");
        kiemTraDoDai(form.getDiaChi(), 200, "Địa chỉ");
        boolean taoMoi = !StringUtils.hasText(form.getMaNhanVien());
        NhanVien nhanVien = taoMoi ? new NhanVien() : nhanVienRepository.findById(form.getMaNhanVien())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên " + form.getMaNhanVien() + "."));
        if (taoMoi) {
            nhanVien.setMaNhanVien(taoMaNhanVienMoiNoTx());
        }
        nhanVien.setHoTen(hoTen);
        nhanVien.setNgaySinh(form.getNgaySinh());
        nhanVien.setDienThoai(trim(form.getDienThoai()));
        nhanVien.setDiaChi(trim(form.getDiaChi()));
        nhanVienRepository.save(nhanVien);

        String tenDangNhap = trim(form.getTenDangNhap());
        TaiKhoan taiKhoan = taiKhoanRepository.findById(nhanVien.getMaNhanVien()).orElse(null);
        if (taoMoi && !StringUtils.hasText(tenDangNhap)) {
            throw new BusinessException("Nhân viên mới cần có tên đăng nhập.");
        }
        if (StringUtils.hasText(tenDangNhap)) {
            if (tenDangNhap.length() > 50) {
                throw new BusinessException("Tên đăng nhập không được vượt quá 50 ký tự.");
            }
            boolean trungTen = taiKhoan == null
                    ? taiKhoanRepository.existsByTenDangNhapIgnoreCase(tenDangNhap)
                    : taiKhoanRepository.existsByTenDangNhapIgnoreCaseAndMaNhanVienNot(
                            tenDangNhap, nhanVien.getMaNhanVien());
            if (trungTen) {
                throw new BusinessException("Tên đăng nhập đã được sử dụng.");
            }
            String matKhau = form.getMatKhau();
            if (taiKhoan == null && (!StringUtils.hasText(matKhau) || matKhau.length() < 6)) {
                throw new BusinessException("Mật khẩu tài khoản mới phải có ít nhất 6 ký tự.");
            }
            if (StringUtils.hasText(matKhau) && matKhau.length() < 6) {
                throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự.");
            }
            if (StringUtils.hasText(matKhau) && !matKhau.equals(form.getXacNhanMatKhau())) {
                throw new BusinessException("Mật khẩu xác nhận không khớp.");
            }
            if (taiKhoan == null) {
                taiKhoan = new TaiKhoan();
                taiKhoan.setMaNhanVien(nhanVien.getMaNhanVien());
                taiKhoan.setKichHoat("Y");
                taiKhoan.setVaiTro("THU_THU");
            }
            taiKhoan.setTenDangNhap(tenDangNhap);
            if (StringUtils.hasText(matKhau)) {
                taiKhoan.setMatKhau(passwordEncoder.encode(matKhau));
            }
            taiKhoanRepository.save(taiKhoan);
        } else if (taiKhoan != null) {
            throw new BusinessException("Nhân viên đã có tài khoản. Nhập tên đăng nhập hiện tại để giữ hoặc đổi tài khoản.");
        }

        return new NhanVienItem(nhanVien.getMaNhanVien(), nhanVien.getHoTen(), nhanVien.getNgaySinh(),
                nhanVien.getDienThoai(), nhanVien.getDiaChi(),
                taiKhoan == null ? null : taiKhoan.getTenDangNhap(),
                taiKhoan == null ? null : taiKhoan.getVaiTro(), taiKhoan != null);
    }

    @Transactional
    public void xoa(String maNhanVien) {
        if (!nhanVienRepository.existsById(maNhanVien)) {
            throw new BusinessException("Không tìm thấy nhân viên " + maNhanVien + ".");
        }
        if (phieuMuonRepository.existsByMaNhanVien(maNhanVien)) {
            throw new BusinessException("Không thể xóa nhân viên đã lập phiếu mượn.");
        }
        taiKhoanRepository.deleteById(maNhanVien);
        nhanVienRepository.deleteById(maNhanVien);
    }

    @Transactional(readOnly = true)
    public List<NhanVien> nhanVienChuaCoTaiKhoan() {
        return nhanVienRepository.findAllByOrderByHoTenAsc().stream()
                .filter(nhanVien -> !taiKhoanRepository.existsById(nhanVien.getMaNhanVien()))
                .toList();
    }

    private String taoMaNhanVienMoiNoTx() {
        int max = nhanVienRepository.findAll().stream()
                .map(NhanVien::getMaNhanVien)
                .map(MA_NHAN_VIEN_PATTERN::matcher)
                .filter(Matcher::matches)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0);
        String ma;
        do {
            ma = "NV" + String.format("%02d", ++max);
        } while (nhanVienRepository.existsById(ma));
        return ma;
    }

    private void kiemTraDoDai(String value, int max, String tenTruong) {
        if (value != null && value.length() > max) {
            throw new BusinessException(tenTruong + " không được vượt quá " + max + " ký tự.");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
