import java.io.*;
import java.net.ServerSocket;
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
                runFirstTest();
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    private static boolean receiveQuery() throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket clientSocket = serverSocket.accept();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        while (true)
        {
            String data = bufferedReader.readLine();
            byte[] dataByte = data.getBytes();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dataByte);
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
