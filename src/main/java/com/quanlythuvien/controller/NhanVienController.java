package com.quanlythuvien.controller;

import com.quanlythuvien.dto.NhanVienForm;
import com.quanlythuvien.dto.PageSlice;
import com.quanlythuvien.exception.BusinessException;
import com.quanlythuvien.service.NhanVienService;
import com.quanlythuvien.util.PaginationUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class NhanVienController {

    private final NhanVienService nhanVienService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/nhanvien")
    public String danhSach(@RequestParam(defaultValue = "1") int page, Model model) {
        NhanVienForm form = model.containsAttribute("form")
                ? (NhanVienForm) model.getAttribute("form")
                : new NhanVienForm();
        boolean moDialog = Boolean.TRUE.equals(model.getAttribute("moDialog"));
        napTrang(model, form, moDialog, page);
        return "nhanvien/list";
    }

    @GetMapping("/nhanvien/sua/{ma}")
    public String sua(@PathVariable String ma, @RequestParam(defaultValue = "1") int page,
                      Model model, RedirectAttributes ra) {
        try {
            napTrang(model, nhanVienService.timForm(ma), true, page);
            return "nhanvien/list";
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/nhanvien";
        }
    }

    @PostMapping("/nhanvien/luu")
    public String luu(@ModelAttribute NhanVienForm form, RedirectAttributes ra) {
        try {
            var saved = nhanVienService.luu(form);
            ra.addFlashAttribute("success", "Đã lưu nhân viên " + saved.maNhanVien() + " - " + saved.hoTen() + ".");
        } catch (BusinessException ex) {
            form.setMatKhau(null);
            form.setXacNhanMatKhau(null);
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("moDialog", true);
        }
        return "redirect:/nhanvien";
    }

    @PostMapping("/nhanvien/xoa/{ma}")
    public String xoa(@PathVariable String ma, Authentication authentication, RedirectAttributes ra) {
        try {
            if (authentication != null && StringUtils.hasText(authentication.getName())) {
                NhanVienForm currentAccount = nhanVienService.timFormByUsername(authentication.getName());
                if (ma.equals(currentAccount.getMaNhanVien())) {
                    throw new BusinessException("Không thể xóa nhân viên đang đăng nhập.");
                }
            }
            nhanVienService.xoa(ma);
            ra.addFlashAttribute("success", "Đã xóa nhân viên " + ma + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/nhanvien";
    }

    private void napTrang(Model model, NhanVienForm form, boolean moDialog, int requestedPage) {
        if (!StringUtils.hasText(form.getMaNhanVien()) && !moDialog) {
            form = new NhanVienForm();
        }
        List<com.quanlythuvien.dto.NhanVienItem> dsNhanVien = nhanVienService.danhSach();
        String maNhanVien = form.getMaNhanVien();
        boolean dangSua = moDialog && dsNhanVien.stream()
                .anyMatch(nhanVien -> nhanVien.maNhanVien().equals(maNhanVien));
        PageSlice<?> pagination = PageSlice.of(dsNhanVien, requestedPage);
        model.addAttribute("dsNhanVien", pagination.items());
        model.addAttribute("pagination", pagination);
        model.addAttribute("paginationUrl", PaginationUrls.forPath("/nhanvien", Map.of()));
        model.addAttribute("form", form);
        model.addAttribute("dangSua", dangSua);
        model.addAttribute("moDialog", moDialog);
        model.addAttribute("pageTitle", "Quản lý nhân viên");
    }

}
