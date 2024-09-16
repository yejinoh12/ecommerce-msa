package com.userservice.repository;

import com.userservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    //기존의 기본 배송지 비활성화
    @Modifying
    @Transactional
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.isDefault = true")
    void updatePreviousDefaultAddressToFalse(@Param("userId") Long userId);

    //기본 배송지 조회
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findDefaultAddressByUserId(@Param("userId") Long userId);

    //사용자 ID로 모든 배송지 조회, 기본 배송지가 먼저 오도록
    List<Address> findAllByUserIdOrderByIsDefaultDesc(Long userId);

}