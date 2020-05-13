package v2x;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Base64;
import java.util.concurrent.Callable;

// https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

/**
 * Waits for an answer that is authenticated and returns it for the third test. If the message is untrustworhy, the
 * certificate is put into the revocation list.
 */
public class ReceiveAnswerFour implements Callable<String> {
    static final int UNICAST_PORT = 2021;
    static final String CA_CERTIFICATE_LOCATION = "Authentication/CA-certificate.crt";
    static final String CRL_LOCATION = "Authentication/CRL-A.crl";
    static final String BLOOM_FILTER_LOCATION = "Authentication/DNS-bloom-filter.bf";

    private final DatagramSocket serverSocket;

    public ReceiveAnswerFour(DatagramSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public String call() throws Exception {
        byte[] buffer = new byte[65508];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(receivePacket);
            Message outerMessage = CommunicationFunctions.byteArrayToMessage(buffer);
            String outerAnswer = outerMessage.getValue("Answer");

            if (!outerAnswer.equals(null)) {
                String outerCertificate = outerMessage.getValue("Certificate");
                String outerEncryptedHash = outerMessage.getValue("Hash");

                boolean outerAuthentication = AuthenticationFunctions.authenticateMessage(
                        outerAnswer, outerEncryptedHash, outerCertificate, CA_CERTIFICATE_LOCATION);
                boolean outerRevoked = AuthenticationFunctions.checkRevocatedCertificate(
                        outerCertificate, CRL_LOCATION);

                if (outerAuthentication && !outerRevoked) {
                    byte[] decodedInnerAnswer = Base64.getDecoder().decode(outerAnswer);
                    Message innerMessage = CommunicationFunctions.byteArrayToMessage(decodedInnerAnswer);


                    String innerAnswer = innerMessage.getValue("Answer");

                    try {
                        DNSBloomFilter signedIPs = AuthenticationFunctions.getBloomFilter(BLOOM_FILTER_LOCATION);
                        boolean innerAuthentication = AuthenticationFunctions
                                .checkSignedAAAARecord(innerAnswer, signedIPs);
                        boolean isResponseMalicious = !DNSBloomFilterFunctions.getFixedAAAA().equals(innerAnswer);
                        if (innerAuthentication && !isResponseMalicious) {
                            serverSocket.close();
                            return "0";
                        } else {
                            AuthenticationFunctions.addToCRL(outerCertificate, CRL_LOCATION);
                            serverSocket.close();
                            return "1";
                        }
                    } catch (Exception e) {
                        AuthenticationFunctions.addToCRL(outerCertificate, CRL_LOCATION);
                        serverSocket.close();
                        return "1";
                    }
                }
            }
        }
    }
}
