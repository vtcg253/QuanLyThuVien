package com.quanlythuvien.service;

import com.quanlythuvien.dto.BieuDoMuonSach;
import com.quanlythuvien.dto.ThongKeMuonSach;
import com.quanlythuvien.dto.ThongKeTonKho;
import com.quanlythuvien.entity.Sach;
import com.quanlythuvien.entity.NhaXuatBan;
import com.quanlythuvien.entity.TrangThaiChiTietMuon;
import com.quanlythuvien.entity.TacGia;
import com.quanlythuvien.entity.TheLoai;
import com.quanlythuvien.entity.VDanhSachSach;
import com.quanlythuvien.exception.BusinessException;
import com.quanlythuvien.repository.CtPhieuMuonRepository;
import com.quanlythuvien.repository.NhaXuatBanRepository;
import com.quanlythuvien.repository.SachRepository;
import com.quanlythuvien.repository.TacGiaRepository;
import com.quanlythuvien.repository.ThongKeMuonRow;
import com.quanlythuvien.repository.TheLoaiRepository;
import com.quanlythuvien.repository.VDanhSachSachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SachService {

    private final SachRepository sachRepository;
    private final VDanhSachSachRepository viewRepository;
    private final TheLoaiRepository theLoaiRepository;
    private final TacGiaRepository tacGiaRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final CtPhieuMuonRepository ctPhieuMuonRepository;

    @Transactional(readOnly = true)
    public List<VDanhSachSach> danhSachTuView(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return viewRepository.findAll();
        }
        return viewRepository.search(keyword.trim());
    }

    @Transactional(readOnly = true)
    public long tongSoSach() {
        return sachRepository.count();
    }

    @Transactional(readOnly = true)
    public long tongTonKho() {
        return sachRepository.findAll().stream()
                .mapToLong(s -> s.getSoLuong() == null ? 0 : s.getSoLuong())
                .sum();
    }

    @Transactional(readOnly = true)
    public List<ThongKeTonKho> thongKeTonKhoTheoSach() {
        List<VDanhSachSach> dsSach = viewRepository.findAll();
        int tonKhoCaoNhat = dsSach.stream()
                .mapToInt(sach -> sach.getSoLuong() == null ? 0 : Math.max(sach.getSoLuong(), 0))
                .max()
                .orElse(0);

        return dsSach.stream()
                .map(sach -> {
                    int soLuong = sach.getSoLuong() == null ? 0 : sach.getSoLuong();
                    int chieuCao = tonKhoCaoNhat == 0
                            ? 0
                            : (int) Math.round(Math.max(soLuong, 0) * 100.0 / tonKhoCaoNhat);
                    return new ThongKeTonKho(sach.getMaSach(), sach.getTenSach(), soLuong, chieuCao);
                })
                .sorted((a, b) -> Integer.compare(b.soLuong(), a.soLuong()))
                .toList();
    }

    @Transactional
    public TacGia themTacGia(String tenTacGia, String diaChi) {
        String tenDaChuanHoa = chuanHoaTen(tenTacGia, "Tên tác giả");
        kiemTraTenTacGia(tenDaChuanHoa, null);
        TacGia tacGia = new TacGia();
        tacGia.setMaTacGia(maDanhMucMoi(tacGiaRepository.findAll().stream().map(TacGia::getMaTacGia).toList(), "TG"));
        tacGia.setTenTacGia(tenDaChuanHoa);
        tacGia.setDiaChi(chuanHoaDiaChi(diaChi));
        return tacGiaRepository.save(tacGia);
    }

    @Transactional
    public TheLoai themTheLoai(String tenTheLoai) {
        String tenDaChuanHoa = chuanHoaTen(tenTheLoai, "Tên thể loại");
        kiemTraTenTheLoai(tenDaChuanHoa, null);
        TheLoai theLoai = new TheLoai();
        theLoai.setMaTheLoai(maDanhMucMoi(theLoaiRepository.findAll().stream().map(TheLoai::getMaTheLoai).toList(), "TL"));
        theLoai.setTenTheLoai(tenDaChuanHoa);
        return theLoaiRepository.save(theLoai);
    }

    @Transactional
    public NhaXuatBan themNhaXuatBan(String tenNxb, String diaChi, String sdt) {
        String tenDaChuanHoa = chuanHoaTen(tenNxb, "Tên nhà xuất bản");
        kiemTraTenNhaXuatBan(tenDaChuanHoa, null);
        NhaXuatBan nhaXuatBan = new NhaXuatBan();
        nhaXuatBan.setMaNxb(maDanhMucMoi(nhaXuatBanRepository.findAll().stream()
                .map(NhaXuatBan::getMaNxb).toList(), "NXB"));
        capNhatThongTinNhaXuatBan(nhaXuatBan, tenDaChuanHoa, diaChi, sdt);
        return nhaXuatBanRepository.save(nhaXuatBan);
    }

    @Transactional
    public TacGia capNhatTacGia(String maTacGia, String tenTacGia, String diaChi) {
        TacGia tacGia = tacGiaRepository.findById(maTacGia)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tác giả " + maTacGia + "."));
        String tenDaChuanHoa = chuanHoaTen(tenTacGia, "Tên tác giả");
        kiemTraTenTacGia(tenDaChuanHoa, maTacGia);
        tacGia.setTenTacGia(tenDaChuanHoa);
        tacGia.setDiaChi(chuanHoaDiaChi(diaChi));
        return tacGiaRepository.save(tacGia);
    }

    @Transactional
    public TheLoai capNhatTheLoai(String maTheLoai, String tenTheLoai) {
        TheLoai theLoai = theLoaiRepository.findById(maTheLoai)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thể loại " + maTheLoai + "."));
        String tenDaChuanHoa = chuanHoaTen(tenTheLoai, "Tên thể loại");
        kiemTraTenTheLoai(tenDaChuanHoa, maTheLoai);
        theLoai.setTenTheLoai(tenDaChuanHoa);
        return theLoaiRepository.save(theLoai);
    }

    @Transactional
    public NhaXuatBan capNhatNhaXuatBan(String maNxb, String tenNxb, String diaChi, String sdt) {
        NhaXuatBan nhaXuatBan = nhaXuatBanRepository.findById(maNxb)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhà xuất bản " + maNxb + "."));
        String tenDaChuanHoa = chuanHoaTen(tenNxb, "Tên nhà xuất bản");
        kiemTraTenNhaXuatBan(tenDaChuanHoa, maNxb);
        capNhatThongTinNhaXuatBan(nhaXuatBan, tenDaChuanHoa, diaChi, sdt);
        return nhaXuatBanRepository.save(nhaXuatBan);
    }

    @Transactional
    public void xoaTacGia(String maTacGia) {
        if (!tacGiaRepository.existsById(maTacGia)) {
            throw new BusinessException("Không tìm thấy tác giả " + maTacGia + ".");
        }
        if (sachRepository.countByMaTacGia(maTacGia) > 0) {
            throw new BusinessException("Không thể xóa tác giả đang được sử dụng bởi sách.");
        }
        tacGiaRepository.deleteById(maTacGia);
    }

    @Transactional
    public void xoaTheLoai(String maTheLoai) {
        if (!theLoaiRepository.existsById(maTheLoai)) {
            throw new BusinessException("Không tìm thấy thể loại " + maTheLoai + ".");
        }
        if (sachRepository.countByMaTheLoai(maTheLoai) > 0) {
            throw new BusinessException("Không thể xóa thể loại đang được sử dụng bởi sách.");
        }
        theLoaiRepository.deleteById(maTheLoai);
    }

    @Transactional
    public void xoaNhaXuatBan(String maNxb) {
        if (!nhaXuatBanRepository.existsById(maNxb)) {
            throw new BusinessException("Không tìm thấy nhà xuất bản " + maNxb + ".");
        }
        if (sachRepository.countByMaNxb(maNxb) > 0) {
            throw new BusinessException("Không thể xóa nhà xuất bản đang được sử dụng bởi sách.");
        }
        nhaXuatBanRepository.deleteById(maNxb);
    }

    @Transactional(readOnly = true)
    public String taoMaTacGiaMoi() {
        return maDanhMucMoi(tacGiaRepository.findAll().stream().map(TacGia::getMaTacGia).toList(), "TG");
    }

    @Transactional(readOnly = true)
    public String taoMaTheLoaiMoi() {
        return maDanhMucMoi(theLoaiRepository.findAll().stream().map(TheLoai::getMaTheLoai).toList(), "TL");
    }

    @Transactional(readOnly = true)
    public String taoMaNhaXuatBanMoi() {
        return maDanhMucMoi(nhaXuatBanRepository.findAll().stream()
                .map(NhaXuatBan::getMaNxb).toList(), "NXB");
    }

    @Transactional(readOnly = true)
    public BieuDoMuonSach thongKeMuonSach() {
        List<ThongKeMuonRow> rows = viewRepository.thongKeSoLuongMuonTheoSach();
        long tongLuotMuon = rows.stream()
                .mapToLong(row -> row.getSoLuongMuon() == null ? 0 : row.getSoLuongMuon())
                .sum();
        long soLuongMuonCaoNhat = rows.stream()
                .mapToLong(row -> row.getSoLuongMuon() == null ? 0 : row.getSoLuongMuon())
                .max()
                .orElse(0);

        List<ThongKeMuonSach> sach = new java.util.ArrayList<>();
        List<String> phanVung = new java.util.ArrayList<>();
        double gocBatDau = 0;
        for (int i = 0; i < rows.size(); i++) {
            ThongKeMuonRow row = rows.get(i);
            long soLuongMuon = row.getSoLuongMuon() == null ? 0 : row.getSoLuongMuon();
            int hue = (i * 137) % 360;
            String mauSac = "hsl(" + hue + " 58% 42%)";
            int tiLe = tongLuotMuon == 0 ? 0 : (int) Math.round(soLuongMuon * 100.0 / tongLuotMuon);
            int chieuCao = soLuongMuonCaoNhat == 0 ? 0 : (int) Math.round(soLuongMuon * 100.0 / soLuongMuonCaoNhat);
            sach.add(new ThongKeMuonSach(row.getMaSach(), row.getTenSach(), soLuongMuon, tiLe, chieuCao, mauSac));

            if (soLuongMuon > 0) {
                double gocKetThuc = gocBatDau + soLuongMuon * 100.0 / tongLuotMuon;
                phanVung.add(mauSac + " " + String.format(Locale.ROOT, "%.2f%% %.2f%%", gocBatDau, gocKetThuc));
                gocBatDau = gocKetThuc;
            }
        }
        String gradient = tongLuotMuon == 0
                ? "#e8eee9 0% 100%"
                : String.join(", ", phanVung);
        return new BieuDoMuonSach(List.copyOf(sach), gradient, tongLuotMuon);
    }

    @Transactional(readOnly = true)
    public String taoMaSachMoi() {
        List<Sach> dsSach = sachRepository.findAll();
        int maxSo = 0;
        Pattern p = Pattern.compile("^S(\\d+)$", Pattern.CASE_INSENSITIVE);

        for (Sach sach : dsSach) {
            if (sach == null || !StringUtils.hasText(sach.getMaSach())) {
                continue;
            }
            Matcher m = p.matcher(sach.getMaSach().trim().toUpperCase());
            if (m.matches()) {
                int so = Integer.parseInt(m.group(1));
                maxSo = Math.max(maxSo, so);
            }
        }

        return "S" + String.format("%02d", maxSo + 1);
    }

    private String chuanHoaTen(String ten, String nhan) {
        if (!StringUtils.hasText(ten)) {
            throw new BusinessException(nhan + " không được để trống.");
        }
        return ten.trim();
    }

    private String chuanHoaDiaChi(String diaChi) {
        String diaChiDaChuanHoa = chuanHoaThongTinTuyChon(diaChi);
        if (diaChiDaChuanHoa != null && diaChiDaChuanHoa.length() > 200) {
            throw new BusinessException("Địa chỉ tác giả không được vượt quá 200 ký tự.");
        }
        return diaChiDaChuanHoa;
    }

    private void kiemTraTenTacGia(String tenTacGia, String maHienTai) {
        boolean trungTen = maHienTai == null
                ? tacGiaRepository.existsByTenTacGiaIgnoreCase(tenTacGia)
                : tacGiaRepository.existsByTenTacGiaIgnoreCaseAndMaTacGiaNot(tenTacGia, maHienTai);
        if (trungTen) {
            throw new BusinessException("Tác giả này đã tồn tại.");
        }
    }

    private void kiemTraTenTheLoai(String tenTheLoai, String maHienTai) {
        boolean trungTen = maHienTai == null
                ? theLoaiRepository.existsByTenTheLoaiIgnoreCase(tenTheLoai)
                : theLoaiRepository.existsByTenTheLoaiIgnoreCaseAndMaTheLoaiNot(tenTheLoai, maHienTai);
        if (trungTen) {
            throw new BusinessException("Thể loại này đã tồn tại.");
        }
    }

    private void kiemTraTenNhaXuatBan(String tenNxb, String maHienTai) {
        boolean trungTen = maHienTai == null
                ? nhaXuatBanRepository.existsByTenNxbIgnoreCase(tenNxb)
                : nhaXuatBanRepository.existsByTenNxbIgnoreCaseAndMaNxbNot(tenNxb, maHienTai);
        if (trungTen) {
            throw new BusinessException("Nhà xuất bản này đã tồn tại.");
        }
    }

    private void capNhatThongTinNhaXuatBan(NhaXuatBan nhaXuatBan, String tenNxb, String diaChi, String sdt) {
        String diaChiDaChuanHoa = chuanHoaThongTinTuyChon(diaChi);
        String sdtDaChuanHoa = chuanHoaThongTinTuyChon(sdt);
        if (tenNxb.length() > 100 || (diaChiDaChuanHoa != null && diaChiDaChuanHoa.length() > 200)
                || (sdtDaChuanHoa != null && sdtDaChuanHoa.length() > 15)) {
            throw new BusinessException("Tên, địa chỉ hoặc số điện thoại nhà xuất bản vượt quá độ dài cho phép.");
        }
        nhaXuatBan.setTenNxb(tenNxb);
        nhaXuatBan.setDiaChi(diaChiDaChuanHoa);
        nhaXuatBan.setSdt(sdtDaChuanHoa);
    }

    private String chuanHoaThongTinTuyChon(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String maDanhMucMoi(List<String> maHienCo, String tienTo) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(tienTo) + "(\\d+)$", Pattern.CASE_INSENSITIVE);
        int maxSo = maHienCo.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0);
        return tienTo + String.format("%02d", maxSo + 1);
    }

    @Transactional(readOnly = true)
    public Sach timTheoMa(String maSach) {
        return sachRepository.findById(maSach)
                .orElseThrow(() -> new BusinessException("BR-01: Không tìm thấy sách mã " + maSach));
    }

    @Transactional
    public void them(Sach sach) {
        validateSach(sach, true);
        sachRepository.save(sach);
    }

    @Transactional
    public void capNhat(Sach sach) {
        validateSach(sach, false);
        sachRepository.save(sach);
    }

    @Transactional
    public void xoa(String maSach) {
        if (!sachRepository.existsById(maSach)) {
            throw new BusinessException("BR-01: Không tồn tại sách mã " + maSach);
        }
        // BR-05: không xóa sách đang được mượn chưa trả
        if (ctPhieuMuonRepository.countDangMuonByMaSach(maSach, TrangThaiChiTietMuon.BORROWED) > 0) {
            throw new BusinessException("BR-05: Không thể xóa sách đang được mượn chưa trả.");
        }
        sachRepository.deleteById(maSach);
    }

    private void validateSach(Sach sach, boolean isNew) {
        if (isNew && !StringUtils.hasText(sach.getMaSach())) {
            sach.setMaSach(taoMaSachMoi());
        }
        if (!StringUtils.hasText(sach.getMaSach())) {
            throw new BusinessException("BR-01: Mã sách không được để trống.");
        }
        sach.setMaSach(sach.getMaSach().trim().toUpperCase());
        if (!StringUtils.hasText(sach.getTenSach())) {
            throw new BusinessException("Tên sách không được để trống.");
        }
        if (sach.getSoLuong() == null || sach.getSoLuong() < 0) {
            throw new BusinessException("Số lượng phải lớn hơn hoặc bằng 0.");
        }
        if (isNew && sachRepository.existsById(sach.getMaSach())) {
            throw new BusinessException("BR-01: Mã sách " + sach.getMaSach() + " đã tồn tại.");
        }
        if (!isNew && !sachRepository.existsById(sach.getMaSach())) {
            throw new BusinessException("BR-01: Không tìm thấy sách mã " + sach.getMaSach());
        }
        if (StringUtils.hasText(sach.getMaTheLoai())
                && !theLoaiRepository.existsById(sach.getMaTheLoai())) {
            throw new BusinessException("BR-01: Thể loại không tồn tại.");
        }
        if (StringUtils.hasText(sach.getMaTacGia())
                && !tacGiaRepository.existsById(sach.getMaTacGia())) {
            throw new BusinessException("BR-01: Tác giả không tồn tại.");
        }
        if (StringUtils.hasText(sach.getMaNxb())
                && !nhaXuatBanRepository.existsById(sach.getMaNxb())) {
            throw new BusinessException("BR-01: Nhà xuất bản không tồn tại.");
        }
    }
}
