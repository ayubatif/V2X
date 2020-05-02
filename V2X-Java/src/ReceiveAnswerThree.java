import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Base64;
import java.util.concurrent.Callable;

// https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

/**
 * Waits for an answer that is authenticated and returns it for the third test. If the message is untrustworhy, the
 * certificate is put into the revocation list.
 */
public class ReceiveAnswerThree implements Callable<String> {
    static final int UNICAST_PORT = 2021;
    static final String CA_CERTIFICATE_LOCATION = "../Authentication/CA-certificate.crt";
    static final String CRL_LOCATION = "../Authentication/CRL-A.crl";
    static final String DNS_CERTIFICATE_LOCATION = "../Authentication/DNS-certificate.crt";

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
                boolean authenticated = AuthenticationFunctions.authenticateMessage(answer, encryptedHash,
                        certificate, CA_CERTIFICATE_LOCATION);
                if (authenticated && !revoked) {
                    byte[] dnsMessageByte = Base64.getDecoder().decode(answer.getBytes());
                    Message dnsMessage = CommunicationFunctions.byteArrayToMessage(dnsMessageByte);
                    String dnsAnswer = dnsMessage.getValue("Message");
                    String dnsEncryptedHash = message.getValue("Hash");
                    System.out.println(dnsEncryptedHash);
                    String dnsCertificate = AuthenticationFunctions.getCertificate(DNS_CERTIFICATE_LOCATION);
                    System.out.println("pompeii");

                    boolean dnsAuthenticated = AuthenticationFunctions.authenticateMessage(dnsAnswer, dnsEncryptedHash,
                            dnsCertificate, DNS_CERTIFICATE_LOCATION);
                    if (dnsAuthenticated) {
                        System.out.println("onion");
                        serverSocket.close();
                        return answer;
                    } else {
                        System.out.println("potato");
                        AuthenticationFunctions.addToCRL(certificate, CRL_LOCATION);
                    }
                }
            }
        }
    }
}
