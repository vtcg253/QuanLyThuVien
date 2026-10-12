package com.quanlythuvien.controller;

import com.quanlythuvien.service.MuonTraService;
import com.quanlythuvien.service.SachService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SachService sachService;
    private final MuonTraService muonTraService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("tongSach", sachService.tongSoSach());
        model.addAttribute("tongTonKho", sachService.tongTonKho());
        model.addAttribute("bieuDoMuonSach", sachService.thongKeMuonSach());
        model.addAttribute("thongKeTonKho", sachService.thongKeTonKhoTheoSach());
        model.addAttribute("thongKeTrangThaiMuon", muonTraService.thongKeTrangThaiMuon());
        model.addAttribute("tongPhieu", muonTraService.demPhieu());
        model.addAttribute("tongDangMuon", muonTraService.demDangMuon());
        model.addAttribute("tongPhieuQuaHan", muonTraService.demPhieuQuaHan());
        model.addAttribute("pageTitle", "Dashboard quản lý");
        return "dashboard";
    }
}
