package com.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    //별칭
    @Column(nullable = false)
    private String alias;

    //우편번호
    @Column(nullable = false)
    private String postalCode;

    //기본 주소
    @Column(nullable = false)
    private String address;

    //상세 주소
    private String detailAddress;

    //수령인 전화번호
    @Column(nullable = false)
    private String phone;

    //기본 배송지 여부
    @Setter
    @Column(columnDefinition = "TINYINT(1)", nullable = false)
    private boolean isDefault;

}
