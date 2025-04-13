package esprit.mindmatch.Controller;

import esprit.mindmatch.Auth.AuthenticationRequest;
import esprit.mindmatch.Auth.AuthenticationService;
import esprit.mindmatch.DTO.UserProfileDTO;
import esprit.mindmatch.DTO.UserRegistrationDTO;
import esprit.mindmatch.Entities.ChangePasswordRequest;
import esprit.mindmatch.Entities.PasswordResetRequest;
import esprit.mindmatch.Entities.User;
import esprit.mindmatch.Service.JwtService;
import esprit.mindmatch.Service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/v1/users")

public class usercontroller {

    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    private JwtService jwtService;

    private final UserService userService;

    public usercontroller(UserService userInfoService) {
        this.userService = userInfoService;
    }


    @PostMapping("/register")
    @CrossOrigin(origins = "https://localhost:4200")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDTO dto) {
        try {
            User registeredUser = userService.registerUser(dto);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.authenticate(request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @PatchMapping("/change-password/{userId}")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request , @PathVariable Long userId){
        return ResponseEntity.ok(userService.changePassword(request , userId));
    }


    @PostMapping("/password-reset-request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> resetPasswordRequest(@RequestBody PasswordResetRequest passwordResetRequest) throws MessagingException {
        userService.resetPasswordRequest(passwordResetRequest);
        return ResponseEntity.accepted().build();
    }


    @PatchMapping("/password-reset/{token}")
    public void resetPassword(@PathVariable String token ,@RequestBody PasswordResetRequest passwordResetRequest){
        userService.resetPassword(token, passwordResetRequest);
    }







//    @PostMapping("/logout")
//    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
//        final String authHeader = request.getHeader("Authorization");
//
//        // Vérifier si le token est fourni
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "No token provided.");
//            return ResponseEntity.badRequest().body(response);
//        }
//
//        String token = authHeader.substring(7); // Extraction du token
//
//        try {
//            // Extraire la date d'expiration du token
//            LocalDateTime expiry = jwtService.extractExpirationAsLocalDateTime(token);
//
//            // Ajouter le token à la liste noire
//            BlacklistedToken blacklistedToken = BlacklistedToken.builder()
//                    .token(token)
//                    .expiration(expiry)
//                    .build();
//            blacklistedTokenRepository.save(blacklistedToken);
//
//            // Retourner une réponse claire
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "Logged out successfully!");
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            // Gestion des erreurs
//            Map<String, String> response = new HashMap<>();
//            response.put("message", "Error during logout: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }




    /**

     *
     * @param profilePicture
     * @param documents
     * @param userId
     * @return
     */


    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<UserProfileDTO> uploadUserImages(
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestParam Long userId
    ) {
        UserProfileDTO userProfile = userService.uploadUserImages(profilePicture, documents, userId);
        return ResponseEntity.ok(userProfile);
    }



    @GetMapping("/me/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@RequestParam String email) {
        return ResponseEntity.ok(userService.getProfile(email));
    }

}