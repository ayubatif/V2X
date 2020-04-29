import java.io.*;
import java.net.*;

public class Querier {
    static final int MULTICAST_PORT = 2020;
    static final int UNICAST_PORT = 2021;
    static final String CERTIFICATE_FOLDER_LOCATION = "~/Desktop/Thesis/Certificate/";
    static final String CA_CERTIFICATE_LOCATION = "~/Desktop/Thesis/Certificate/CA-certificate.crt";

    // main() handles the initialization of the program to see which experiment it is running
    public static void main(String args[]) throws IOException, ClassNotFoundException {
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

    // sendQuery sends query message to the 2 obu
    // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
    // https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html
    private static void sendQuery() throws IOException {
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

    private static String receiveAnswer() throws IOException, ClassNotFoundException {
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
    private static void runFirstTest(int testAmount) throws IOException, ClassNotFoundException {
        int counter = 0;
        while (counter < testAmount) {
            sendQuery();
            String answer = receiveAnswer();
            System.out.println(answer);
            counter++;
        }
    }
}