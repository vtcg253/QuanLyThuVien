package com.quanlythuvien.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "NHAXUATBAN")
public class NhaXuatBan {

    @Id
    @Column(name = "MANXB")
    private String maNxb;

    @Column(name = "TENNXB")
    private String tenNxb;

    @Column(name = "DIACHI")
    private String diaChi;

    @Column(name = "SDT")
    private String sdt;
}
