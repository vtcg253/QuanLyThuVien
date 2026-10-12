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
@Table(name = "THELOAI")
public class TheLoai {

    @Id
    @Column(name = "MATL")
    private String maTheLoai;

    @Column(name = "TENTHELOAI")
    private String tenTheLoai;
}
