package com.quanlythuvien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "DOCGIA")
public class DocGia {

    @Id
    @Column(name = "MADG")
    private String maDocGia;

    @Column(name = "HOTEN")
    private String hoTen;

    @Column(name = "NGAYSINH")
    private LocalDate ngaySinh;

    @Column(name = "SDT")
    private String dienThoai;

    @Column(name = "DIACHI")
    private String diaChi;
}
