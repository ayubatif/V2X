package v2x;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.PublicKey;
import java.util.Base64;
import java.util.concurrent.Callable;

// https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

/**
 * Waits for an answer that is authenticated and returns it for the third test. If the message is untrustworhy, the
 * certificate is put into the revocation list.
 */
public class ReceiveAnswerThree implements Callable<String> {
    static final int UNICAST_PORT = 2021;
    static final String CA_CERTIFICATE_LOCATION = "Authentication/CA-certificate.crt";
    static final String CRL_LOCATION = "Authentication/CRL-A.crl";
    static final String DNS_CERTIFICATE_LOCATION = "Authentication/DNS-certificate.crt";

    private final DatagramSocket serverSocket;

    public ReceiveAnswerThree(DatagramSocket serverSocket) {
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
                    String innerCertificate = AuthenticationFunctions.getCertificate(DNS_CERTIFICATE_LOCATION);
                    String innerEncryptedHash = innerMessage.getValue("Hash");

                    boolean innerAuthentication = false;
                    try {
                        String calculatedHash = AuthenticationFunctions.hashMessage(innerAnswer);
                        PublicKey publicKey = AuthenticationFunctions.getPublicKey(innerCertificate);
                        String decryptedHash = AuthenticationFunctions.decryptMessage(innerEncryptedHash, publicKey);
                        boolean certificateVerification = AuthenticationFunctions.verifyCertificate(
                                innerCertificate, CA_CERTIFICATE_LOCATION);

                        if (certificateVerification && calculatedHash.equals(decryptedHash)) {
                            innerAuthentication = true;
                        }

                        /* Check if DNS server is revocated */
                        boolean innerRevoked = AuthenticationFunctions.checkRevocatedCertificate(
                                innerCertificate, CRL_LOCATION);

                        if (innerAuthentication && !innerRevoked) {
                            serverSocket.close();
                            return innerAnswer;
                        } else {
                            AuthenticationFunctions.addToCRL(outerCertificate, CRL_LOCATION);
                        }
                    } catch (Exception e) {
                        AuthenticationFunctions.addToCRL(outerCertificate, CRL_LOCATION);
                    }
                }
            }
        }
    }
}
