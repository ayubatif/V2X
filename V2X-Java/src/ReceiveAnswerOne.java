import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Callable;

/**
 * Waits for an answer and returns it for the first test. Now built for timeouts
 */
class ReceiveAnswerOne implements Callable<Message> {
    static final int UNICAST_PORT = 2021;

    @Override
    public Message call() throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(UNICAST_PORT);
        byte[] buffer = new byte[65508];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(packet);
            Message message = CommunicationFunctions.byteArrayToMessage(buffer);
            String answer = message.getValue("Answer");
            if (!answer.equals(null)) {
                serverSocket.close();
                return message;
            }
        }
    }
}
