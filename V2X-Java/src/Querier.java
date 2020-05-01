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
    static final String OWN_CERTIFICATE_LOCATION = "/home/justin/Desktop/Thesis/Certificate/OBU-A-certificate.crt";
    static final String CA_CERTIFICATE_LOCATION = "/home/justin/Desktop/Thesis/Certificate/CA-certificate.crt";
    static final String OWN_PRIVATE_KEY_LOCATION = "/home/justin/Desktop/Thesis/Certificate/OBU-A-private-key.der";

    /**
     * Handles the initialization of the program to see which experiment it is running.
     *
     * @param args input from the command line when running the program
     */
    public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException,
            CertificateException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException {
        int mode = Integer.parseInt(args[0]);
        int testAmount = Integer.parseInt(args[1]);
        switch (mode) {
            case 1:
                System.out.println("running test 1");
                runFirstTest(testAmount);
                break;
            case 2:
                System.out.println("running test 2");
                runSecondTest(testAmount);
                break;
            case 3:
                System.out.println("running test 3");
                break;
            case 0:
                System.out.println("running test");
                test();
                break;
        }
    }

    // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
    // https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html
    /**
     * Sends query message to the 2 OBUs for the first test.
     *
     * @throws IOException
     */
    private static void sendQueryTest1() throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress groupIP = InetAddress.getByName("225.0.0.0");
        multicastSocket.joinGroup(groupIP);
        Message query = new Message();
        query.putValue("Query", "Query");
        byte[] data = CommunicationFunctions.messageToByteArray(query);
        System.out.println(data.length);
        int randomPort = multicastSocket.getLocalPort();
        DatagramPacket queryPacket = new DatagramPacket(data, data.length, groupIP, randomPort);
        multicastSocket.send(queryPacket);
        System.out.println("query sent");
        multicastSocket.close();
    }

    /**
     * Waits for an answer and returns it for the first test.
     *
     * @return <code>String</code> a string from the message received
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static String receiveAnswerTest1() throws IOException, ClassNotFoundException {
        DatagramSocket serverSocket = new DatagramSocket(UNICAST_PORT);
        byte[] buffer = new byte[65508];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            Message message = CommunicationFunctions.byteArrayToMessage(buffer);
            String answer = message.getValue("Answer");
            if (!answer.equals(null)) {
                serverSocket.close();
                return answer;
            }
        }
    }

    /**
     * Handles the first test.
     *
     * @param testAmount an integer specifying the amount of query to be sent
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
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

    // sendQueryTest2() sendQueryTest1() sends query message, hash, and certificate to the 2 OBUs
    private static void sendQueryTest2()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        String userCertificate = AuthenticationFunctions.getCertificate(OWN_CERTIFICATE_LOCATION);
        PrivateKey userPrivateKey = AuthenticationFunctions.getPrivateKey(OWN_PRIVATE_KEY_LOCATION);
        String hash = AuthenticationFunctions.hashMessage("Query");
        String authentication = AuthenticationFunctions.encryptMessage(hash, userPrivateKey);
        MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress groupIP = InetAddress.getByName("225.0.0.0");
        multicastSocket.joinGroup(groupIP);
        Message query = new Message();
        query.putValue("Query", "Query");
        query.putValue("Certificate", userCertificate);
        query.putValue("Hash", authentication);
        System.out.println(query);
        byte[] data = CommunicationFunctions.messageToByteArray(query);
        System.out.println(data);
        System.out.println(data.length);
        int randomPort = multicastSocket.getLocalPort();
        DatagramPacket queryPacket = new DatagramPacket(data, data.length, groupIP, randomPort);
        multicastSocket.send(queryPacket);
        System.out.println("query sent");
        multicastSocket.close();
    }

    // receiveAnswerTest2() waits for an answer that is correct and returns it
    private static String receiveAnswerTest2() throws IOException, ClassNotFoundException {
        DatagramSocket serverSocket = new DatagramSocket(UNICAST_PORT);
        byte[] buffer = new byte[256];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            Message message = CommunicationFunctions.byteArrayToMessage(buffer);
            String answer = message.getValue("Answer");
            if (!answer.equals(null)) {
                serverSocket.close();
                return answer;
            }
        }
    }

    // runSecondTest() handles the second test
    private static void runSecondTest(int testAmount)
            throws IOException, InterruptedException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException,
            InvalidKeySpecException, ClassNotFoundException {
        sendQueryTest2();
        int counter = 0;
        while (counter < testAmount) {
            sendQueryTest2();
            String answer = receiveAnswerTest2();
            System.out.println(answer);
            Thread.sleep(2000);
            counter++;
        }
    }

    private static void test() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            CertificateException {
        String test = "test";
        PrivateKey userPrivateKey = AuthenticationFunctions.getPrivateKey("/home/justin/Desktop/Thesis/Certificate/OBU-X-private-key.der");
//        String encrypt = AuthenticationFunctions.encryptMessage(test, userPrivateKey);
        String certificate = AuthenticationFunctions.getCertificate("/home/justin/Desktop/Thesis/Certificate/OBU-X-certificate.crt");
        PublicKey userPublicKey = AuthenticationFunctions.getPublicKey(certificate);
//        String decrypt = AuthenticationFunctions.decryptMessage(encrypt, userPublicKey);
//        System.out.println(decrypt);
        AuthenticationFunctions.test("potato", userPrivateKey, userPublicKey);
    }
}