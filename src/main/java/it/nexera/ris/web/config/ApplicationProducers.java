package it.nexera.ris.web.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

// Producer class for "CDI" injecting password encoder
@ApplicationScoped
public class ApplicationProducers {

    @Produces
    @ApplicationScoped
    public PasswordEncoder createPasswordEncoder() {
            String idForEncode = "bcrypt";

            Map<String, PasswordEncoder> encoders = new HashMap<>();
            encoders.put(idForEncode, new BCryptPasswordEncoder());

            DelegatingPasswordEncoder delegatingPasswordEncoder =
                    new DelegatingPasswordEncoder(idForEncode, encoders);
            delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(
                    new MessageDigestPasswordEncoder("MD5")
            );
            return delegatingPasswordEncoder;
    }

}