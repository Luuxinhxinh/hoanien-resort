package com.kawai.repositories;

import com.kawai.models.FolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho FolioItem entity.
 * Lưu vết các khoản phí (ăn uống, tour, ...) được ghi nợ vào Folio phòng.
 */
@Repository
public interface FolioItemRepository extends JpaRepository<FolioItem, Long> {
}