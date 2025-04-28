package esprit.mindmatch.Controller;

import esprit.mindmatch.Entities.Room;
import esprit.mindmatch.Repository.RoomRepository;
import esprit.mindmatch.Service.RoomService;
import esprit.mindmatch.Service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/Room")
public class RoomController {

    @Autowired
    RoomService roomService;

    @Autowired
    SessionService sessionService;

    @PostMapping("/assignRoomtoRoom/{idS}/{idR}")
    public void assignSessionToRoom(@PathVariable("idS") Long idSession, @PathVariable("idR") Long idRoom){
        sessionService.affectSessionToRoom(idSession, idRoom);
    }


    //DanceVenue Controller
    @PostMapping("/add")
    public Room add(@RequestBody Room room) {
        return roomService.addRoom(room);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<Room> getDanceVenueById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        return room != null ? ok(room) : ResponseEntity.notFound().build();
    }



    @GetMapping("/all")
    public List<Room> getAll() {
        return roomService.getAllRooms();
    }

    @PutMapping("/updateDanceVenue/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody Room room) {
        return roomService.updateRoom(id, room);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        boolean isDeleted = roomService.deleteRoom(id);
        return isDeleted ? ok("Room deleted successfully") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found");
    }

}
