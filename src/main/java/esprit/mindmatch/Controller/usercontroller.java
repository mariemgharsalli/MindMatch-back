package esprit.mindmatch.Controller;

import esprit.mindmatch.Auth.AuthenticationRequest;
import esprit.mindmatch.Auth.AuthenticationService;
import esprit.mindmatch.DTO.UserProfileDTO;
import esprit.mindmatch.DTO.UserRegistrationDTO;
import esprit.mindmatch.Entities.ChangePasswordRequest;
import esprit.mindmatch.Entities.PasswordResetRequest;
import esprit.mindmatch.Entities.Submission;
import esprit.mindmatch.Entities.User;
import esprit.mindmatch.Repository.SubmissionRepository;
import esprit.mindmatch.Repository.UserRepository;
import esprit.mindmatch.Service.JwtService;
import esprit.mindmatch.Service.UserService;
import esprit.mindmatch.file.FileStorageService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*") // pour éviter les erreurs CORS

public class usercontroller {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    FileStorageService fileStorageService;

    public usercontroller(UserService userService) {
        this.userService = userService;
    }

    // Inscription d'un utilisateur
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO dto) {
        try {
            User registeredUser = userService.registerUser(dto);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Authentification
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.authenticate(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur d'authentification.");
        }
    }

    // Changement de mot de passe
    @PatchMapping("/change-password/{userId}")
    public ResponseEntity<String> changePassword(
            @RequestBody ChangePasswordRequest request,
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(userService.changePassword(request, userId));
    }

    // Demande de réinitialisation de mot de passe
   /* @PostMapping("/password-reset-request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> resetPasswordRequest(@RequestBody PasswordResetRequest passwordResetRequest)
            throws MessagingException {
        userService.resetPasswordRequest(passwordResetRequest);
        return ResponseEntity.accepted().body("Email de réinitialisation envoyé.");
    }
*/
    // Réinitialisation avec token
    @PatchMapping("/password-reset/{token}")
    public ResponseEntity<?> resetPassword(
            @PathVariable String token,
            @RequestBody PasswordResetRequest passwordResetRequest
    ) {
        userService.resetPassword(token, passwordResetRequest);
        return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
    }

    // Upload d'image de profil et de documents
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<UserProfileDTO> uploadUserImages(
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestParam Long userId
    ) {
        UserProfileDTO userProfile = userService.uploadUserImages(profilePicture, documents, userId);
        return ResponseEntity.ok(userProfile);
    }

    // Récupération du profil utilisateur
    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@RequestParam String email) {
        return ResponseEntity.ok(userService.getProfile(email));
    }

    // Ajoutez ces nouvelles endpoints
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/exists/{userId}")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable Long userId) {
        boolean exists = userRepository.existsById(userId);
        return ResponseEntity.ok(exists);
    }

    // Afficher la liste des utilisateurs (Admin uniquement)
    @GetMapping()
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // Supprimer un utilisateur (Admin uniquement)
    @DeleteMapping("/{userId}")
    @Transactional
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        try {
            // Charger l'utilisateur avec toutes ses relations
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Supprimer d'abord les documents des soumissions
            List<Submission> submissions = submissionRepository.findByUser(user);
            for (Submission submission : submissions) {
                // Supprimer les fichiers physiques si nécessaire
                if (submission.getDocumentPaths() != null) {
                    for (String documentPath : submission.getDocumentPaths()) {
                        fileStorageService.deleteFile(documentPath);
                    }
                }
                // Vider la liste des documents (cela devrait supprimer les entrées dans submission_document_paths)
                submission.getDocumentPaths().clear();
                submissionRepository.save(submission);
            }

            // Supprimer les soumissions
            submissionRepository.deleteAll(submissions);

            // Supprimer le profil utilisateur s'il existe
            if (user.getProfile() != null) {
                // Suppression du profil (géré par cascade.ALL normalement)
            }

            // Enfin supprimer l'utilisateur
            userRepository.delete(user);

            return ResponseEntity.ok("Utilisateur supprimé avec succès.");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }
}
