package esprit.mindmatch.Controller;

import esprit.mindmatch.DTO.SessionDTO;
import esprit.mindmatch.DTO.UserProfileDTO;
import esprit.mindmatch.Entities.Session;
import esprit.mindmatch.Service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/Session")
public class SessionController {

    @Autowired
    SessionService sessionService;

    @GetMapping("/non-archivees")
    public ResponseEntity<List<Session>> getAllSessionNonArchivees() {
        return ResponseEntity.ok(sessionService.getAllSessionNonArchivees());
    }

    @PostMapping("/archive/{id}")
    public ResponseEntity<String> archiveSession(@PathVariable Long id) {
        try {
            sessionService.archiveSession(id);
            return ResponseEntity.ok("Session archivée avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Méthode pour désarchiver une formation
    @PutMapping("/unarchive/{id}")
    public ResponseEntity<String> unarchiveSession(@PathVariable("id") Long id) {
        try {
            sessionService.unarchiveSession(id);
            return ResponseEntity.ok("Session désarchivée avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/add")
    public Session addSession(@RequestBody Session session) {
        return sessionService.addSession(session);
    }

    @GetMapping("/getSessionById/{id}")
    public ResponseEntity<Session> getById(@PathVariable Long id) {
        Session session = sessionService.getSessionById(id);
        return session != null ? ok(session) : ResponseEntity.notFound().build();
    }

    @GetMapping("/allSessions")
    public List<SessionDTO> getAllSessions() {
        return sessionService.getAllSessions();
    }

    @DeleteMapping("/deleteSession/{id}")
    public ResponseEntity<String> deleteSession(@PathVariable Long id) {
        boolean isDeleted = sessionService.deleteSession(id);
        return isDeleted ? ok("Session deleted successfully") : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Competition not found");
    }

    @PutMapping("/updateCompetition/{id}")
    public ResponseEntity<String> updateSession(@PathVariable Long id, @RequestBody Session updatedSession) {
        return sessionService.updateSession(id, updatedSession);
    }

    @PostMapping(value = "/images/upload/{sessionId}", consumes = "multipart/form-data")
    public ResponseEntity<Session> uploadUserImages(
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @PathVariable Long sessionId
    ) {
        Session imageSession = sessionService.upload(profilePicture, sessionId);
        return ResponseEntity.ok(imageSession);
    }
}
