package esprit.mindmatch.Service;

import esprit.mindmatch.Entities.PasswordResetToken;
import esprit.mindmatch.Entities.User;
import esprit.mindmatch.Repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenService {


    @Autowired
    PasswordResetTokenRepository PasswordResetTokenRepository;

    @Autowired
    public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenDAO) {
        this.PasswordResetTokenRepository = passwordResetTokenDAO;
    }


    public PasswordResetToken findByToken(String token) {
        return PasswordResetTokenRepository.findByToken(token).orElse(null);
    }


    public PasswordResetToken save(PasswordResetToken passwordResetToken) {
        return PasswordResetTokenRepository.save(passwordResetToken);
    }

    public PasswordResetToken findByUser(User user) {
        return PasswordResetTokenRepository.findByUser(user).orElse(null);
    }

    @Transactional
    public void deleteByUser(User user) {
        PasswordResetTokenRepository.deleteByUser(user);
    }
}