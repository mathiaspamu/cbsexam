package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import model.User;
import org.bouncycastle.util.encoders.Hex;

public final class Hashing {

  // TODO: You should add a salt and make this secure (FIX - created time instead of salt)
  public static String sha(String rawString, long created_at) {

    if (rawString == "null") {
      return "null";

    } else
      try {
        // We load the hashing algorithm we wish to use.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Add created time which will give every new user a unique password but not the ones already in the database
        rawString = rawString + created_at;

        // We convert to byte array
        byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));

        // We create the hashed string
        String sha256hex = new String(Hex.encode(hash));

        // And return the string
        return sha256hex;

      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }

    return rawString;
  }
}


  /**
   * Random salt with fixed length stored as byte array
   * @return
   * @throws NoSuchAlgorithmException kommentar
   * @throws NoSuchProviderException kommentar
   */
/*  public static byte[] generateSalt() throws NoSuchAlgorithmException, NoSuchProviderException {

    int lengthOfSalt = 16;

    //
    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");

    byte[] salt = new byte[lengthOfSalt];

    //
    secureRandom.nextBytes(salt);

    return salt;
  } */