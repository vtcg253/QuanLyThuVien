package com.quanlythuvien.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PhieuMuonForm {

    private String maPhieu;
    private String maDocGia;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate ngayMuon = LocalDate.now();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate hanTra = LocalDate.now().plusDays(14);

    private List<DongMuon> chiTiet = new ArrayList<>();
}
