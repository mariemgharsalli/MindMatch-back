package esprit.mindmatch.Service;

import esprit.mindmatch.DTO.SessionDTO;
import esprit.mindmatch.DTO.UserProfileDTO;
import esprit.mindmatch.Entities.Room;
import esprit.mindmatch.Entities.Session;
import esprit.mindmatch.Entities.User;
import esprit.mindmatch.Entities.UserProfile;
import esprit.mindmatch.Repository.RoomRepository;
import esprit.mindmatch.Repository.SessionRepository;
import esprit.mindmatch.file.FileStorageService;
import esprit.mindmatch.file.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SessionService {

    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    RoomRepository roomRepository;

    //Competition functions
    public Session addSession(Session session) {

        return sessionRepository.save(session);
    }


    public Session getSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }


    public List<SessionDTO> getAllSessions() {
        return sessionRepository.findAll().stream()
                .map(this::mapToSessionDTO)
                .collect(Collectors.toList());
    }

    private SessionDTO mapToSessionDTO(Session session) {
        return SessionDTO.builder()
                .id(session.getId())
                .nom(session.getNom())
                .description(session.getDescription())
                .date(session.getDate())
                .expirationDate(session.getExpirationDate())
                .salle(session.getSalle())
                .prix(session.getPrix())
                .niveau(session.getNiveau())
                .location(session.getLocation())
                .archived(session.getArchived())
                .speakerEmail(session.getSpeakerEmail())
                .speakerName(session.getSpeakerName()

                )
                .profilePicture(FileUtils.readFileFromLocation(session.getProfilePicture()))
                .build();
    }

    public boolean deleteSession(Long id) {
        Optional<Session> sessionOptional = sessionRepository.findById(id);

        if (sessionOptional.isPresent()) {
            sessionRepository.delete(sessionOptional.get());
            return true; // Deletion successful
        } else {
            return false; // Competition not found
        }
    }


    public ResponseEntity<String> updateSession(Long id, Session updatedSession) {
        Optional<Session> existingSession = sessionRepository.findById(id);
        if (existingSession.isPresent()) {
            Session session = existingSession.get();
            session.setNom(updatedSession.getNom());
            session.setDate(updatedSession.getDate());
            session.setDescription(updatedSession.getDescription());
            session.setExpirationDate(updatedSession.getExpirationDate());
            session.setRoom(updatedSession.getRoom());
            session.setLocation(updatedSession.getLocation());
            session.setArchived(updatedSession.getArchived());
            session.setSalle(updatedSession.getSalle());
            session.setNiveau(updatedSession.getNiveau());
            session.setSpeakerName(updatedSession.getSpeakerName());
            session.setSpeakerEmail(updatedSession.getSpeakerEmail());
            sessionRepository.save(session);
            return ResponseEntity.ok("competition updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("competition not found");
        }
    }

    public void affectSessionToRoom(Long idSession, Long idRoom) {
        Session session=sessionRepository.findById(idSession).get();
        Room room=roomRepository.findById(idRoom).get();
        session.setRoom(room);
        sessionRepository.save(session);
        roomRepository.save(room);
    }

    public Session findByRoom( Long roomId){
        return sessionRepository.getSessionByRoom_RoomId(roomId);
    }


    public Session upload(MultipartFile profilePicture, Long sessionId) {
      Session   session = this.sessionRepository.findById(sessionId)
              .orElseThrow();
        String uploadedProfilePicture = null;

        // Traitement du fichier image de profil
        if (profilePicture != null && !profilePicture.isEmpty()) {
            uploadedProfilePicture = fileStorageService.saveFile(profilePicture, session.getNom(), "profile");
            session.setProfilePicture(uploadedProfilePicture);
            sessionRepository.save(session);
        }
      return session ;
    }

    public void archiveSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        if (session.isArchived()) {
            throw new RuntimeException("La Session est déjà archivée");
        }

        session.setArchived(true);
        session.setArchiveDate(new Date()); // Enregistrer la date d'archivage
        sessionRepository.save(session);
    }

    public List<Session> getAllSessionNonArchivees() {
        return (List<Session>) sessionRepository.findByArchived(false); // Récupérer uniquement les formations non archivées
    }

    public void unarchiveSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        if (!session.isArchived()) {
            throw new RuntimeException("La Session n'est pas archivée");
        }

        session.setArchived(false);
        session.setArchiveDate(null); // Effacer la date d'archivage
        sessionRepository.save(session);
    }

    @Scheduled(fixedRate = 3600000) // Exécuté toutes les heures (3600000 ms)
    public void cleanupArchivedSessions() {
        List<Session> archivedSessions = sessionRepository.findByArchived(true);
        Date now = new Date();

        for (Session session : archivedSessions) {
            if (session.getArchiveDate() != null) {
                long diffInHours = TimeUnit.MILLISECONDS.toHours(now.getTime() - session.getArchiveDate().getTime());
                if (diffInHours >= 1) {
                    sessionRepository.delete(session);
                }
            }
        }
    }
}
