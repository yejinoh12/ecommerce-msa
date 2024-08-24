package com.common.entity;


import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Column(updatable = false) //최초 생성 시간만 저장하고 변하지 않음
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column
    @LastModifiedDate //변경 시간 저장(변경이 생길 때마다 업데이트)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime modifiedAt;

}
