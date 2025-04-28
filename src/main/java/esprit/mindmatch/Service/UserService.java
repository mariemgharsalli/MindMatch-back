package esprit.mindmatch.Service;

import esprit.mindmatch.DTO.UserProfileDTO;
import esprit.mindmatch.DTO.UserRegistrationDTO;
import esprit.mindmatch.Entities.*;
import esprit.mindmatch.Repository.RoleRepository;
import esprit.mindmatch.Repository.TokenRepository;
import esprit.mindmatch.Repository.UserRepository;
import esprit.mindmatch.file.FileStorageService;
import esprit.mindmatch.file.FileUtils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private final TokenRepository tokenRepository;

    @Value("${application.mailing.frontend.resetPassword-url}")
    private String resetPasswordUrl;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
                       EmailService emailService, FileStorageService fileStorageService, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.fileStorageService = fileStorageService;
        this.tokenRepository = tokenRepository;
    }

//    public ResponseEntity<String> processConfirmEmail(String email) {
//        User user = findByEmail(email);
//        Mail mail = new Mail();
//        mail.setFrom("no-reply@FindMe.com");
//        mail.setTo(user.getEmail());
//        mail.setSubject("Confirmation Création compte");
//
//        Map<String, Object> mailModel = new HashMap<>();
//        mailModel.put("user", user);
//        mailModel.put("signature", "http://FindMe.com");
//        mail.setModel(mailModel);
//
//        emailService.sendConfiramtionEmail(mail);
//        return ResponseEntity.ok("Email de confirmation envoyé avec succès.");
//    }

    @Transactional
    public User registerUser(UserRegistrationDTO dto) throws Exception {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new Exception("Email déjà utilisé.");
        }

        ERole roleEnum;
        try {
            roleEnum = ERole.valueOf(dto.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Rôle invalide. Choisissez parmi : " + Arrays.toString(ERole.values()));
        }

        Role role = roleRepository.findByRole(roleEnum)
                .orElseGet(() -> roleRepository.save(new Role(roleEnum)));

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .sexe(dto.getSexe())
                .role(role)
                .build();

        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Modifying
    public void updatePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé."));
    }

    @Transactional
    public UserProfileDTO uploadUserImages(MultipartFile profilePicture, List<MultipartFile> documents, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable avec ID : " + userId));

        if (user.getProfile() == null) user.setProfile(new UserProfile());
        user.getProfile().setUser(user);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String profilePath = fileStorageService.saveFile(profilePicture, user.getEmail(), "profile");
            user.getProfile().setProfilePicture(profilePath);
        }

        if (documents != null && !documents.isEmpty()) {
            List<String> docPaths = documents.stream()
                    .map(doc -> fileStorageService.saveFile(doc, user.getEmail(), "documents"))
                    .collect(Collectors.toList());
            user.getProfile().setDocuments(String.join(",", docPaths));
        }

        userRepository.save(user);
        return mapToProfileResponse(user);
    }

    public UserProfileDTO getProfile(String email) {
        User user = userRepository.findWithProfileByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé."));
        return mapToProfileResponse(user);
    }

    public String changePassword(ChangePasswordRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable avec ID : " + userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Mot de passe changé avec succès.";
    }

    public void resetPasswordRequest(PasswordResetRequest request) throws MessagingException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable avec cet email."));
        sendResetPasswordEmail(user);
    }

    private void sendResetPasswordEmail(User user) throws MessagingException {
        String tokenStr = UUID.randomUUID().toString();
        Token token = Token.builder()
                .token(tokenStr)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);

        String fullUrl = resetPasswordUrl + "?token=" + tokenStr;
        emailService.sendEmailResetPassword(
                user.getEmail(),
                EmailTemplateName.RESET_PASSWORD,
                fullUrl,
                "Réinitialisation du mot de passe"
        );
    }

    public void resetPassword(String token, PasswordResetRequest request) {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide."));

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            throw new RuntimeException("Le token a expiré.");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas.");
        }

        User user = tokenRepository.findUserByToken(token);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

    private UserProfileDTO mapToProfileResponse(User user) {
        UserProfile profile = user.getProfile();
        List<byte[]> documentsBytes = new ArrayList<>();

        if (profile.getDocuments() != null && !profile.getDocuments().isEmpty()) {
            documentsBytes = Arrays.stream(profile.getDocuments().split(","))
                    .map(FileUtils::readFileFromLocation)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return UserProfileDTO.builder()
                .firstname(user.getFirstName())
                .lastname(user.getLastName())
                .email(user.getEmail())
                .documents(documentsBytes)
                .profilePicture(FileUtils.readFileFromLocation(profile.getProfilePicture()))
                .build();
    }
}
