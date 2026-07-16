package com.earthworm.repository;

import com.earthworm.model.BattleRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BattleRoomRepository extends JpaRepository<BattleRoom, String> {
}
