package it.nexera.ris.web.handlers;

    import it.nexera.ris.common.helpers.DateTimeHelper;
    import it.nexera.ris.common.helpers.LogHelper;
    import it.nexera.ris.persistence.beans.dao.ConnectionManager;
    import it.nexera.ris.persistence.beans.entities.domain.AppToken;
    import it.nexera.ris.persistence.beans.entities.domain.User;
    import org.apache.logging.log4j.Logger;
    import org.apache.logging.log4j.LogManager;
    import org.hibernate.HibernateException;
    import org.hibernate.Session;
    import org.hibernate.criterion.Criterion;
    import org.hibernate.criterion.Restrictions;
    import org.jose4j.jwa.AlgorithmConstraints;
    import org.jose4j.jwk.RsaJsonWebKey;
    import org.jose4j.jwk.RsaJwkGenerator;
    import org.jose4j.jws.AlgorithmIdentifiers;
    import org.jose4j.jws.JsonWebSignature;
    import org.jose4j.jwt.JwtClaims;
    import org.jose4j.jwt.consumer.InvalidJwtException;
    import org.jose4j.jwt.consumer.JwtConsumer;
    import org.jose4j.jwt.consumer.JwtConsumerBuilder;
    import org.jose4j.lang.JoseException;

    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;

    public class TokenManager {
        private static final String ISSUER = "RIS.APP";
        private static final String BEARER_PREFIX = "bearer ";
        public transient final Logger log = LogManager.getLogger(getClass());
        private static RsaJsonWebKey rsaJsonWebKey;
        private static TokenManager tokenManager = null;
        private static Map<String, Long> tokenExpirationMap = new HashMap<>();
        private static Long EXPIRATION_SECS = 30 * 60L * 1000;

        public static TokenManager getInstance() {
            if (null == tokenManager) {
                tokenManager = new TokenManager();
                try {
                    rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
                    rsaJsonWebKey.setKeyId("k1");
                } catch (JoseException e) {
                    e.printStackTrace();
                }
            }
            return tokenManager;
        }

        public String generateToken(User user) throws JoseException, HibernateException, IllegalAccessException {
            JwtClaims claims = new JwtClaims();
            claims.setIssuer(ISSUER);
            claims.setExpirationTimeMinutesInTheFuture(24 * 60);
            claims.setGeneratedJwtId();
            claims.setIssuedAtToNow();
            claims.setNotBeforeMinutesInThePast(2);
            claims.setSubject(user.getLogin());
            claims.setClaim("email", user.getEmail());
            claims.setClaim("userid", user.getId());
            JsonWebSignature jws = new JsonWebSignature();
            jws.setPayload(claims.toJson());
            jws.setKey(rsaJsonWebKey.getPrivateKey());
            jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
            String jwt = jws.getCompactSerialization();
            tokenExpirationMap.put(jwt, System.currentTimeMillis());
            return jwt;
        }

        public JwtClaims validateAuthBearer(String authentication) throws JoseException {
            if (authentication != null) {
                authentication = authentication.trim();
                return validateToken(authentication);
            }
            return null;
        }


        public JwtClaims validateToken(String jwt) {
            if (!tokenExpirationMap.containsKey(jwt)) {
                LogHelper.log(log, "JWT not present!(validateToken)");
                return null;
            }
            if ((tokenExpirationMap.get(jwt) + getExpirationSecs()) < System.currentTimeMillis()) {
                LogHelper.log(log, "JWT is expired!(validateToken)");
                tokenExpirationMap.remove(jwt);
                return null;
            }

            JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setAllowedClockSkewInSeconds(30)
                    .setRequireSubject()
                    .setExpectedIssuer(ISSUER)
                    .setVerificationKey(rsaJsonWebKey.getKey())
                    .setJwsAlgorithmConstraints(
                            new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                    AlgorithmIdentifiers.RSA_USING_SHA256))
                    .build();

            try {
                JwtClaims claims = jwtConsumer.processToClaims(jwt);
                if(claims != null){
                    tokenExpirationMap.put(jwt, System.currentTimeMillis());
                    LogHelper.debugInfo(log, "Token expiration has been refreshed");
                }
                return claims;
            } catch (InvalidJwtException e) {
                System.out.println("Invalid JWT! " + e);
                LogHelper.log(log, e);
                return null;
            }
        }

        public User validateAuthBearer(String authentication, Session session)
                throws InstantiationException, IllegalAccessException {
            if (authentication != null) {
                authentication = authentication.trim();
                return validateToken(authentication, session);
            }
            return null;
        }

        public User validateToken(String jwt, Session session) throws InstantiationException, IllegalAccessException {
            AppToken appToken = ConnectionManager.get(AppToken.class,
                    new Criterion[]{
                            Restrictions.eq("token", jwt)
                    }, session);

            if(appToken == null){
                LogHelper.log(log, "JWT not present!(validateTokenSession)");
                return null;
            }
            Date expirationDate = appToken.getExpirationDate();
            if (expirationDate.compareTo(DateTimeHelper.getNow()) < 0) {
                LogHelper.log(log, "JWT is expired!(validateTokenSession) - " + expirationDate + ":" + DateTimeHelper.getNow());
                ConnectionManager.delete(appToken, true, session);
                return null;
            }
            return ConnectionManager.get(User.class,
                    new Criterion[]{
                            Restrictions.eq("id", appToken.getUserId())
                    }, session);
        }

        public static Long getExpirationSecs() {
            return EXPIRATION_SECS;
        }

        public static void setExpirationSecs(Long expirationSecs) {
            EXPIRATION_SECS = expirationSecs;
        }
    }
