import java.io.*;
import java.net.*;

public class NonCompromised {
    static final int MULTICAST_PORT = 2020;
    static final int UNICAST_PORT = 2021;
    static final String CERTIFICATE_FOLDER_LOCATION = "~/Desktop/Thesis/Certificate/";
    static final String CA_CERTIFICATE_LOCATION = "~/Desktop/Thesis/Certificate/CA-certificate.crt";

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

    private static String receiveQuery() throws IOException, ClassNotFoundException {
        MulticastSocket serverSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress group = InetAddress.getByName("225.0.0.0");
        serverSocket.joinGroup(group);
        byte[] buffer = new byte[256];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            ObjectInput objectInput = new ObjectInputStream(byteArrayInputStream);
            Message message = (Message) objectInput.readObject();
            String request = message.getValue("Query");
            if (request.equals("Query")) {
                System.out.println("query received");
                String inetAddress = packet.getAddress().getHostAddress();
                return inetAddress;
            }
        }
    }

    private static void sendAnswer(String returnIPAddress) throws IOException {
        InetAddress address = InetAddress.getByName(returnIPAddress);
        DatagramSocket clientSocket = new DatagramSocket();
        Message answer = new Message();
        answer.putValue("Answer", "0");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(answer);
        objectOutputStream.flush();
        byte[] data = byteArrayOutputStream.toByteArray();
        DatagramPacket answerPacket = new DatagramPacket(data, data.length, address, UNICAST_PORT);
        clientSocket.send(answerPacket);
    }

    private static void runFirstTest() throws IOException, ClassNotFoundException {
        while (true) {
            String returnIPAddress = receiveQuery();
            sendAnswer(returnIPAddress);
        }
    }
}
