package esprit.mindmatch.Auth;

import esprit.mindmatch.Entities.User;
import esprit.mindmatch.Repository.UserRepository;
import esprit.mindmatch.Service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var jwtToken= jwtService.generateToken(user, user.getEmail() , user.getUserId());
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public User getUser(Authentication authentication)
    {
        return userRepository.getUserByEmail(authentication.getName());
    }

}
