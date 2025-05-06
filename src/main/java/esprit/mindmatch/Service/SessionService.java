package esprit.mindmatch.Service;

import esprit.mindmatch.DTO.SessionDTO;
import esprit.mindmatch.DTO.UserProfileDTO;
import esprit.mindmatch.Entities.*;
import esprit.mindmatch.Repository.RoomRepository;
import esprit.mindmatch.Repository.SessionRepository;
import esprit.mindmatch.Repository.SubmissionRepository;
import esprit.mindmatch.file.FileStorageService;
import esprit.mindmatch.file.FileUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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
    @Autowired
    private EmailService emailService;
    @Autowired
    private SubmissionRepository submissionRepository;


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


    public Session updateSession(Long id, Session updatedSession) {
        Session existingSession = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Mettre à jour tous les champs nécessaires
        existingSession.setNom(updatedSession.getNom());
        existingSession.setDescription(updatedSession.getDescription());
        existingSession.setDate(updatedSession.getDate());
        existingSession.setExpirationDate(updatedSession.getExpirationDate());
        existingSession.setSalle(updatedSession.getSalle());
        existingSession.setPrix(updatedSession.getPrix());
        existingSession.setNiveau(updatedSession.getNiveau());
        existingSession.setLocation(updatedSession.getLocation());
        existingSession.setSpeakerName(updatedSession.getSpeakerName());
        existingSession.setSpeakerEmail(updatedSession.getSpeakerEmail());

        return sessionRepository.save(existingSession);
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
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.isArchived()) {
            throw new RuntimeException("Session is already archived");
        }

        session.setArchived(true);
        session.setArchiveDate(new Date());
        session.setScheduledForDeletion(false); // Reset in case it was set before
        sessionRepository.save(session);
    }

    public void markSessionForDeletion(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.isArchived()) {
            throw new RuntimeException("Only archived sessions can be marked for deletion");
        }

        session.setScheduledForDeletion(true);
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
    @Scheduled(fixedRate = 60000) // Run every minute for more precise timing
    public void cleanupArchivedSessions() {
        // Get sessions that are either marked for deletion or have been archived for more than 1 hour
        Date oneHourAgo = new Date(System.currentTimeMillis() - 3600000);
        List<Session> sessionsToDelete = sessionRepository.findByArchivedAndArchiveDateBefore(true, oneHourAgo);

        // Also include sessions manually marked for deletion
        List<Session> manuallyMarked = sessionRepository.findByScheduledForDeletion(true);
        sessionsToDelete.addAll(manuallyMarked);

        // Remove duplicates
        sessionsToDelete = sessionsToDelete.stream().distinct().collect(Collectors.toList());

        for (Session session : sessionsToDelete) {
            sessionRepository.delete(session);
        }
    }
    /*@Scheduled(fixedDelay = 60000) // Exécuté toutes les 60 secondes après la fin du précédent
    public void cleanupArchivedSessions() {
        List<Session> archivedSessions = sessionRepository.findByArchived(true);
        Date now = new Date();

        for (Session session : archivedSessions) {
            if (session.getArchiveDate() != null) {
                long diffInMillis = now.getTime() - session.getArchiveDate().getTime();
                if (diffInMillis >= 60000) { // 60000 ms = 1 minute exactement
                    sessionRepository.delete(session);
                    System.out.println("Deleted session id=" + session.getId());
                }
            }
        }
    }*/


    /*@Scheduled(cron = "0 0 0 * * *")*/
    @Scheduled(cron = "0 * * * * *")

    public void sendSessionReminders() {
        Date now = new Date();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(now);
        tomorrow.add(Calendar.DATE, 1); // Demain

        List<Session> sessions = sessionRepository.findAll();

        for (Session session : sessions) {
            if (session.getDate() != null && isSameDay(session.getDate(), tomorrow.getTime())) {
                // Récupère toutes les Submissions ACCEPTÉES pour cette session
                List<Submission> acceptedSubmissions = submissionRepository.findBySessionAndStatus(session, submissionStatus.ACCEPTED);

                for (Submission submission : acceptedSubmissions) {
                    User user = submission.getUser();
                    if (user != null) {
                        try {
                            // Create the Mail object
                            Mail mail = new Mail();
                            mail.setTo(user.getEmail());
                            mail.setFrom("gharsallim060@gmail.com");
                            mail.setSubject("📢 Rappel : Votre session " + session.getNom() + " est demain !");

                            // Adding dynamic content to the Mail model
                            Map<String, Object> model = new HashMap<>();
                            model.put("name", user.getFirstName());
                            model.put("sessionName", session.getNom());
                            model.put("sessionDate", session.getDate());
                            model.put("location", session.getLocation());
                            mail.setModel(model);

                            // Use the EmailService to send the reminder
                            emailService.send(mail);

                        } catch (Exception e) {
                            // Log the exception or handle it appropriately
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}