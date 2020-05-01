import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class AuthenticationFunctions {
    /**
     * Takes in a location and gets the certificate as a string.
     *
     * @param location a string of the location of the certificate
     * @return <code>String</code> a string representation of the certificate
     * @throws IOException
     */
    public static String getCertificate(String location) throws IOException {
        File userFile = new File(location);
        InputStream userInputStream = new FileInputStream(userFile);
        byte[] userCertificateByte = Base64.getEncoder().encode(userInputStream.readAllBytes());
        String userCertificateString = new String(userCertificateByte);
        return userCertificateString;
    }

    /**
     * Takes in a location and gets the private key.
     *
     * @param location a string of the location of the private key
     * @return <code>PrivateKey</code> a private key
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey getPrivateKey(String location)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File keyFile = new File(location);
        byte[] keyByte = Files.readAllBytes(keyFile.toPath());
        PKCS8EncodedKeySpec keyPKCS8 = new PKCS8EncodedKeySpec(keyByte);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey userPrivateKey = keyFactory.generatePrivate(keyPKCS8);

        return userPrivateKey;
    }

    /**
     * Takes in a string representation of a certificate and extracts the public key.
     *
     * @param certificate a string representation of a certificate
     * @return <code>PublicKey</code> a public key from teh certificate
     * @throws CertificateException
     */
    public static PublicKey getPublicKey(String certificate) throws CertificateException {
        byte[] userCertificateByteArray = Base64.getDecoder().decode(certificate);
        InputStream inputStream = new ByteArrayInputStream(userCertificateByteArray);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate userCertificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        PublicKey userPublicKey = userCertificate.getPublicKey();

        return userPublicKey;
    }

    /**
     * Verifies a certificate with a CA certificate.
     *
     * @param certificate a base64 string representation of a certificate
     * @param caLocation a string of the location of the CA certificate
     * @return <code>true</code> if certificate is valid
     *         <code>false</code> if certificate invalid
     * @throws CertificateException
     * @throws IOException
     */
    public static boolean verifyCertificate(String certificate, String caLocation)
            throws CertificateException, IOException {
        String caCertificate = getCertificate(caLocation);
        PublicKey caPublicKey = getPublicKey(caCertificate);
        byte[] userCertificateByteArray = Base64.getDecoder().decode(certificate);
        InputStream inputStream = new ByteArrayInputStream(userCertificateByteArray);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate userCertificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        try {
            userCertificate.verify(caPublicKey);
            userCertificate.checkValidity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hashes a string with sha-256 and returns it.
     *
     * @param message a string to be hashed
     * @return <code>String</code> the hash of the message
     * @throws NoSuchAlgorithmException
     */
    public static String hashMessage(String message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        String hashMessage = new String(hash);
        return hashMessage;
    }

    /**
     * Encrypts the message with RSA using a private key.
     *
     * @param message a string with the message to be encrypted
     * @param userPrivateKey a private key to encrypt with
     * @return <code>String</code> a base64 encrypted string
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static String encryptMessage(String message, PrivateKey userPrivateKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, userPrivateKey);
        byte[] encrypted = cipher.doFinal(message.getBytes());
        byte[] encryptedBase64 = Base64.getEncoder().encode(encrypted);
        String encryptedMessage = new String(encryptedBase64);
        return encryptedMessage;
    }

    /**
     * Decrypts the message with RSA using a public key.
     *
     * @param message a base64 encrypted string with the message
     * @param userPublicKey a public key to decrypt with
     * @return <code>String</code> a decoded and decrypted string
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static String decryptMessage(String message, PublicKey userPublicKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException {
        byte[] messageByte = Base64.getDecoder().decode(message);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, userPublicKey);
        byte[] decrypted = cipher.doFinal(messageByte);
        String decryptedMessage = new String(decrypted);
        return decryptedMessage;
    }

    public static boolean authenticateMessage(String message, String encryptedHash,
                                              String certificate, String caLocation)
            throws NoSuchAlgorithmException, CertificateException, IOException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        String calculatedHash = hashMessage(message);
        PublicKey publicKey = getPublicKey(certificate);
        String decryptedHash = decryptMessage(encryptedHash, publicKey);
        boolean certificateVerification = verifyCertificate(certificate, caLocation);
        if (certificateVerification && calculatedHash.equals(decryptedHash)) {
            return true;
        } else {
            return false;
        }
    }

    public static void test() {
    }
}
