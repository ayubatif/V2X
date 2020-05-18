package v2x;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.security.PublicKey;
import java.util.Base64;
import java.util.concurrent.Callable;

// https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

/**
 * Waits for an answer that is authenticated and returns it for the third test. If the message is untrustworhy, the
 * certificate is put into the revocation list.
 */
public class ReceiveAnswerThree extends Thread {
    static final int UNICAST_PORT = 2021;
    static final String CA_CERTIFICATE_LOCATION = "Authentication/CA-certificate.crt";
    static final String CRL_LOCATION = "Authentication/CRL-A.crl";
    static final String DNS_CERTIFICATE_LOCATION = "Authentication/DNS-certificate.crt";

    private AnswerCounter answerCounter;
    private ValidityCounter validityCounter;
    byte[] buffer;

    public ReceiveAnswerThree(byte[] buffer,
                              AnswerCounter answerCounter,
                              ValidityCounter validityCounter
    ) {
        this.buffer = buffer;
        this.answerCounter = answerCounter;
        this.validityCounter = validityCounter;
    }

    @Override
    public void run() {

        try {
            System.out.println("running banana bread "+answerCounter.answerZero);
            Message outerMessage = CommunicationFunctions.byteArrayToMessage(buffer);
            String outerAnswer = outerMessage.getValue("Answer");

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
                        if (innerAnswer.equals("0")) {
                            answerCounter.addAnswer(innerAnswer);
                            validityCounter.addValidity("2");
                            String time = outerMessage.getValue("Time");
                            long startTime = Long.parseLong(time);
                            long endTime = System.currentTimeMillis();
                            long totalTime = startTime - endTime;

//                    System.out.println("start time" + startTime);
//                    System.out.println("end time" + endTime);
                            System.out.println("total time" + totalTime);
                        }
                    } else {
                        AuthenticationFunctions.addToCRL(outerCertificate, CRL_LOCATION);
                        validityCounter.addValidity("1");
                    }
                } catch (Exception e) {
                    AuthenticationFunctions.addToCRL(outerCertificate, CRL_LOCATION);
                    validityCounter.addValidity("1");
                }
            } else {
                validityCounter.addValidity("0");
            }
        } catch (Exception e) {
            System.out.println("error two");
            e.printStackTrace();
        }

//        System.out.println(answerCounter.printAnswer());
//        System.out.println(answerCounter.printMath());
//        System.out.println(validityCounter.printValidity());
//        System.out.println(validityCounter.printMath());
//
//        answerCounter.logAnswers();
//        validityCounter.logAnswers();
//        try {
//            answerCounter.exportJSONLog();
//            validityCounter.exportJSONLog();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        serverSocket.close();
    }
}
