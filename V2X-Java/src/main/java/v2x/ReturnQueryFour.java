package v2x;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.PrivateKey;
import java.util.Base64;

public class ReturnQueryFour extends Thread {
    private String inetAddress;
    private String time;
    private int unicastPort;
    private String answer;
    private String ownCertificateLocation;
    private String ownPrivateKeyLocation;
    private int number;
    private String dnsPrivateKeylocation;

    public ReturnQueryFour(String inetAddress, String time, int unicastPort, String answer,
                           String ownCertificateLocation, String ownPrivateKeyLocation,
                           int number, String dnsPrivateKeylocation) {
        this.inetAddress = inetAddress;
        this.time = time;
        this.unicastPort = unicastPort;
        this.answer = answer;
        this.ownCertificateLocation = ownCertificateLocation;
        this.ownPrivateKeyLocation = ownPrivateKeyLocation;
        this.number = number;
        this.dnsPrivateKeylocation = dnsPrivateKeylocation;
    }

    @Override
    public void run() {
        try {
            String userCertificate = AuthenticationFunctions
                    .getCertificate("Authentication/OBU-N-certificate" + number + ".crt");
            PrivateKey userPrivateKey = AuthenticationFunctions
                    .getPrivateKey("Authentication/OBU-N-private-key" + number + ".der");
            PrivateKey dnsPrivateKey = AuthenticationFunctions.getPrivateKey(dnsPrivateKeylocation);

            String innerAnswer = answer;

            Message innerMessage = new Message();
            innerMessage.putValue("Answer", innerAnswer);

            byte[] innerMessageByte = CommunicationFunctions.messageToByteArray(innerMessage);
            byte[] innerMessageByteBase64 = Base64.getEncoder().encode(innerMessageByte);
            String innerMessageString = new String(innerMessageByteBase64);

            String outerHash = AuthenticationFunctions.hashMessage(innerMessageString);
            String outerEncryptedHash = AuthenticationFunctions.encryptMessage(outerHash, userPrivateKey);

            Message outerMessage = new Message();
            outerMessage.putValue("Answer", innerMessageString);
            outerMessage.putValue("Hash", outerEncryptedHash);
            outerMessage.putValue("Certificate", userCertificate);
            outerMessage.putValue("Time", time);

            byte[] outerMessageByte = CommunicationFunctions.messageToByteArray(outerMessage);
            InetAddress address = InetAddress.getByName(inetAddress);
            DatagramPacket answerPacket = new DatagramPacket(outerMessageByte, outerMessageByte.length,
                    address, unicastPort);
            DatagramSocket clientSocket = new DatagramSocket();
            clientSocket.send(answerPacket);
            System.out.println("answer sent");
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
