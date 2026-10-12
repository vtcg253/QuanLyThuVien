package com.quanlythuvien.controller;

import com.quanlythuvien.dto.TacGiaResponse;
import com.quanlythuvien.dto.TheLoaiResponse;
import com.quanlythuvien.dto.NhaXuatBanResponse;
import com.quanlythuvien.dto.PageSlice;
import com.quanlythuvien.entity.TheLoai;
import com.quanlythuvien.entity.Sach;
import com.quanlythuvien.exception.BusinessException;
import com.quanlythuvien.service.DanhMucService;
import com.quanlythuvien.service.SachService;
import com.quanlythuvien.util.PaginationUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/sach")
@RequiredArgsConstructor
public class SachController {

    private final SachService sachService;
    private final DanhMucService danhMucService;

    @GetMapping
    public String danhSach(@RequestParam(value = "q", required = false) String q,
                           @RequestParam(defaultValue = "1") int page, Model model) {
        String keyword = StringUtils.hasText(q) ? q.trim() : "";
        PageSlice<?> pagination = PageSlice.of(sachService.danhSachTuView(keyword), page);
        model.addAttribute("dsSach", pagination.items());
        model.addAttribute("pagination", pagination);
        model.addAttribute("paginationUrl", PaginationUrls.forPath("/sach", Map.of("q", keyword)));
        model.addAttribute("q", keyword);
        model.addAttribute("pageTitle", "Danh sách sách");
        return "sach/list";
    }

    @GetMapping("/them")
    public String formThem(Model model) {
        Sach sach = new Sach();
        sach.setMaSach(sachService.taoMaSachMoi());
        sach.setSoLuong(0);
        napForm(model, sach, false);
        return "sach/form";
    }

    @GetMapping("/sua/{maSach}")
    public String formSua(@PathVariable String maSach, Model model, RedirectAttributes ra) {
        try {
            napForm(model, sachService.timTheoMa(maSach), true);
            return "sach/form";
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/sach";
        }
    }

    @PostMapping("/luu")
    public String luu(@ModelAttribute Sach sach,
                      @RequestParam("isEdit") boolean isEdit,
                      RedirectAttributes ra) {
        try {
            if (isEdit) {
                sachService.capNhat(sach);
                ra.addFlashAttribute("success", "Đã cập nhật sách " + sach.getMaSach());
            } else {
                sachService.them(sach);
                ra.addFlashAttribute("success", "Đã thêm sách " + sach.getMaSach());
            }
            return "redirect:/sach";
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            ra.addFlashAttribute("sach", sach);
            return isEdit ? "redirect:/sach/sua/" + sach.getMaSach() : "redirect:/sach/them";
        }
    }

    @PostMapping("/tac-gia")
    @ResponseBody
    public ResponseEntity<TacGiaResponse> themTacGia(@RequestParam("tenTacGia") String tenTacGia,
                                                      @RequestParam(value = "diaChi", required = false) String diaChi) {
        try {
            var tacGia = sachService.themTacGia(tenTacGia, diaChi);
            return ResponseEntity.ok(new TacGiaResponse(tacGia.getMaTacGia(), tacGia.getTenTacGia(), null));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(new TacGiaResponse(null, null, ex.getMessage()));
        }
    }

    @PostMapping("/the-loai")
    @ResponseBody
    public ResponseEntity<TheLoaiResponse> themTheLoai(@RequestParam("tenTheLoai") String tenTheLoai) {
        try {
            TheLoai theLoai = sachService.themTheLoai(tenTheLoai);
            return ResponseEntity.ok(new TheLoaiResponse(theLoai.getMaTheLoai(), theLoai.getTenTheLoai(), null));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(new TheLoaiResponse(null, null, ex.getMessage()));
        }
    }

    @PostMapping("/nha-xuat-ban")
    @ResponseBody
    public ResponseEntity<NhaXuatBanResponse> themNhaXuatBan(
            @RequestParam("tenNxb") String tenNxb,
            @RequestParam(value = "diaChi", required = false) String diaChi,
            @RequestParam(value = "sdt", required = false) String sdt) {
        try {
            var nhaXuatBan = sachService.themNhaXuatBan(tenNxb, diaChi, sdt);
            return ResponseEntity.ok(new NhaXuatBanResponse(
                    nhaXuatBan.getMaNxb(), nhaXuatBan.getTenNxb(), null));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(new NhaXuatBanResponse(null, null, ex.getMessage()));
        }
    }

    @PostMapping("/xoa/{maSach}")
    public String xoa(@PathVariable String maSach, RedirectAttributes ra) {
        try {
            sachService.xoa(maSach);
            ra.addFlashAttribute("success", "Đã xóa sách " + maSach);
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/sach";
    }

    private void napForm(Model model, Sach sach, boolean isEdit) {
        model.addAttribute("sach", sach);
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("dsTheLoai", danhMucService.theLoai());
        model.addAttribute("dsTacGia", danhMucService.tacGia());
        model.addAttribute("dsNxb", danhMucService.nhaXuatBan());
        model.addAttribute("pageTitle", isEdit ? "Sửa sách" : "Thêm sách");
    }
}
