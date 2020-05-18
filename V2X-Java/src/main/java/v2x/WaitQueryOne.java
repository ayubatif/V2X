package v2x;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class WaitQueryOne extends Thread{
    private MulticastSocket serverSocket;
    private int unicastPort;
    private String answer;

    public WaitQueryOne(MulticastSocket serverSocket, int unicastPort, String answer) {
        this.serverSocket = serverSocket;
        this.unicastPort = unicastPort;
        this.answer = answer;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                Message message = CommunicationFunctions.byteArrayToMessage(buffer);
                String request = message.getValue("Query");
                if (request.equals("Query")) {
                    System.out.println("query received");
                    String inetAddress = packet.getAddress().getHostAddress();
                    String time = message.getValue("Time");
                    ReturnQueryOne returnQueryOne = new ReturnQueryOne(inetAddress, time, unicastPort, answer);
                    returnQueryOne.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
