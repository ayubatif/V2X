import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class NonCompromised {
    static final int PORT = 2020;
    static final String CERTIFICATE_FOLDER_LOCATION = "~/Desktop/Thesis/Certificate/";
    static final String CA_CERTIFICATE_LOCATION = "~/Desktop/Thesis/Certificate/CA-certificate.crt";

    public static void main(String args[]) throws IOException {
        int mode = Integer.getInteger(args[0]);
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

    private static void runFirstTest() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket clientSocket = serverSocket.accept();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    }
}
