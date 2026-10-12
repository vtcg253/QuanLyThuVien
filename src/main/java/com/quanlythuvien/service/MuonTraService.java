package com.quanlythuvien.service;

import com.quanlythuvien.dto.DongMuon;
import com.quanlythuvien.dto.PhieuMuonForm;
import com.quanlythuvien.dto.ThongKeTrangThaiMuon;
import com.quanlythuvien.entity.CtPhieuMuon;
import com.quanlythuvien.entity.CtPhieuMuonId;
import com.quanlythuvien.entity.PhieuMuon;
import com.quanlythuvien.entity.Sach;
import com.quanlythuvien.entity.TrangThaiChiTietMuon;
import com.quanlythuvien.exception.BusinessException;
import com.quanlythuvien.repository.CtPhieuMuonRepository;
import com.quanlythuvien.repository.DocGiaRepository;
import com.quanlythuvien.repository.NhanVienRepository;
import com.quanlythuvien.repository.PhieuMuonRepository;
import com.quanlythuvien.repository.SachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MuonTraService {

    private final PhieuMuonRepository phieuMuonRepository;
    private final CtPhieuMuonRepository ctPhieuMuonRepository;
    private final SachRepository sachRepository;
    private final DocGiaRepository docGiaRepository;
    private final NhanVienRepository nhanVienRepository;

    @Transactional(readOnly = true)
    public List<PhieuMuon> danhSachPhieu() {
        return phieuMuonRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public List<PhieuMuon> timPhieu(String keyword, String trangThai) {
        return locPhieu(danhSachPhieu(), keyword, trangThai);
    }

    public List<PhieuMuon> locPhieu(List<PhieuMuon> danhSach, String keyword, String trangThai) {
        String tuKhoa = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return danhSach.stream()
                .filter(phieu -> khopTrangThai(phieu, trangThai))
                .filter(phieu -> khopTuKhoa(phieu, tuKhoa))
                .toList();
    }

    @Transactional(readOnly = true)
    public PhieuMuon chiTietPhieu(String maPhieu) {
        return phieuMuonRepository.findByIdWithDetails(maPhieu)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu mượn " + maPhieu + "."));
    }

    @Transactional(readOnly = true)
    public long demPhieu() {
        return phieuMuonRepository.count();
    }

    @Transactional(readOnly = true)
    public long demDangMuon() {
        return phieuMuonRepository.findAll().stream()
                .filter(p -> p.getChiTiet() != null && !p.getChiTiet().isEmpty())
                .filter(p -> p.getChiTiet().stream().anyMatch(CtPhieuMuon::dangMuon))
                .count();
    }

    @Transactional(readOnly = true)
    public long demPhieuQuaHan() {
        return phieuMuonRepository.countPhieuQuaHan(LocalDate.now(), TrangThaiChiTietMuon.BORROWED);
    }

    @Transactional(readOnly = true)
    public String taoMaPhieuMoi() {
        Pattern pattern = Pattern.compile("^PM(\\d+)$", Pattern.CASE_INSENSITIVE);
        int maxSo = phieuMuonRepository.findAll().stream()
                .map(PhieuMuon::getMaPhieu)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0);
        return "PM" + String.format("%03d", maxSo + 1);
    }

    @Transactional(readOnly = true)
    public List<ThongKeTrangThaiMuon> thongKeTrangThaiMuon() {
        long dangMuon = 0;
        long quaHan = 0;
        long daTraDungHan = 0;
        long daTraTreHan = 0;
        long daMat = 0;
        LocalDate homNay = LocalDate.now();

        for (PhieuMuon phieu : phieuMuonRepository.findAllWithDetails()) {
            for (CtPhieuMuon chiTiet : phieu.getChiTiet()) {
                long soLuong = chiTiet.getSoLuong() == null ? 0 : chiTiet.getSoLuong();
                long soLuongMat = chiTiet.getTrangThai() == TrangThaiChiTietMuon.LOST
                        ? soLuong
                        : (chiTiet.getSoLuongMat() == null ? 0 : chiTiet.getSoLuongMat());
                daMat += soLuongMat;
                long soLuongKhongMat = Math.max(0, soLuong - soLuongMat);
                if (chiTiet.dangMuon()) {
                    if (phieu.getHanTra().isBefore(homNay)) {
                        quaHan += soLuongKhongMat;
                    } else {
                        dangMuon += soLuongKhongMat;
                    }
                } else if (chiTiet.daTra()) {
                    if (chiTiet.getNgayTra().isAfter(phieu.getHanTra())) {
                        daTraTreHan += soLuongKhongMat;
                    } else {
                        daTraDungHan += soLuongKhongMat;
                    }
                }
            }
        }

        long max = Math.max(Math.max(Math.max(dangMuon, quaHan), Math.max(daTraDungHan, daTraTreHan)), daMat);
        List<ThongKeTrangThaiMuon> thongKe = new ArrayList<>();
        themTrangThai(thongKe, "Đang mượn - còn hạn", dangMuon, max, "#31856b");
        themTrangThai(thongKe, "Đang mượn - quá hạn", quaHan, max, "#c34b43");
        themTrangThai(thongKe, "Đã trả đúng hạn", daTraDungHan, max, "#5687b8");
        themTrangThai(thongKe, "Đã trả trễ hạn", daTraTreHan, max, "#d39b3d");
        themTrangThai(thongKe, "Đã mất", daMat, max, "#8b5f78");
        return List.copyOf(thongKe);
    }

    private void themTrangThai(List<ThongKeTrangThaiMuon> thongKe, String nhan, long soLuong, long max, String mauSac) {
        int chieuCao = max == 0 ? 0 : (int) Math.round(soLuong * 100.0 / max);
        thongKe.add(new ThongKeTrangThaiMuon(nhan, soLuong, chieuCao, mauSac));
    }

    private boolean khopTrangThai(PhieuMuon phieu, String trangThai) {
        return switch (trangThai == null ? "ALL" : trangThai) {
            case "BORROWED" -> "Đang mượn".equals(phieu.trangThai());
            case "OVERDUE" -> "Quá hạn".equals(phieu.trangThai());
            case "RETURNED" -> "Đã trả".equals(phieu.trangThai());
            case "LOST" -> phieu.getChiTiet().stream().anyMatch(CtPhieuMuon::daMat);
            default -> true;
        };
    }

    private boolean khopTuKhoa(PhieuMuon phieu, String tuKhoa) {
        if (tuKhoa.isEmpty()) {
            return true;
        }
        if (chua(phieu.getMaPhieu(), tuKhoa)
                || chua(phieu.getMaDocGia(), tuKhoa)
                || (phieu.getDocGia() != null && chua(phieu.getDocGia().getHoTen(), tuKhoa))
                || chua(phieu.getMaNhanVien(), tuKhoa)
                || (phieu.getNhanVien() != null && chua(phieu.getNhanVien().getHoTen(), tuKhoa))) {
            return true;
        }
        return phieu.getChiTiet().stream().anyMatch(ct ->
                chua(ct.getMaSach(), tuKhoa)
                        || (ct.getSach() != null && chua(ct.getSach().getTenSach(), tuKhoa)));
    }

    private boolean chua(String giaTri, String tuKhoa) {
        return giaTri != null && giaTri.toLowerCase(Locale.ROOT).contains(tuKhoa);
    }

    @Transactional
    public String taoPhieuMuon(PhieuMuonForm form, String maNhanVien) {
        String maPhieu = taoMaPhieuMoi();
        form.setMaPhieu(maPhieu);
        if (!StringUtils.hasText(form.getMaDocGia()) || !docGiaRepository.existsById(form.getMaDocGia())) {
            throw new BusinessException("BR-01: Độc giả không hợp lệ.");
        }
        if (!StringUtils.hasText(maNhanVien) || !nhanVienRepository.existsById(maNhanVien)) {
            throw new BusinessException("Tài khoản đăng nhập chưa được liên kết với hồ sơ nhân viên hợp lệ.");
        }
        if (form.getNgayMuon() == null || form.getHanTra() == null) {
            throw new BusinessException("Ngày mượn và hạn trả không được để trống.");
        }
        // BR-04
        if (form.getHanTra().isBefore(form.getNgayMuon())) {
            throw new BusinessException("BR-04: Hạn trả phải lớn hơn hoặc bằng ngày mượn.");
        }

        List<DongMuon> chiTietHopLe = layDongMuonHopLe(form.getChiTiet());
        Map<String, Integer> gopSach = gopSoLuongMuon(chiTietHopLe);
        if (gopSach.isEmpty()) {
            throw new BusinessException("BR-05: Phiếu mượn phải có ít nhất một đầu sách.");
        }

        PhieuMuon phieu = new PhieuMuon();
        phieu.setMaPhieu(maPhieu);
        phieu.setMaDocGia(form.getMaDocGia());
        phieu.setMaNhanVien(maNhanVien);
        phieu.setNgayMuon(form.getNgayMuon());
        phieu.setHanTra(form.getHanTra());

        for (Map.Entry<String, Integer> entry : gopSach.entrySet()) {
            String maSach = entry.getKey();
            int soLuongMuon = entry.getValue();
            Sach sach = sachRepository.findById(maSach)
                    .orElseThrow(() -> new BusinessException("BR-01: Không tìm thấy sách mã " + maSach));

            int tonKho = sach.getSoLuong() == null ? 0 : sach.getSoLuong();
            // BR-02
            if (tonKho <= 0) {
                throw new BusinessException("BR-02: Sách [" + sach.getTenSach() + "] đã hết, không thể mượn.");
            }
            if (soLuongMuon > tonKho) {
                throw new BusinessException("BR-02: Sách [" + sach.getTenSach()
                        + "] chỉ còn " + tonKho + " cuốn, không đủ để mượn " + soLuongMuon + ".");
            }

            // BR-03: mượn tự động trừ kho
            sach.setSoLuong(tonKho - soLuongMuon);
            sachRepository.save(sach);

            CtPhieuMuon ct = new CtPhieuMuon();
            ct.setMaPhieu(maPhieu);
            ct.setMaSach(maSach);
            ct.setSoLuong(soLuongMuon);
            ct.setNgayTra(null);
            ct.setTrangThai(TrangThaiChiTietMuon.BORROWED);
            phieu.themChiTiet(ct);
        }

        phieuMuonRepository.save(phieu);
        return maPhieu;
    }

    @Transactional
    public void traSach(String maPhieu, String maSach) {
        CtPhieuMuonId id = new CtPhieuMuonId(maPhieu, maSach);
        CtPhieuMuon ct = ctPhieuMuonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("BR-05: Không tìm thấy dòng mượn sách trên phiếu " + maPhieu));

        // BR-05: không trả hai lần
        if (!ct.dangMuon()) {
            throw new BusinessException("BR-05: Dòng sách này đã được xử lý với trạng thái " + ct.getTrangThai() + ".");
        }

        ct.setNgayTra(LocalDate.now());
        ct.setTrangThai(TrangThaiChiTietMuon.RETURNED);
        ctPhieuMuonRepository.save(ct);

        Sach sach = sachRepository.findById(maSach)
                .orElseThrow(() -> new BusinessException("BR-01: Không tìm thấy sách mã " + maSach));
        int tonKho = sach.getSoLuong() == null ? 0 : sach.getSoLuong();
        // BR-03: trả tự động cộng hoàn kho
        int soLuongTra = ct.soLuongConDangMuon();
        sach.setSoLuong(tonKho + soLuongTra);
        sachRepository.save(sach);
    }

    @Transactional
    public void baoMatSach(String maPhieu, String maSach, int soLuongMat) {
        CtPhieuMuonId id = new CtPhieuMuonId(maPhieu, maSach);
        CtPhieuMuon ct = ctPhieuMuonRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy dòng mượn sách trên phiếu " + maPhieu));
        if (!ct.dangMuon()) {
            throw new BusinessException("Chỉ có thể báo mất sách đang được mượn.");
        }
        int soLuongConDangMuon = ct.soLuongConDangMuon();
        if (soLuongMat <= 0 || soLuongMat > soLuongConDangMuon) {
            throw new BusinessException("Số lượng báo mất phải từ 1 đến " + soLuongConDangMuon + ".");
        }
        int tongSoLuongMat = (ct.getSoLuongMat() == null ? 0 : ct.getSoLuongMat()) + soLuongMat;
        ct.setSoLuongMat(tongSoLuongMat);
        if (tongSoLuongMat == ct.getSoLuong()) {
            ct.setTrangThai(TrangThaiChiTietMuon.LOST);
        }
        ctPhieuMuonRepository.save(ct);
    }

    private List<DongMuon> layDongMuonHopLe(List<DongMuon> dongMuons) {
        if (dongMuons == null) {
            return List.of();
        }
        return dongMuons.stream()
                .filter(dong -> dong != null)
                .filter(dong -> StringUtils.hasText(dong.getMaSach()))
                .toList();
    }

    private Map<String, Integer> gopSoLuongMuon(List<DongMuon> dongMuons) {
        Map<String, Integer> map = new LinkedHashMap<>();
        if (dongMuons == null) {
            return map;
        }
        for (DongMuon dong : dongMuons) {
            if (dong == null || !StringUtils.hasText(dong.getMaSach())) {
                continue;
            }
            int sl = dong.getSoLuong() == null ? 0 : dong.getSoLuong();
            if (sl <= 0) {
                throw new BusinessException("BR-05: Số lượng mượn phải lớn hơn 0.");
            }
            String ma = dong.getMaSach().trim();
            map.merge(ma, sl, Integer::sum);
        }
        return map;
    }
}
