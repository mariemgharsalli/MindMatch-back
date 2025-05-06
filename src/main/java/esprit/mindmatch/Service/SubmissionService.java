package esprit.mindmatch.Service;

import esprit.mindmatch.Entities.*;
import esprit.mindmatch.Repository.SessionRepository;
import esprit.mindmatch.Repository.SubmissionRepository;
import esprit.mindmatch.Repository.UserRepository;
import esprit.mindmatch.file.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;



    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public List<Submission> getSubmissionsBySession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        return submissionRepository.findBySession(session);
    }

    public List<Submission> getSubmissionsByUser(String userEmail) {
        return submissionRepository.findByUser_Email(userEmail);
    }
    public void deleteById(Long id) {
        if (!submissionRepository.existsById(id)) {
            throw new EntityNotFoundException("Submission not found");
        }
        submissionRepository.deleteById(id);
    }

    @Transactional
    public Submission updateSubmissionStatus(Long submissionId, submissionStatus status) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        submission.setStatus(status);
        Submission updatedSubmission = submissionRepository.save(submission);

        User user = submission.getUser();
        String fullName = user.getFirstName() + " " + user.getLastName();
        String to = user.getEmail();

        Mail mail = new Mail();
        mail.setTo(to);
        mail.setFrom("gharsallim060@gmail.com");

        // Choisir le sujet et le message selon le statut
        if (status == submissionStatus.ACCEPTED) {
            mail.setSubject("Your submission has been accepted");
            mail.setModel(Map.of(
                    "name", fullName,
                    "message", "Congratulations! Your paper has been accepted. You may proceed with the payment."
            ));
        } else if (status == submissionStatus.REJECTED) {
            mail.setSubject("Your submission has been rejected");
            mail.setModel(Map.of(
                    "name", fullName,
                    "message", "We regret to inform you that your paper was not accepted."
            ));
        }


        emailService.sendDecisionEmail(mail);  // nouvelle méthode à créer dans EmailService

        return updatedSubmission;
    }


    @Transactional
    public Submission createSubmissionWithDocument(Long userId, Long sessionId, MultipartFile document) {
        // Debug logging
        log.info("Creating submission for user: {} and session: {}", userId, sessionId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        // Validate session exists and is not archived
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    log.error("Session not found with id: {}", sessionId);
                    return new EntityNotFoundException("Session not found");
                });

        if (session.isArchived()) {
            log.warn("Attempt to submit to archived session: {}", sessionId);
            throw new IllegalStateException("Cannot submit to archived session");
        }

        // Store document with session-specific path
        String documentPath = fileStorageService.saveFile(
                document,
                "session_" + sessionId,
                "user_" + userId + "" + System.currentTimeMillis() + "" + document.getOriginalFilename()
        );

        // Debug the session being associated
        log.info("Associating submission with session: {} ({})", session.getId(), session.getNom());

        // Create and save submission
        Submission submission = new Submission();
        submission.setUser(user);
        submission.setSession(session); // Explicitly set the session
        submission.setSubmissionDate(new Date());
        submission.setDocumentPaths(List.of(documentPath));
        submission.setStatus(submissionStatus.PENDING);

        Submission savedSubmission = submissionRepository.save(submission);

        // Verify the saved association
        log.info("Created submission {} for session {}", savedSubmission.getId(), savedSubmission.getSession().getId());

        return savedSubmission;
    }

// SubmissionService.java

    public Map<String, Object> getSessionStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 1. Statistiques par jour
        List<Object[]> dailyStats = submissionRepository.countDailySubmissions();
        List<Map<String, Object>> dailyData = dailyStats.stream()
                .map(row -> {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("date", row[0]);
                    entry.put("totalSubmissions", row[1]);
                    return entry;
                })
                .sorted((a, b) -> ((Date) a.get("date")).compareTo((Date) b.get("date")))
                .collect(Collectors.toList());
        result.put("dailyData", dailyData);

        // 2. Top sessions
        List<Object[]> topSessions = submissionRepository.countSubmissionsBySession();
        List<Map<String, Object>> topSessionsData = topSessions.stream()
                .map(row -> {
                    Session session = (Session) row[0];
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("sessionId", session.getId());
                    entry.put("sessionName", session.getNom());
                    entry.put("totalSubmissions", row[1]);
                    entry.put("speaker", session.getSpeakerName());
                    return entry;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("totalSubmissions"), (Long) a.get("totalSubmissions")))
                .limit(10) // Top 10 seulement
                .collect(Collectors.toList());
        result.put("topSessions", topSessionsData);

        return result;
    }//    @Transactional
//    public Submission createSubmissionWithDocument(Long userId, Long sessionId, MultipartFile document) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        Session session = sessionRepository.findById(sessionId)
//                .orElseThrow(() -> new RuntimeException("Session not found"));
//
//        List<String> documentPaths = new ArrayList<>();
//
//        if (document != null && !document.isEmpty()) {
//            String path = fileStorageService.saveFile(document, String.valueOf(userId), "submission");
//            if (path != null) {
//                documentPaths.add(path);
//            } else {
//                throw new RuntimeException("Document upload failed");
//            }
//        }
//
//        Submission submission = Submission.builder()
//                .user(user)
//                .session(session)
//                .submissionDate(new Date())
//                .documentPaths(documentPaths)
//                .status(submissionStatus.PENDING)
//                .build();
//
//        return submissionRepository.save(submission);
//    }


}
