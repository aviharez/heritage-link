package com.project.heritagelink.repository;

import com.project.heritagelink.model.Item;
import com.project.heritagelink.model.enums.DispositionType;
import com.project.heritagelink.model.enums.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByStatus(ItemStatus status, Pageable pageable);
    Page<Item> findByDispositionType(DispositionType dispositionType, Pageable pageable);
    Page<Item> findByRoomOfOriginIgnoreCase(String roomOfOrigin, Pageable pageable);
    Page<Item> findByStatusAndDispositionType(ItemStatus status, DispositionType dispositionType, Pageable pageable);

    List<Item> findByMediationRequired(Boolean mediationRequired);
    List<Item> findBySentimentalScoreGreaterThanEqual(Integer score);
}
