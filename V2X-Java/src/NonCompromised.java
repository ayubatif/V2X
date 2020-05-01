import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class NonCompromised {
    static final int MULTICAST_PORT = 2020;
    static final int UNICAST_PORT = 2021;
    static final String OWN_CERTIFICATE_LOCATION = "/home/justin/Desktop/Thesis/Certificate/OBU-N-certificate-test.crt";
    static final String CA_CERTIFICATE_LOCATION = "/home/justin/Desktop/Thesis/Certificate/CA-certificate.crt";
    static final String OWN_PRIVATE_KEY_LOCATION = "/home/justin/Desktop/Thesis/Certificate/OBU-N-private-key.der";

    /**
     * Handles the initialization of the program to see which experiment it is running.
     *
     * @param args input from the command line when running the program
     */
    public static void main(String args[]) throws IOException, ClassNotFoundException, CertificateException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException,
            InvalidKeyException {
        int mode = Integer.parseInt(args[0]);
        switch (mode) {
            case 1:
                System.out.println("running test 1");
                runFirstTest();
                break;
            case 2:
                System.out.println("running test 2");
                runSecondTest();
                break;
            case 3:
                System.out.println("running test 3");
                break;
        }
    }

    /**
     * Waits for an input and checks if it is a query for the first test.
     *
     * @return inetAddress a string that is the IP address of the sender
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static String receiveQueryTest1() throws IOException, ClassNotFoundException {
        MulticastSocket serverSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress group = InetAddress.getByName("225.0.0.0");
        serverSocket.joinGroup(group);
        byte[] buffer = new byte[256];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            Message message = CommunicationFunctions.byteArrayToMessage(buffer);
            String request = message.getValue("Query");
            if (request.equals("Query")) {
                System.out.println("query received");
                String inetAddress = packet.getAddress().getHostAddress();
                return inetAddress;
            }
        }
    }

    /**
     * Sends a message with the correct answer for the first test.
     *
     * @param returnIPAddress a string that is the IP address of who to send to
     * @throws IOException
     */
    private static void sendAnswerTest1(String returnIPAddress) throws IOException {
        InetAddress address = InetAddress.getByName(returnIPAddress);
        DatagramSocket clientSocket = new DatagramSocket();
        Message answer = new Message();
        answer.putValue("Answer", "0");
        byte[] data = CommunicationFunctions.messageToByteArray(answer);
        DatagramPacket answerPacket = new DatagramPacket(data, data.length, address, UNICAST_PORT);
        clientSocket.send(answerPacket);
    }

    /**
     * Handles the first test.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void runFirstTest() throws IOException, ClassNotFoundException {
        while (true) {
            String returnIPAddress = receiveQueryTest1();
            sendAnswerTest1(returnIPAddress);
        }
    }

    // receiveQueryTest2() waits for an input, checks if it is a query, and checks if it is correctly authenticated
    private static String receiveQueryTest2() throws IOException, ClassNotFoundException, CertificateException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            NoSuchPaddingException, InvalidKeyException {
        MulticastSocket serverSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress group = InetAddress.getByName("225.0.0.0");
        serverSocket.joinGroup(group);
        byte[] buffer = new byte[65508];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            Message message = CommunicationFunctions.byteArrayToMessage(buffer);
            String request = message.getValue("Query");
            if (request.equals("Query")) {
                System.out.println("query received");
                String certificate = message.getValue("Certificate");
                String encryptedHash = message.getValue("Hash");
                if (AuthenticationFunctions.authenticateMessage("Query", encryptedHash,
                        certificate, CA_CERTIFICATE_LOCATION)) {
                    String inetAddress = packet.getAddress().getHostAddress();
                    return inetAddress;
                }
            }
        }
    }

    // runFirstTest() handles the second test
    private static void runSecondTest() throws IOException, ClassNotFoundException, CertificateException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException,
            InvalidKeyException {
        while (true) {
            String returnIPAddress = receiveQueryTest2();
        }
    }

    private static void test() {

    }
}
