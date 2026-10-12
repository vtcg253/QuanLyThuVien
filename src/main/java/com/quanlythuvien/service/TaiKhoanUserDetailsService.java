package com.quanlythuvien.service;

import com.quanlythuvien.entity.TaiKhoan;
import com.quanlythuvien.repository.TaiKhoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaiKhoanUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản."));
        return User.withUsername(taiKhoan.getTenDangNhap())
                .password(taiKhoan.getMatKhau())
                .roles(taiKhoan.getVaiTro())
                .disabled(!"Y".equalsIgnoreCase(taiKhoan.getKichHoat()))
                .build();
    }
}
