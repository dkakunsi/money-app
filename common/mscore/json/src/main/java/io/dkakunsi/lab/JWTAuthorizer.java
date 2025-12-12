package io.dkakunsi.lab;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import io.dkakunsi.lab.common.Configuration;
import io.dkakunsi.lab.common.DefaultLogger;
import io.dkakunsi.lab.common.Logger;
import io.dkakunsi.lab.common.security.AuthorizedPrincipal;
import io.dkakunsi.lab.common.security.Authorizer;

/**
 * The {@code JWTAuthentication} class is responsible for handling JWT-based
 * authentication. It implements the {@link Authentication} interface and
 * provides methods to verify JWT tokens and extract user information from them.
 *
 * <p>
 * This class uses an RSA public key to verify the signature of the JWT tokens.
 * The public key is configured via the application configuration and must be
 * provided in the correct format.
 *
 * <p>
 * Key features of this class include:
 * <ul>
 * <li>Parsing and verifying JWT tokens using the RSA256 algorithm.</li>
 * <li>Extracting user information such as ID, name, email, and roles from the
 * token payload.</li>
 * <li>Handling configuration and decoding of the RSA public key.</li>
 * </ul>
 *
 * <p>
 * Note: The {@code storeToken} method is not implemented and will throw an
 * {@link RuntimeException}
 * if called.
 *
 * <p>
 * Usage example:
 * 
 * <pre>{@code
 * Configuration configuration = ...; // Obtain application configuration
 * JWTAuthentication authentication = JWTAuthentication.of(configuration);
 * User user = authentication.verify("Bearer <token>");
 * }</pre>
 *
 * @author Deddy Kakunsi
 * @see Authentication
 * @see User
 */
public class JWTAuthorizer implements Authorizer {

  public static final String SUB = "sub";

  public static final String REALM_ACCESS = "realm_access";

  private static final Logger LOG = DefaultLogger.getLogger(JWTAuthorizer.class);

  protected static final String PUBLIC_KEY = "jwt.public.key";

  protected RSAPublicKey publicKey;

  protected JWTAuthorizer(RSAPublicKey publicKey) {
    this.publicKey = publicKey;
  }

  public static JWTAuthorizer of(Configuration configuration) {
    var publicKeyString = configuration.get(PUBLIC_KEY)
        .orElseThrow(() -> new RuntimeException("Public key is not configured correctly"));
    try {
      var publicKey = toRSAPublicKey(publicKeyString.getBytes(StandardCharsets.UTF_8.name()));
      return new JWTAuthorizer(publicKey);
    } catch (UnsupportedEncodingException ex) {
      throw new RuntimeException("Invalid security configuration", ex);
    }
  }

  protected static RSAPublicKey toRSAPublicKey(byte[] byteKey) {
    try {
      var keySpec = new X509EncodedKeySpec(decode(byteKey));
      var keyFactory = KeyFactory.getInstance("RSA");
      return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
      LOG.error("Failed to create RSA public key");
      throw new RuntimeException("Cannot create RSA key for authentication", ex);
    }
  }

  @Override
  public AuthorizedPrincipal verify(String key) {
    if (StringUtils.isBlank(key)) {
      LOG.debug("Session key is not provided");
      return null;
    }

    LOG.debug("Verifying session with token '{}'", key);
    var token = key.replace("Bearer ", "");
    var algorithm = Algorithm.RSA256(publicKey, null);
    var verifier = JWT.require(algorithm).build();
    try {
      var jwt = verifier.verify(token);
      var payload = new JSONObject(decode(jwt.getPayload()));
      var email = payload.optString("email");
      return new AuthorizedPrincipal(email);
    } catch (JWTVerificationException ex) {
      LOG.info("Token is not valid: '{}'", token);
      throw new IllegalArgumentException("Token is not valid", ex);
    } catch (JSONException | UnsupportedEncodingException ex) {
      LOG.error("Cannot verify token", ex);
      throw new RuntimeException(ex);
    }
  }

  private static String decode(String encoded) throws UnsupportedEncodingException {
    var decoded = decode(encoded.getBytes(StandardCharsets.UTF_8.name()));
    return new String(decoded, StandardCharsets.UTF_8.name());
  }

  private static byte[] decode(byte[] encoded) {
    return Base64.getDecoder().decode(encoded);
  }
}
