package v2x;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.PrivateKey;

public class ReturnQueryTwo extends Thread {
    private String inetAddress;
    private String time;
    private int unicastPort;
    private String answer;
    private String ownCertificateLocation;
    private String ownPrivateKeyLocation;

    public ReturnQueryTwo(String inetAddress, String time, int unicastPort, String answer,
                          String ownCertificateLocation, String ownPrivateKeyLocation) {
        this.inetAddress = inetAddress;
        this.time = time;
        this.unicastPort = unicastPort;
        this.answer = answer;
        this.ownCertificateLocation = ownCertificateLocation;
        this.ownPrivateKeyLocation = ownPrivateKeyLocation;
    }

    @Override
    public void run() {
        try {
            String userCertificate = AuthenticationFunctions.getCertificate(ownCertificateLocation);
            PrivateKey userPrivateKey = AuthenticationFunctions.getPrivateKey(ownPrivateKeyLocation);
            String message = answer;
            String hash = AuthenticationFunctions.hashMessage(message);
            String authentication = AuthenticationFunctions.encryptMessage(hash, userPrivateKey);
            InetAddress address = InetAddress.getByName(inetAddress);
            DatagramSocket clientSocket = new DatagramSocket();
            Message answer = new Message();
            answer.putValue("Answer", message);
            answer.putValue("Certificate", userCertificate);
            answer.putValue("Hash", authentication);
            answer.putValue("Time", time);
            byte[] data = CommunicationFunctions.messageToByteArray(answer);
            DatagramPacket answerPacket = new DatagramPacket(data, data.length, address, unicastPort);
            clientSocket.send(answerPacket);
            //System.out.println("answer sent");
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
