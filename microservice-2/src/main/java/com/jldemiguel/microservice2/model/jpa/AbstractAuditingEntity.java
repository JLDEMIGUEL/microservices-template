package com.jldemiguel.microservice2.model.jpa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(value = {"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"}, allowGetters = true)
public abstract class AbstractAuditingEntity<T> implements Persistable<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public abstract T getId();

    @CreatedBy
    @Size(max = 50)
    @Column(value = "created_by")
    private String createdBy;

    @CreatedDate
    @Builder.Default
    @Column(value = "created_date")
    private Instant createdDate = Instant.now();

    @LastModifiedBy
    @Size(max = 50)
    @Column(value = "last_modified_by")
    private String lastModifiedBy;

    @LastModifiedDate
    @Builder.Default
    @Column(value = "last_modified_date")
    private Instant lastModifiedDate = Instant.now();

    @Transient
    private boolean isNew;

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew || getId() == null;
    }
}