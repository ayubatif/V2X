package v2x;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

// https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

/**
 * Waits for an answer that is authenticated and returns it for the second test. Now built for timeouts
 */
class ReceiveAnswerTwo extends Thread {
    static final String CA_CERTIFICATE_LOCATION = "Authentication/CA-certificate.crt";
    static final String CRL_LOCATION = "Authentication/CRL-A.crl";

    private final DatagramSocket serverSocket;
    private AnswerCounter answerCounter;
    private ValidityCounter validityCounter;
    private TimeCounter timeCounter;
    private int counter;
    private ThreadCommunication threadCommunication;

    public ReceiveAnswerTwo(DatagramSocket serverSocket,
                            AnswerCounter answerCounter,
                            ValidityCounter validityCounter,
                            TimeCounter timeCounter, int counter,
                            ThreadCommunication threadCommunication) {
        this.serverSocket = serverSocket;
        this.answerCounter = answerCounter;
        this.validityCounter = validityCounter;
        this.timeCounter = timeCounter;
        this.counter = counter;
        this.threadCommunication = threadCommunication;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[65508];

        int counter = 0;
        boolean run = true;

        while (run) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                serverSocket.receive(packet);
                Message message = CommunicationFunctions.byteArrayToMessage(buffer);
                String answer = message.getValue("Answer");

                String certificate = message.getValue("Certificate");
                String encryptedHash = message.getValue("Hash");
                boolean revoked = AuthenticationFunctions.checkRevocatedCertificate(certificate, CRL_LOCATION);
                boolean authenticated = AuthenticationFunctions.authenticateMessage(answer, encryptedHash,
                        certificate, CA_CERTIFICATE_LOCATION);

                if (authenticated && !revoked) {

                    if (answer.equals("0")) {
                        long endTime = System.currentTimeMillis();
                        String time = message.getValue("Time");
                        long startTime = Long.parseLong(time);
                        long totalTime = endTime - startTime;

//                    System.out.println("start time" + startTime);
//                    System.out.println("end time" + endTime);
                    System.out.println("total time " + totalTime);
                        timeCounter.addTime(totalTime);
                    }

                    answerCounter.addAnswer(answer);
                    validityCounter.addValidity("2");

                    run = false;
                    serverSocket.close();
                    threadCommunication.setReady(true);

                } else {
                    validityCounter.addValidity("0");
                }

            } catch (SocketException e) {
                System.out.println("Thread ended");
                run = false;
            } catch (Exception e) {
                System.out.println("error two");
                e.printStackTrace();
            }
        }
    }
}
