package esprit.mindmatch.Service;

import esprit.mindmatch.DTO.UserProfileDTO;
import esprit.mindmatch.DTO.UserRegistrationDTO;
import esprit.mindmatch.Entities.*;
import esprit.mindmatch.Repository.RoleRepository;
import esprit.mindmatch.Repository.TokenRepository;
import esprit.mindmatch.Repository.UserRepository;
import esprit.mindmatch.Entities.EmailTemplateName;
import esprit.mindmatch.file.FileStorageService;
import esprit.mindmatch.file.FileUtils;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String uploadDir = "user-images/";

    private final EmailService emailService;
    private final FileStorageService fileStorageService;

    private final TokenRepository tokenRepository;


    @Value("${application.mailing.frontend.resetPassword-url}")
    private  String resetPasswordUrl;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmailService emailService, EmailService emailServices, FileStorageService fileStorageService, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.fileStorageService=fileStorageService;
        this.tokenRepository = tokenRepository;
    }
    // Send Email de confiramrion
    public ResponseEntity<String> processConfirmEmail(String email) {
        User user = findByEmail(email);
        Mail mail = new Mail();
        mail.setFrom("no-reply@FindMe.com");
        mail.setTo(user.getEmail());
        mail.setSubject("Confirmation Création compte");
        Map<String, Object> mailModel = new HashMap<>();
        mailModel.put("user", user);
        mailModel.put("signature", "http://FindMe.com");
        mail.setModel(mailModel);
        emailService.sendConfiramtionEmail(mail);
        return ResponseEntity.ok("Confiramtion Email is sent with sucess..." );
    }


    @Transactional
    public User registerUser(UserRegistrationDTO dto) throws Exception {
        // Validate email existence
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new Exception("Email is already in use");
        }

        // Validate and convert role
        ERole roleEnum;
        try {
            roleEnum = ERole.valueOf(dto.getRole().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid role. Please choose from: " +
                    Arrays.toString(ERole.values()));
        }

        // Get or create role
        Role role = roleRepository.findByRole(roleEnum)
                .orElseGet(() -> {
                    Role newRole = new Role(roleEnum);
                    return roleRepository.save(newRole);
                });

        // Build and save user
        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .dateOfBirth(dto.getDateOfBirth())
                .sexe(dto.getSexe())
                .role(role)  // This sets the role relationship
                .build();

        return userRepository.save(user);
    }

    //Aziz Add this to email part
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Modifying
    public void updatePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }


    private void saveFile(String uploadPath, String fileName, org.springframework.web.multipart.MultipartFile file) throws IOException {
        Path uploadPathPath = Paths.get(uploadPath);
        if (!uploadPathPath.toFile().exists()) {
            uploadPathPath.toFile().mkdirs();
        }
        Path filePath = uploadPathPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    public User getUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }




    @Transactional
    public UserProfileDTO uploadUserImages(MultipartFile profilePicture, List<MultipartFile> documents, Long userId) {
        // Récupérer l'utilisateur par son ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found by id" + userId));

        // Vérifier si le profil existe, sinon en créer un nouveau
        if (user.getProfile() == null) {
            user.setProfile(new UserProfile());
        }

        user.getProfile().setUser(user);  // Assurez-vous que la relation bidirectionnelle est bien établie


        String uploadedProfilePicture = null;
        List<String> uploadedDocuments = new ArrayList<>(); // Liste pour stocker les chemins des documents téléchargés


        // Traitement du fichier image de profil
        if (profilePicture != null && !profilePicture.isEmpty()) {
            uploadedProfilePicture = fileStorageService.saveFile(profilePicture, user.getEmail(), "profile");
            user.getProfile().setProfilePicture(uploadedProfilePicture);
        }

        // Traitement des fichiers de documents
        if (documents != null && !documents.isEmpty()) {
            for (MultipartFile document : documents) {
                String uploadedDocument = fileStorageService.saveFile(document, user.getEmail(), "documents");
                uploadedDocuments.add(uploadedDocument);  // Ajouter chaque document à la liste
            }
            user.getProfile().setDocuments(String.join(",", uploadedDocuments));  // Joindre les chemins des documents en une seule chaîne
        }

        // Sauvegarder l'utilisateur avec son profil
        userRepository.save(user);

        // Retourner l'objet DTO de la réponse
        return mapToProfileResponse(user);
    }

    private UserProfileDTO mapToProfileResponse(User user) {
        UserProfile profile = user.getProfile();
        // Vérifier si la liste des documents n'est pas vide ou nulle
        List<byte[]> documentsBytes = new ArrayList<>();
        if (profile.getDocuments() != null && !profile.getDocuments().isEmpty()) {
            List<String> documentPaths = Arrays.asList(profile.getDocuments().split(",")); // Séparer les fichiers stockés sous forme de chaîne
            documentsBytes = documentPaths.stream()
                    .map(FileUtils::readFileFromLocation) // Lire chaque fichier en binaire
                    .filter(Objects::nonNull) // Filtrer les valeurs nulles (fichiers introuvables)
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

    public UserProfileDTO getProfile(String email) {
        User user = userRepository.findWithProfileByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return mapToProfileResponse(user);
    }

    public String changePassword(ChangePasswordRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("User not found by id" + userId));
        // check if the current password is correct
        if(!passwordEncoder.matches(request.getOldPassword() , user.getPassword())){
            throw new IllegalArgumentException("Wrong password ")   ;
        }
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new IllegalArgumentException("Password are not the same ");
        }
        // update the new  password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user) ;

        return  "Mot de passe changé avec succès." ;
    }


    public void resetPasswordRequest(PasswordResetRequest passwordResetRequest) throws MessagingException {
        User user = userRepository.findByEmail(passwordResetRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found by email" + passwordResetRequest.getEmail()));
        sendResetPasswordEmail(user);
    }

    private void sendResetPasswordEmail(User user) throws MessagingException {
        String passwordResetUrl = "" ;
        // generer le token de reset password
        String passwordResetToken = UUID.randomUUID().toString();

        var token = Token.builder()
                .token(passwordResetToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);
        // construire l'url qui contient le path de angular + le parametre token
        passwordResetUrl = this.resetPasswordUrl + "?token=" + passwordResetToken;;

        emailService.sendEmailResetPassword(
                user.getEmail() ,
                EmailTemplateName.RESET_PASSWORD ,
                passwordResetUrl ,
                "Vérification de la demande de réinitialisation de mot de passe"
        );
    }


    public void resetPassword(String token, PasswordResetRequest passwordResetRequest) {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(()-> new RuntimeException("token invalid"));
        // find user by token
        User user = this.tokenRepository.findUserByToken(token);
        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())){
            throw new RuntimeException("Activation token has expired. A new token has been send to the same email address") ;
        }
        if(!passwordResetRequest.getNewPassword().equals(passwordResetRequest.getConfirmPassword()) ){
            throw new IllegalArgumentException("Le mot de passe et sa confirmation ne correspondent pas. Veuillez réessayer.");
        }
        user.setPassword(this.passwordEncoder.encode(passwordResetRequest.getNewPassword()));
        userRepository.save(user);
        // mise ajour de token ;
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
    }

}