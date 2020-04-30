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
    public static String getCertificate(String location) throws IOException {
        File userFile = new File(location);
        InputStream userInputStream = new FileInputStream(userFile);
        byte[] userCertificateByte = Base64.getEncoder().encode(userInputStream.readAllBytes());
        String userCertificateString = new String(userCertificateByte);
        return userCertificateString;
    }

    public static PrivateKey getPrivateKey(String location)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File keyFile = new File(location);
        byte[] keyByte = Files.readAllBytes(keyFile.toPath());
        PKCS8EncodedKeySpec keyPKCS8 = new PKCS8EncodedKeySpec(keyByte);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey userPrivateKey = keyFactory.generatePrivate(keyPKCS8);

        return userPrivateKey;
    }

    public static Key getPublicKey(String certificate) throws CertificateException {
        byte[] userCertificateByteArray = Base64.getDecoder().decode(certificate);
        InputStream inputStream = new ByteArrayInputStream(userCertificateByteArray);
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate userCertificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        Key userPublicKey = userCertificate.getPublicKey();

        return userPublicKey;
    }

//    public static boolean verifyCertificate(String certificate, String caLocation)
//            throws CertificateException, IOException {
//        String caCertificate = getCertificate(caLocation);
//        PublicKey caPublicKey = getPublicKey(caCertificate);
//        byte[] userCertificateByteArray = Base64.getDecoder().decode(certificate);
//        InputStream inputStream = new ByteArrayInputStream(userCertificateByteArray);
//        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//        X509Certificate userCertificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
//        try {
//            userCertificate.verify(caPublicKey);
//            userCertificate.checkValidity();
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public static String hashMessage(String message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        String hashMessage = hash.toString();
        return hashMessage;
    }

    public static String encryptMessage(String message, PrivateKey userPrivateKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, userPrivateKey);
//        byte[] encrypted = cipher.doFinal(message.getBytes());
//        String encryptedMessage = encrypted.toString();
//        return encryptedMessage;
        System.out.println(message);
        byte[] encrypted = cipher.doFinal(message.getBytes());
        System.out.println(encrypted);
        byte[] test = Base64.getEncoder().encode(encrypted);
        System.out.println(test);
        return test.toString();
    }

    public static String decryptMessage(String message, PublicKey userPublicKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, userPublicKey);
//        byte[] decrypted = cipher.doFinal(message.getBytes());
//        String decryptedMessage = decrypted.toString();
//        return decryptedMessage;
        System.out.println(message);
//        byte[] test = Base64.getDecoder().decode(message);
//        byte[] decrypted = cipher.doFinal(test);
//        String decryptedMessage = decrypted.toString();
        return "onion";
    }

//    public static boolean authenticateMessage(String message, String encryptedHash,
//                                              String certificate, String caLocation)
//            throws NoSuchAlgorithmException, CertificateException, IOException, IllegalBlockSizeException,
//            InvalidKeyException, BadPaddingException, NoSuchPaddingException {
//        String calculatedHash = hashMessage(message);
//        PublicKey publicKey = getPublicKey(certificate);
//        String decryptedHash = decryptMessage(encryptedHash, publicKey);
//        boolean certificateVerification = verifyCertificate(certificate, caLocation);
//        if (certificateVerification && calculatedHash.equals(decryptedHash)) {
//            return true;
//        } else {
//            return false;
//        }
//    }

    public static void test(String message, Key userPrivateKey, Key userPublicKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException,
            InvalidKeyException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        Key pub = kp.getPublic();
        Key pvt = kp.getPrivate();

        Cipher cipher1 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher1.init(Cipher.ENCRYPT_MODE, userPrivateKey);
        System.out.println(message);
        System.out.println(message.getBytes());
        byte[] encrypted = cipher1.doFinal(message.getBytes());
        System.out.println(message.getBytes());
        System.out.println("Encrypted");
        System.out.println(Base64.getEncoder().encodeToString(encrypted));
//        String encryptedMessage = encrypted.toString();

        Cipher cipher2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher2.init(Cipher.DECRYPT_MODE, userPublicKey);
        byte[] decrypted = cipher2.doFinal(encrypted);
        System.out.println("Decrypted");
        System.out.println(Base64.getEncoder().encodeToString(decrypted));
        String decryptedMessage = decrypted.toString();
        System.out.println(decryptedMessage);
        String testMessage = new String(decrypted);
        System.out.println(testMessage);

//        System.out.println(decryptedMessage);
    }
}
