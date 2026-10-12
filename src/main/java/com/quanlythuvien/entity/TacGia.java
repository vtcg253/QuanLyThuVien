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
@Table(name = "TACGIA")
public class TacGia {

    @Id
    @Column(name = "MATG")
    private String maTacGia;

    @Column(name = "TENTACGIA")
    private String tenTacGia;

    @Column(name = "DIACHI", length = 200)
    private String diaChi;
}
