package com.quanlythuvien.controller;

import com.quanlythuvien.dto.DanhMucForm;
import com.quanlythuvien.dto.DanhMucItem;
import com.quanlythuvien.dto.PageSlice;
import com.quanlythuvien.entity.NhaXuatBan;
import com.quanlythuvien.entity.TacGia;
import com.quanlythuvien.entity.TheLoai;
import com.quanlythuvien.exception.BusinessException;
import com.quanlythuvien.service.DanhMucService;
import com.quanlythuvien.service.SachService;
import com.quanlythuvien.util.PaginationUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/danhmuc")
@RequiredArgsConstructor
public class DanhMucController {

    private final DanhMucService danhMucService;
    private final SachService sachService;

    @GetMapping("/tac-gia")
    public String danhSachTacGia(@RequestParam(defaultValue = "1") int page, Model model) {
        List<DanhMucItem> items = danhMucService.tacGia().stream()
                .map(tacGia -> new DanhMucItem(tacGia.getMaTacGia(), tacGia.getTenTacGia(), tacGia.getDiaChi(), null))
                .toList();
        return hienThiDanhMuc(model, "tac-gia", "Tác giả", items, null, page);
    }

    @GetMapping("/the-loai")
    public String danhSachTheLoai(@RequestParam(defaultValue = "1") int page, Model model) {
        List<DanhMucItem> items = danhMucService.theLoai().stream()
                .map(theLoai -> new DanhMucItem(theLoai.getMaTheLoai(), theLoai.getTenTheLoai()))
                .toList();
        return hienThiDanhMuc(model, "the-loai", "Thể loại", items, null, page);
    }

    @GetMapping("/nha-xuat-ban")
    public String danhSachNhaXuatBan(@RequestParam(defaultValue = "1") int page, Model model) {
        List<DanhMucItem> items = danhMucService.nhaXuatBan().stream()
                .map(nxb -> new DanhMucItem(nxb.getMaNxb(), nxb.getTenNxb(), nxb.getDiaChi(), nxb.getSdt()))
                .toList();
        return hienThiDanhMuc(model, "nha-xuat-ban", "Nhà xuất bản", items, null, page);
    }

    @GetMapping("/tac-gia/sua/{ma}")
    public String suaTacGia(@PathVariable String ma, @RequestParam(defaultValue = "1") int page,
                            Model model, RedirectAttributes ra) {
        TacGia tacGia = danhMucService.tacGia().stream()
                .filter(item -> item.getMaTacGia().equals(ma))
                .findFirst()
                .orElse(null);
        if (tacGia == null) {
            ra.addFlashAttribute("error", "Không tìm thấy tác giả " + ma + ".");
            return "redirect:/danhmuc/tac-gia";
        }
        List<DanhMucItem> items = danhMucService.tacGia().stream()
                .map(item -> new DanhMucItem(item.getMaTacGia(), item.getTenTacGia(), item.getDiaChi(), null))
                .toList();
        DanhMucForm form = new DanhMucForm();
        form.setMa(tacGia.getMaTacGia());
        form.setTen(tacGia.getTenTacGia());
        form.setDiaChi(tacGia.getDiaChi());
        return hienThiDanhMuc(model, "tac-gia", "Tác giả", items, form, page);
    }

    @GetMapping("/the-loai/sua/{ma}")
    public String suaTheLoai(@PathVariable String ma, @RequestParam(defaultValue = "1") int page,
                             Model model, RedirectAttributes ra) {
        TheLoai theLoai = danhMucService.theLoai().stream()
                .filter(item -> item.getMaTheLoai().equals(ma))
                .findFirst()
                .orElse(null);
        if (theLoai == null) {
            ra.addFlashAttribute("error", "Không tìm thấy thể loại " + ma + ".");
            return "redirect:/danhmuc/the-loai";
        }
        List<DanhMucItem> items = danhMucService.theLoai().stream()
                .map(item -> new DanhMucItem(item.getMaTheLoai(), item.getTenTheLoai()))
                .toList();
        DanhMucForm form = new DanhMucForm();
        form.setMa(theLoai.getMaTheLoai());
        form.setTen(theLoai.getTenTheLoai());
        return hienThiDanhMuc(model, "the-loai", "Thể loại", items, form, page);
    }

    @GetMapping("/nha-xuat-ban/sua/{ma}")
    public String suaNhaXuatBan(@PathVariable String ma, @RequestParam(defaultValue = "1") int page,
                                Model model, RedirectAttributes ra) {
        NhaXuatBan nhaXuatBan = danhMucService.nhaXuatBan().stream()
                .filter(item -> item.getMaNxb().equals(ma))
                .findFirst()
                .orElse(null);
        if (nhaXuatBan == null) {
            ra.addFlashAttribute("error", "Không tìm thấy nhà xuất bản " + ma + ".");
            return "redirect:/danhmuc/nha-xuat-ban";
        }
        List<DanhMucItem> items = danhMucService.nhaXuatBan().stream()
                .map(item -> new DanhMucItem(item.getMaNxb(), item.getTenNxb(), item.getDiaChi(), item.getSdt()))
                .toList();
        DanhMucForm form = new DanhMucForm();
        form.setMa(nhaXuatBan.getMaNxb());
        form.setTen(nhaXuatBan.getTenNxb());
        form.setDiaChi(nhaXuatBan.getDiaChi());
        form.setSdt(nhaXuatBan.getSdt());
        return hienThiDanhMuc(model, "nha-xuat-ban", "Nhà xuất bản", items, form, page);
    }

    @PostMapping("/tac-gia/luu")
    public String luuTacGia(@ModelAttribute DanhMucForm form, RedirectAttributes ra) {
        try {
            TacGia saved = form.getMa() == null || form.getMa().isBlank()
                    ? sachService.themTacGia(form.getTen(), form.getDiaChi())
                    : sachService.capNhatTacGia(form.getMa(), form.getTen(), form.getDiaChi());
            ra.addFlashAttribute("success", "Đã lưu tác giả " + saved.getMaTacGia() + " - " + saved.getTenTacGia() + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/danhmuc/tac-gia";
    }

    @PostMapping("/the-loai/luu")
    public String luuTheLoai(@ModelAttribute DanhMucForm form, RedirectAttributes ra) {
        try {
            TheLoai saved = form.getMa() == null || form.getMa().isBlank()
                    ? sachService.themTheLoai(form.getTen())
                    : sachService.capNhatTheLoai(form.getMa(), form.getTen());
            ra.addFlashAttribute("success", "Đã lưu thể loại " + saved.getMaTheLoai() + " - " + saved.getTenTheLoai() + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/danhmuc/the-loai";
    }

    @PostMapping("/nha-xuat-ban/luu")
    public String luuNhaXuatBan(@ModelAttribute DanhMucForm form, RedirectAttributes ra) {
        try {
            NhaXuatBan saved = form.getMa() == null || form.getMa().isBlank()
                    ? sachService.themNhaXuatBan(form.getTen(), form.getDiaChi(), form.getSdt())
                    : sachService.capNhatNhaXuatBan(form.getMa(), form.getTen(), form.getDiaChi(), form.getSdt());
            ra.addFlashAttribute("success", "Đã lưu nhà xuất bản " + saved.getMaNxb() + " - " + saved.getTenNxb() + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/danhmuc/nha-xuat-ban";
    }

    @PostMapping("/tac-gia/xoa/{ma}")
    public String xoaTacGia(@PathVariable String ma, RedirectAttributes ra) {
        try {
            sachService.xoaTacGia(ma);
            ra.addFlashAttribute("success", "Đã xóa tác giả " + ma + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/danhmuc/tac-gia";
    }

    @PostMapping("/the-loai/xoa/{ma}")
    public String xoaTheLoai(@PathVariable String ma, RedirectAttributes ra) {
        try {
            sachService.xoaTheLoai(ma);
            ra.addFlashAttribute("success", "Đã xóa thể loại " + ma + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/danhmuc/the-loai";
    }

    @PostMapping("/nha-xuat-ban/xoa/{ma}")
    public String xoaNhaXuatBan(@PathVariable String ma, RedirectAttributes ra) {
        try {
            sachService.xoaNhaXuatBan(ma);
            ra.addFlashAttribute("success", "Đã xóa nhà xuất bản " + ma + ".");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/danhmuc/nha-xuat-ban";
    }

    private String hienThiDanhMuc(Model model, String loai, String tenDanhMuc,
                                  List<DanhMucItem> allItems, DanhMucForm form, int requestedPage) {
        boolean dangSua = form != null;
        if (form == null) {
            form = new DanhMucForm();
            form.setMa(switch (loai) {
                case "tac-gia" -> sachService.taoMaTacGiaMoi();
                case "the-loai" -> sachService.taoMaTheLoaiMoi();
                case "nha-xuat-ban" -> sachService.taoMaNhaXuatBanMoi();
                default -> throw new IllegalArgumentException("Danh mục không được hỗ trợ: " + loai);
            });
        }
        PageSlice<DanhMucItem> pagination = PageSlice.of(allItems, requestedPage);
        model.addAttribute("items", pagination.items());
        model.addAttribute("pagination", pagination);
        model.addAttribute("paginationUrl", PaginationUrls.forPath("/danhmuc/" + loai, Map.of()));
        model.addAttribute("form", form);
        model.addAttribute("loai", loai);
        model.addAttribute("tenDanhMuc", tenDanhMuc);
        model.addAttribute("dangSua", dangSua);
        model.addAttribute("pageTitle", "Quản lý " + tenDanhMuc.toLowerCase());
        return "danhmuc/list";
    }
}
