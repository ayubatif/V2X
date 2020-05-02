import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

/**
 * Waits for an answer that is authenticated and returns it for the second test. Now built for timeouts
 */
class ReceiveAnswerTwo implements Callable<String> {
    static final int UNICAST_PORT = 2021;
    static final String CA_CERTIFICATE_LOCATION = "../Authentication/CA-certificate.crt";
    static final String CRL_LOCATION = "../Authentication/CRL-A.crl";

    @Override
    public String call() throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(UNICAST_PORT);
        byte[] buffer = new byte[65508];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            Message message = CommunicationFunctions.byteArrayToMessage(buffer);
            String answer = message.getValue("Answer");
            if (!answer.equals(null)) {
                String certificate = message.getValue("Certificate");
                String encryptedHash = message.getValue("Hash");
                boolean revoked = AuthenticationFunctions.checkRevocatedCertificate(certificate, CRL_LOCATION);
                if (AuthenticationFunctions.authenticateMessage(answer, encryptedHash,
                        certificate, CA_CERTIFICATE_LOCATION) && !revoked) {
                    serverSocket.close();
                    return answer;
                }
            }
        }
    }
}
