package v2x;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ReturnQueryOne extends Thread{
    private String inetAddress;
    private String time;
    private int unicastPort;
    private String answer;

    public ReturnQueryOne(String inetAddress, String time, int unicastPort, String answer) {
        this.inetAddress = inetAddress;
        this.time = time;
        this.unicastPort = unicastPort;
        this.answer = answer;
    }

    @Override
    public void run() {
        try {
            InetAddress address = InetAddress.getByName(inetAddress);
            DatagramSocket clientSocket = new DatagramSocket();
            Message message = new Message();
            message.putValue("Answer", answer);
            message.putValue("Time", time);
            byte[] data = CommunicationFunctions.messageToByteArray(message);
            DatagramPacket answerPacket = new DatagramPacket(data, data.length, address, unicastPort);
            clientSocket.send(answerPacket);
            //System.out.println("answer sent");
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
