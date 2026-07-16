package com.earthworm.service;

import com.earthworm.model.BattleRoom;
import com.earthworm.repository.BattleRoomRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BattleService {
    private final BattleRoomRepository battleRoomRepository;

    public BattleService(BattleRoomRepository battleRoomRepository) {
        this.battleRoomRepository = battleRoomRepository;
    }

    public Map<String, Object> createRoom(String userId, String coursePackId) {
        BattleRoom room = new BattleRoom();
        room.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        room.setCreatorId(userId);
        room.setCoursePackId(coursePackId);
        room.setStatus("waiting");
        battleRoomRepository.save(room);
        return Map.of("roomId", room.getId(), "status", room.getStatus());
    }

    public Map<String, Object> joinRoom(String userId, String roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found"));
        if (!"waiting".equals(room.getStatus())) {
            throw new IllegalStateException("Room is not available");
        }
        if (room.getCreatorId().equals(userId)) {
            throw new IllegalStateException("Cannot join your own room");
        }
        room.setOpponentId(userId);
        room.setStatus("playing");
        battleRoomRepository.save(room);
        return Map.of("roomId", room.getId(), "coursePackId", room.getCoursePackId(), "status", room.getStatus());
    }

    public Map<String, Object> submitScore(String userId, String roomId, int score) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found"));
        if (!"playing".equals(room.getStatus())) {
            throw new IllegalStateException("Room is not in playing state");
        }
        if (room.getCreatorId().equals(userId)) {
            room.setCreatorScore(score);
        } else if (userId.equals(room.getOpponentId())) {
            room.setOpponentScore(score);
        } else {
            throw new IllegalArgumentException("User is not in this room");
        }
        if (room.getCreatorScore() != null && room.getOpponentScore() != null) {
            room.setStatus("finished");
        }
        battleRoomRepository.save(room);
        return Map.of("roomId", room.getId(), "status", room.getStatus());
    }

    public Map<String, Object> getResult(String roomId) {
        BattleRoom room = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Room not found"));
        Map<String, Object> result = new HashMap<>();
        result.put("roomId", room.getId());
        result.put("status", room.getStatus());
        result.put("creatorId", room.getCreatorId());
        result.put("opponentId", room.getOpponentId());
        result.put("creatorScore", room.getCreatorScore());
        result.put("opponentScore", room.getOpponentScore());
        result.put("coursePackId", room.getCoursePackId());
        if ("finished".equals(room.getStatus()) && room.getCreatorScore() != null && room.getOpponentScore() != null) {
            if (room.getCreatorScore() > room.getOpponentScore()) {
                result.put("winner", room.getCreatorId());
            } else if (room.getOpponentScore() > room.getCreatorScore()) {
                result.put("winner", room.getOpponentId());
            } else {
                result.put("winner", "draw");
            }
        }
        return result;
    }
}
