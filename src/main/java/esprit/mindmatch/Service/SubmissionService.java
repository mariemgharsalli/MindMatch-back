package esprit.mindmatch.Service;

import esprit.mindmatch.Entities.*;
import esprit.mindmatch.Repository.SessionRepository;
import esprit.mindmatch.Repository.SubmissionRepository;
import esprit.mindmatch.Repository.UserRepository;
import esprit.mindmatch.file.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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

    @Transactional
    public Submission updateSubmissionStatus(Long submissionId, submissionStatus status) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        submission.setStatus(status);
        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission createSubmissionWithDocument(Long userId, Long sessionId, MultipartFile document) {
        // Vérification plus détaillée de l'utilisateur
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        // Vérification plus détaillée de la session
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session with ID " + sessionId + " not found. Available sessions: " +
                        sessionRepository.findAll().stream().map(s -> s.getId().toString()).collect(Collectors.joining(", "))));

        // Le reste du code reste inchangé
        List<String> documentPaths = new ArrayList<>();

        if (document != null && !document.isEmpty()) {
            String path = fileStorageService.saveFile(document, String.valueOf(userId), "submission");
            if (path != null) {
                documentPaths.add(path);
            } else {
                throw new RuntimeException("Document upload failed");
            }
        }

        Submission submission = Submission.builder()
                .user(user)
                .session(session)
                .submissionDate(new Date())
                .documentPaths(documentPaths)
                .status(submissionStatus.PENDING)
                .build();

        return submissionRepository.save(submission);
    }
//    @Transactional
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
