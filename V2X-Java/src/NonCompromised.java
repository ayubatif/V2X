import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

public class NonCompromised {
    static final int PORT = 2020;
    static final String CERTIFICATE_FOLDER_LOCATION = "~/Desktop/Thesis/Certificate/";
    static final String CA_CERTIFICATE_LOCATION = "~/Desktop/Thesis/Certificate/CA-certificate.crt";

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        int mode = Integer.parseInt(args[0]);
        switch (mode)
        {
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

    private static boolean receiveQuery() throws IOException, ClassNotFoundException {
        MulticastSocket serverSocket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName("225.0.0.0");
        serverSocket.joinGroup(group);
        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true)
        {
            serverSocket.receive(packet);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
            ObjectInput objectInput = new ObjectInputStream(byteArrayInputStream);
            Message message = (Message) objectInput.readObject();
            String request = message.getValue("Query");
            if (request.equals("Query"))
            {
                return true;
            }
        }
    }

    private static void runFirstTest() throws IOException, ClassNotFoundException {
        if (receiveQuery()) {
            System.out.println("received");
        }
    }
}
