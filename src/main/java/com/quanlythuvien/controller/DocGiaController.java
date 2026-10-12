package com.quanlythuvien.controller;

import com.quanlythuvien.dto.DocGiaForm;
import com.quanlythuvien.dto.PageSlice;
import com.quanlythuvien.entity.DocGia;
import com.quanlythuvien.exception.BusinessException;
import com.quanlythuvien.service.DocGiaService;
import com.quanlythuvien.util.PaginationUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/docgia")
@RequiredArgsConstructor
public class DocGiaController {

    private final DocGiaService docGiaService;

    @GetMapping
    public String danhSach(@RequestParam(defaultValue = "1") int page, Model model) {
        DocGiaForm form = model.containsAttribute("form")
                ? (DocGiaForm) model.getAttribute("form")
                : new DocGiaForm();
        boolean moDialog = Boolean.TRUE.equals(model.getAttribute("moDialog"));
        napTrang(model, form, moDialog, page);
        return "docgia/list";
    }

    @GetMapping("/sua/{ma}")
    public String sua(@PathVariable String ma, @RequestParam(defaultValue = "1") int page,
                      Model model, RedirectAttributes ra) {
        try {
            DocGia docGia = docGiaService.timTheoMa(ma);
            DocGiaForm form = new DocGiaForm();
            form.setMaDocGia(docGia.getMaDocGia());
            form.setHoTen(docGia.getHoTen());
            form.setNgaySinh(docGia.getNgaySinh());
            form.setDienThoai(docGia.getDienThoai());
            form.setDiaChi(docGia.getDiaChi());
            napTrang(model, form, true, page);
            return "docgia/list";
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/docgia";
        }
    }

    @PostMapping("/luu")
    public String luu(@ModelAttribute DocGiaForm form, RedirectAttributes ra) {
        try {
            DocGia saved = docGiaService.luu(form);
            ra.addFlashAttribute("success", "Đã lưu độc giả " + saved.getMaDocGia() + " - " + saved.getHoTen() + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("form", form);
            ra.addFlashAttribute("moDialog", true);
        }
        return "redirect:/docgia";
    }

    @PostMapping("/xoa/{ma}")
    public String xoa(@PathVariable String ma, RedirectAttributes ra) {
        try {
            docGiaService.xoa(ma);
            ra.addFlashAttribute("success", "Đã xóa độc giả " + ma + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/docgia";
    }

    private void napTrang(Model model, DocGiaForm form, boolean moDialog, int requestedPage) {
        if (!StringUtils.hasText(form.getMaDocGia()) && !moDialog) {
            form = new DocGiaForm();
        }
        PageSlice<?> pagination = PageSlice.of(docGiaService.danhSach(), requestedPage);
        model.addAttribute("dsDocGia", pagination.items());
        model.addAttribute("pagination", pagination);
        model.addAttribute("paginationUrl", PaginationUrls.forPath("/docgia", Map.of()));
        model.addAttribute("form", form);
        model.addAttribute("dangSua", StringUtils.hasText(form.getMaDocGia()));
        model.addAttribute("moDialog", moDialog);
        model.addAttribute("pageTitle", "Quản lý độc giả");
    }
}
