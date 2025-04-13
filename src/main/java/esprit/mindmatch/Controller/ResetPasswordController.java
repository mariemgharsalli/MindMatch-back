package esprit.mindmatch.Controller;

import esprit.mindmatch.Entities.PasswordReset;
import esprit.mindmatch.Entities.PasswordResetToken;
import esprit.mindmatch.Entities.User;
import esprit.mindmatch.Service.PasswordResetTokenService;
import esprit.mindmatch.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/reset-password")
@Tag(name = "Reset Password API", description = "API for handling password reset requests")
public class ResetPasswordController {

    private final PasswordResetTokenService tokenService;
    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;

    @Autowired
    public ResetPasswordController(PasswordResetTokenService tokenService, PasswordResetTokenService passwordResetTokenService,UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @PostMapping
    @Operation(summary = "Reset password using a token", description = "Updates the user's password if the token is valid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Token not found or expired")
    })
    public ResponseEntity<String> resetPassword(@RequestBody PasswordReset passwordReset, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid request");
        }
        PasswordResetToken token = tokenService.findByToken(passwordReset.getToken());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token not found");
        }
        if (token.getExpirationDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token expired");
        }
        User user = token.getUser();
        user.setPassword(passwordReset.getPassword());
        userService.updatePassword(user);
        passwordResetTokenService.deleteByUser(user);
        return ResponseEntity.ok("Password reset successfully");
    }
}
