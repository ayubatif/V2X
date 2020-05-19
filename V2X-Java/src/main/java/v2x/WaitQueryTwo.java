package v2x;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class WaitQueryTwo extends Thread {
    private MulticastSocket serverSocket;
    private int unicastPort;
    private String caCertificateLocation;
    private String answer;
    private String ownCertificateLocation;
    private String ownPrivateKeyLocation;

    public WaitQueryTwo(MulticastSocket serverSocket, int unicastPort, String answer,
                        String caCertificateLocation, String ownCertificateLocation, String ownPrivateKeyLocation) {
        this.serverSocket = serverSocket;
        this.unicastPort = unicastPort;
        this.caCertificateLocation = caCertificateLocation;
        this.answer = answer;
        this.ownCertificateLocation = ownCertificateLocation;
        this.ownPrivateKeyLocation = ownPrivateKeyLocation;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[65508];
        while (true) {
            try {
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(packet);
                    Message message = CommunicationFunctions.byteArrayToMessage(buffer);
                    String request = message.getValue("Query");
                    if (request.equals("Query")) {
                        //System.out.println("query received");
                        String certificate = message.getValue("Certificate");
                        String encryptedHash = message.getValue("Hash");
                        if (AuthenticationFunctions.authenticateMessage(request, encryptedHash,
                                certificate, caCertificateLocation)) {
                            String inetAddress = packet.getAddress().getHostAddress();
                            String time = message.getValue("Time");
                            ReturnQueryTwo returnQueryTwo =
                                    new ReturnQueryTwo(inetAddress, time, unicastPort, answer,
                                            ownCertificateLocation, ownPrivateKeyLocation);
                            returnQueryTwo.start();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
