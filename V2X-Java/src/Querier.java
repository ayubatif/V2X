import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class Querier {
    static final int MULTICAST_PORT = 2020;
    static final int UNICAST_PORT = 2021;
    static final String OWN_CERTIFICATE_LOCATION = "~/Desktop/Thesis/Certificate/OBU-A-certificate-test.crt";
    static final String CA_CERTIFICATE_LOCATION = "~/Desktop/Thesis/Certificate/CA-certificate.crt";
    static final String OWN_PRIVATE_KEY = "~/Desktop/Thesis/Certificate/OBU-A-private-key.der";

    // main() handles the initialization of the program to see which experiment it is running
    public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException {
        int mode = Integer.parseInt(args[0]);
        int testAmount = Integer.parseInt(args[1]);
        switch (mode) {
            case 1:
                System.out.println("running test 1");
                runFirstTest(testAmount);
                break;
            case 2:
                System.out.println("running test 2");
                break;
            case 3:
                System.out.println("running test 3");
                break;
        }
    }

    // sendQueryTest1() sends query message to the 2 obu
    // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
    // https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html
    private static void sendQueryTest1() throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress groupIP = InetAddress.getByName("225.0.0.0");
        multicastSocket.joinGroup(groupIP);
        Message query = new Message();
        query.putValue("Query", "Query");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(query);
        objectOutputStream.flush();
        byte[] data = byteArrayOutputStream.toByteArray();
        int randomPort = multicastSocket.getLocalPort();
        DatagramPacket queryPacket = new DatagramPacket(data, data.length, groupIP, randomPort);
        multicastSocket.send(queryPacket);
        System.out.println("query sent");
        multicastSocket.close();
    }

    // receiveAnswerTest1() waits for an answer and returns it
    private static String receiveAnswerTest1() throws IOException, ClassNotFoundException {
        DatagramSocket serverSocket = new DatagramSocket(UNICAST_PORT);
        byte[] buffer = new byte[256];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            ObjectInput objectInput = new ObjectInputStream(byteArrayInputStream);
            Message message = (Message) objectInput.readObject();
            String answer = message.getValue("Answer");
            if (!answer.equals(null)) {
                serverSocket.close();
                return answer;
            }
        }
    }

    // runFirstTest() handles the first test
    private static void runFirstTest(int testAmount) throws IOException, ClassNotFoundException, InterruptedException {
        int counter = 0;
        while (counter < testAmount) {
            sendQueryTest1();
            String answer = receiveAnswerTest1();
            System.out.println(answer);
            Thread.sleep(2000);
            counter++;
        }
    }

    private static String getOwnCertificate() throws IOException, CertificateException {
        File userFile = new File(OWN_CERTIFICATE_LOCATION);
        InputStream userInputStream = new FileInputStream(userFile);
        byte[] userCertificateByte = Base64.getEncoder().encode(userInputStream.readAllBytes());
        String userCertificateString = new String(userCertificateByte);
        return userCertificateString;
    }

    private static PrivateKey getOwnPrivateKey()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        File keyFile = new File(OWN_PRIVATE_KEY);
        byte[] keyByte = Files.readAllBytes(keyFile.toPath());
        PKCS8EncodedKeySpec keyPKCS8 = new PKCS8EncodedKeySpec(keyByte);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey userPrivateKey = keyFactory.generatePrivate(keyPKCS8);
        return userPrivateKey;
    }

    private static String hashMessage(String message) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
        String hashMessage = hash.toString();
        return hashMessage;
    }

    private static String encryptMessage(String message, PrivateKey userPrivateKey)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, userPrivateKey);
        byte[] encrypted = cipher.doFinal(message.getBytes());
        String encryptedMessage = encrypted.toString();
        return encryptedMessage;
    }

    private static void sendQueryTest2()
            throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        String userCertificate = getOwnCertificate();
        PrivateKey userPrivateKey = getOwnPrivateKey();
        String hash = hashMessage("Query");
        String authentication = encryptMessage(hash, userPrivateKey);
        MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress groupIP = InetAddress.getByName("225.0.0.0");
        multicastSocket.joinGroup(groupIP);
        Message query = new Message();
        query.putValue("Query", "Query");
        query.putValue("Certificate", userCertificate);
        query.putValue("Hash", authentication);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(query);
        objectOutputStream.flush();
        byte[] data = byteArrayOutputStream.toByteArray();
        int randomPort = multicastSocket.getLocalPort();
        DatagramPacket queryPacket = new DatagramPacket(data, data.length, groupIP, randomPort);
        multicastSocket.send(queryPacket);
        System.out.println("query sent");
        multicastSocket.close();
    }

//    private static void runSecondTest(int testAmount) throws IOException, ClassNotFoundException, InterruptedException {
//        int counter = 0;
//        while (counter < testAmount) {
//            sendQueryTest2();
//            String answer = receiveAnswerTest2();
//            System.out.println(answer);
//            Thread.sleep(2000);
//            counter++;
//        }
//    }
}