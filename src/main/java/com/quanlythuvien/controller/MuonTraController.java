package com.quanlythuvien.controller;

import com.quanlythuvien.dto.DongMuon;
import com.quanlythuvien.dto.DocGiaForm;
import com.quanlythuvien.dto.DocGiaResponse;
import com.quanlythuvien.dto.PhieuMuonForm;
import com.quanlythuvien.dto.PageSlice;
import com.quanlythuvien.entity.DocGia;
import com.quanlythuvien.entity.PhieuMuon;
import com.quanlythuvien.entity.NhanVien;
import com.quanlythuvien.exception.BusinessException;
import com.quanlythuvien.repository.NhanVienRepository;
import com.quanlythuvien.repository.TaiKhoanRepository;
import com.quanlythuvien.service.DocGiaService;
import com.quanlythuvien.service.DanhMucService;
import com.quanlythuvien.service.MuonTraService;
import com.quanlythuvien.service.SachService;
import com.quanlythuvien.util.PaginationUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/muontra")
@RequiredArgsConstructor
public class MuonTraController {

    private final MuonTraService muonTraService;
    private final DanhMucService danhMucService;
    private final SachService sachService;
    private final DocGiaService docGiaService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanVienRepository nhanVienRepository;

    @GetMapping
    public String danhSach(@RequestParam(value = "q", required = false) String q,
                           @RequestParam(value = "status", defaultValue = "ALL") String status,
                           @RequestParam(defaultValue = "1") int page,
                           Model model) {
        String keyword = StringUtils.hasText(q) ? q.trim() : "";
        String filter = List.of("ALL", "BORROWED", "OVERDUE", "RETURNED", "LOST").contains(status) ? status : "ALL";
        List<PhieuMuon> tatCaPhieu = muonTraService.danhSachPhieu();
        List<PhieuMuon> matchedLoans = muonTraService.locPhieu(tatCaPhieu, keyword, filter);
        PageSlice<PhieuMuon> pagination = PageSlice.of(matchedLoans, page);
        model.addAttribute("dsPhieu", pagination.items());
        model.addAttribute("pagination", pagination);
        model.addAttribute("paginationUrl", PaginationUrls.forPath("/muontra",
                Map.of("q", keyword, "status", filter)));
        model.addAttribute("soPhieuPhuHop", matchedLoans.size());
        model.addAttribute("tongPhieu", tatCaPhieu.size());
        model.addAttribute("dangMuon", tatCaPhieu.stream().filter(p -> "Đang mượn".equals(p.trangThai())).count());
        model.addAttribute("quaHan", tatCaPhieu.stream().filter(p -> "Quá hạn".equals(p.trangThai())).count());
        model.addAttribute("daTra", tatCaPhieu.stream().filter(p -> "Đã trả".equals(p.trangThai())).count());
        model.addAttribute("q", keyword);
        model.addAttribute("status", filter);
        model.addAttribute("pageTitle", "Mượn / Trả sách");
        return "muontra/list";
    }

    @GetMapping("/{maPhieu}")
    public String chiTiet(@PathVariable String maPhieu, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("phieu", muonTraService.chiTietPhieu(maPhieu));
            model.addAttribute("pageTitle", "Chi tiết phiếu " + maPhieu);
            return "muontra/detail";
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/muontra";
        }
    }

    @GetMapping("/tao")
    public String formTao(Authentication authentication, Model model, RedirectAttributes ra) {
        NhanVien nhanVien = nhanVienDangNhap(authentication);
        if (nhanVien == null) {
            ra.addFlashAttribute("error", "Tài khoản đăng nhập chưa được liên kết với hồ sơ nhân viên.");
            return "redirect:/muontra";
        }
        if (!model.containsAttribute("form")) {
            PhieuMuonForm form = new PhieuMuonForm();
            form.setMaPhieu(muonTraService.taoMaPhieuMoi());
            form.setChiTiet(new ArrayList<>(List.of(new DongMuon())));
            model.addAttribute("form", form);
        } else if (model.getAttribute("form") instanceof PhieuMuonForm existingForm) {
            existingForm.setMaPhieu(muonTraService.taoMaPhieuMoi());
            existingForm.setChiTiet(normalizeChiTiet(existingForm.getChiTiet()));
        }
        napForm(model);
        model.addAttribute("nhanVienLapPhieu", nhanVien);
        return "muontra/form";
    }

    private List<DongMuon> normalizeChiTiet(List<DongMuon> chiTiet) {
        List<DongMuon> result = new ArrayList<>();
        if (chiTiet == null) {
            return result;
        }
        for (DongMuon dong : chiTiet) {
            if (dong == null) {
                continue;
            }
            if (!StringUtils.hasText(dong.getMaSach()) && (dong.getSoLuong() == null || dong.getSoLuong() <= 0)) {
                continue;
            }
            result.add(dong);
        }
        if (result.isEmpty()) {
            result.add(new DongMuon());
        }
        return result;
    }

    @PostMapping("/tao")
    public String tao(@ModelAttribute PhieuMuonForm form, Authentication authentication, RedirectAttributes ra) {
        try {
            NhanVien nhanVien = nhanVienDangNhap(authentication);
            if (nhanVien == null) {
                throw new BusinessException("Tài khoản đăng nhập chưa được liên kết với hồ sơ nhân viên.");
            }
            String maPhieu = muonTraService.taoPhieuMuon(form, nhanVien.getMaNhanVien());
            ra.addFlashAttribute("success", "Đã lập phiếu mượn " + maPhieu + " và trừ kho.");
            return "redirect:/muontra";
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("form", form);
            return "redirect:/muontra/tao";
        }
    }

    @PostMapping("/docgia")
    @ResponseBody
    public ResponseEntity<DocGiaResponse> themDocGia(@ModelAttribute DocGiaForm form) {
        try {
            DocGia docGia = docGiaService.luu(form);
            return ResponseEntity.ok(new DocGiaResponse(docGia.getMaDocGia(), docGia.getHoTen(), null));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(new DocGiaResponse(null, null, ex.getMessage()));
        }
    }

    @PostMapping("/{maPhieu}/tra/{maSach}")
    public String tra(@PathVariable String maPhieu, @PathVariable String maSach, RedirectAttributes ra) {
        try {
            muonTraService.traSach(maPhieu, maSach);
            ra.addFlashAttribute("success", "Đã trả sách " + maSach + " trên phiếu " + maPhieu + " và hoàn kho.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/muontra";
    }

    @PostMapping("/{maPhieu}/mat/{maSach}")
    public String baoMat(@PathVariable String maPhieu, @PathVariable String maSach,
                         @RequestParam int soLuongMat, RedirectAttributes ra) {
        try {
            muonTraService.baoMatSach(maPhieu, maSach, soLuongMat);
            ra.addFlashAttribute("success", "Đã ghi nhận " + soLuongMat + " cuốn sách " + maSach
                    + " bị mất trên phiếu " + maPhieu + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/muontra";
    }

    private void napForm(Model model) {
        model.addAttribute("dsDocGia", danhMucService.docGia());
        model.addAttribute("dsSach", sachService.danhSachTuView(null));
        model.addAttribute("pageTitle", "Lập phiếu mượn");
    }

    private NhanVien nhanVienDangNhap(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return taiKhoanRepository.findByTenDangNhapIgnoreCase(authentication.getName())
                .filter(taiKhoan -> "Y".equalsIgnoreCase(taiKhoan.getKichHoat()))
                .flatMap(taiKhoan -> nhanVienRepository.findById(taiKhoan.getMaNhanVien()))
                .orElse(null);
    }
}
