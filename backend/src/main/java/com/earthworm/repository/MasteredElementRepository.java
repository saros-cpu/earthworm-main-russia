package com.earthworm.repository;

import com.earthworm.model.MasteredElement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MasteredElementRepository extends JpaRepository<MasteredElement, String> {
    List<MasteredElement> findByUserIdOrderByMasteredAtDesc(String userId);
}
