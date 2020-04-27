import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Querier {
    static final int PORT = 2020;
    static final String CERTIFICATE_FOLDER_LOCATION = "~/Desktop/Thesis/Certificate/";
    static final String CA_CERTIFICATE_LOCATION = "~/Desktop/Thesis/Certificate/CA-certificate.crt";

    // main() handles the initialization of the program to see which experiment it is running
    public static void main(String args[]) throws IOException {
        int mode = Integer.parseInt(args[0]);
        switch (mode) {
            case 1:
                runFirstTest();
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    // sendQuery sends query message to the 2 obu
    // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
    private static MulticastSocket sendQuery() throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(PORT);
        // InetAddress groupIP = InetAddress.getByName("192.168.2.0");
        InetAddress groupIP = InetAddress.getByName("192.168.1.0");
        multicastSocket.joinGroup(groupIP);
        Message query = new Message();
        query.putValue("Query", "Query");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(query);
        objectOutputStream.flush();
        byte[] data = byteArrayOutputStream.toByteArray();
        DatagramPacket queryPacket = new DatagramPacket(data, data.length, groupIP, PORT);
        multicastSocket.send(queryPacket);
        return multicastSocket;
    }

    // runFirstTest() handles the first test
    private static void runFirstTest() throws IOException {
        MulticastSocket multicastSocket = sendQuery();
        System.out.println("sent");
    }
}