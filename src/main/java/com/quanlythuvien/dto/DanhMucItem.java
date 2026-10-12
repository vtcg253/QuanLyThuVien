package com.quanlythuvien.dto;

public record DanhMucItem(String ma, String ten, String diaChi, String sdt) {

    public DanhMucItem(String ma, String ten) {
        this(ma, ten, null, null);
    }
}
