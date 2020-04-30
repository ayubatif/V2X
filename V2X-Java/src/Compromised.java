import java.io.*;
import java.net.*;

public class Compromised {
    static final int MULTICAST_PORT = 2020;
    static final int UNICAST_PORT = 2021;
    static final String OWN_CERTIFICATE_LOCATION = "/home/justin/Desktop/Thesis/Certificate/OBU-X-certificate-test.crt";
    static final String CA_CERTIFICATE_LOCATION = "/home/justin/Desktop/Thesis/Certificate/CA-certificate.crt";
    static final String OWN_PRIVATE_KEY_LOCATION = "/home/justin/Desktop/Thesis/Certificate/OBU-X-private-key.der";

    /**
     * Handles the initialization of the program to see which experiment it is running
     *
     * @param args input from the command line when running the program
     */
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        int mode = Integer.parseInt(args[0]);
        switch (mode) {
            case 1:
                System.out.println("running test 1");
                runFirstTest();
                break;
            case 2:
                System.out.println("running test 2");
                break;
            case 3:
                System.out.println("running test 3");
                break;
        }
    }

    // receiveQueryTest1() waits for an input and checks if it is a query

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
     * Sends a message with the incorrect answer.
     *
     * @param returnIPAddress a string that is the IP address of who to send to
     * @throws IOException
     */
    private static void sendAnswerTest1(String returnIPAddress) throws IOException {
        InetAddress address = InetAddress.getByName(returnIPAddress);
        DatagramSocket clientSocket = new DatagramSocket();
        Message answer = new Message();
        answer.putValue("Answer", "1");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(answer);
        objectOutputStream.flush();
        byte[] data = byteArrayOutputStream.toByteArray();
        DatagramPacket answerPacket = new DatagramPacket(data, data.length, address, UNICAST_PORT);
        clientSocket.send(answerPacket);
    }

    // runFirstTest() handles the first test
    private static void runFirstTest() throws IOException, ClassNotFoundException {
        while (true) {
            String returnIPAddress = receiveQueryTest1();
            sendAnswerTest1(returnIPAddress);
        }
    }
}
