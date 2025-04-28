package esprit.mindmatch.Service;

import esprit.mindmatch.Entities.Room;
import esprit.mindmatch.Repository.RoomRepository;
import esprit.mindmatch.Repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    SessionRepository sessionRepository;

    public Room addRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId).orElse(null);
    }


    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }


    public ResponseEntity<String> updateRoom(Long id, Room updatedRoom) {
        Optional<Room> existingRoom = roomRepository.findById(id);
        if (existingRoom.isPresent()) {
            Room room = existingRoom.get();
            room.setName(updatedRoom.getName());

            roomRepository.save(room);
            return ResponseEntity.ok("Dance venue updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Dance venue not found");
        }
    }

    public boolean deleteRoom(Long id) {
        Optional<Room> room = roomRepository.findById(id);
        room.ifPresent(roomRepository::delete);
        return room.isPresent();
    }
}
