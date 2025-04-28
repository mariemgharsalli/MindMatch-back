package esprit.mindmatch.Controller;

import esprit.mindmatch.Entities.Mail;
import esprit.mindmatch.Entities.PasswordForgot;
import esprit.mindmatch.Entities.PasswordResetToken;
import esprit.mindmatch.Entities.User;
import esprit.mindmatch.Service.EmailService;
import esprit.mindmatch.Service.PasswordResetTokenService;
import esprit.mindmatch.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Controller
@RequestMapping("/forgot-password")
@Tag(name = "Forgot Password API", description = "API for handling password reset requests")
public class ForgotPasswordController {
    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;

    @Autowired
    public ForgotPasswordController(UserService userService, PasswordResetTokenService passwordResetTokenService, EmailService emailService) {
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
    }

    @PostMapping
    @Operation(summary = "Process password forgot request", description = "Sends a password reset link to the user's email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset link sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> processPasswordForgot(@RequestBody PasswordForgot passwordForgot, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }
        User user = userService.findByEmail(passwordForgot.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found!");
        }
        // Delete existing token for user
        PasswordResetToken existingToken = passwordResetTokenService.findByUser(user);
        if (existingToken != null) {

            passwordResetTokenService.deleteByUser(user);
        }


        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpirationDate(LocalDateTime.now().plusMinutes(10));
        token = passwordResetTokenService.save(token);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save token");
        }

        Mail mail = new Mail();
        mail.setFrom("no-reply@FindMe.com");
        mail.setTo(user.getEmail());
        mail.setSubject("Password reset request");

        Map<String, Object> mailModel = new HashMap<>();
        mailModel.put("token", token);
        mailModel.put("user", user);
        mailModel.put("signature", "http://localhost:4200");
        String url = "http://localhost:4200/reinitialiser-mot-passe?token=" + token.getToken();
        mailModel.put("resetUrl", url);
        mail.setModel(mailModel);
        /* send email using emailService
        if email sent successfully return success message with URL
         */
        emailService.send(mail);
        return ResponseEntity.ok("Password reset link sent to your email. Please click this link to reset your password: " + url);
    }
}