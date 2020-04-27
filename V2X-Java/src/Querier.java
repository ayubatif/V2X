import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Querier {
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
        MulticastSocket multicastSocket = new MulticastSocket(PORT);
        InetAddress GROUP_IP = InetAddress.getByName("192.168.2.0");
        
    }
}