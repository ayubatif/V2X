package v2x;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

import static v2x.PseudonymAuthority.CERTIFICATE_AMOUNT;

public class WaitQueryFour extends Thread {
    private MulticastSocket serverSocket;
    private int unicastPort;
    private String caCertificateLocation;
    private String answer;
    private String ownCertificateLocation;
    private String ownPrivateKeyLocation;
    private int rate;
    private String dnsPrivateKeylocation;

    public WaitQueryFour(MulticastSocket serverSocket, int unicastPort, String answer,
                         String caCertificateLocation, String ownCertificateLocation, String ownPrivateKeyLocation,
                         int rate, String dnsPrivateKeylocation) {
        this.serverSocket = serverSocket;
        this.unicastPort = unicastPort;
        this.caCertificateLocation = caCertificateLocation;
        this.answer = answer;
        this.ownCertificateLocation = ownCertificateLocation;
        this.ownPrivateKeyLocation = ownPrivateKeyLocation;
        this.rate = rate;
        this.dnsPrivateKeylocation = dnsPrivateKeylocation;
    }

    @Override
    public void run() {
        int counter = 0;
        int number = 0;
        byte[] buffer = new byte[65508];
        while (true) {
            try {
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
                        String givenNumber = message.getValue("TestNumber");
                        counter = Integer.parseInt(givenNumber);
                        ReturnQueryFour returnQueryFour =
                                new ReturnQueryFour(inetAddress, time, unicastPort, answer,
                                        ownCertificateLocation, ownPrivateKeyLocation, number, dnsPrivateKeylocation);
                        returnQueryFour.start();
                        if (number > CERTIFICATE_AMOUNT - 2) {
                            System.out.println("certificate limit reached");
                        }
                        else if (counter != 0 && counter % rate == 0) {
                            System.out.println("changing certificate");
                            number++;
                        }
//                        counter++;
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
}
