package v2x;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Vector;
import java.util.concurrent.Callable;

// https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

/**
 * Waits for an answer and returns it for the first test. Now built for timeouts
 */
class ReceiveAnswerOne extends Thread {
    private final DatagramSocket serverSocket;
    private AnswerCounter answerCounter;
    private ValidityCounter validityCounter;
    private Vector<String> messages;
    private int testAmount;

    public ReceiveAnswerOne(DatagramSocket serverSocket,
                            AnswerCounter answerCounter,
                            ValidityCounter validityCounter,
                            Vector<String> messages,
                            int testAmount) {
        this.serverSocket = serverSocket;
        this.answerCounter = answerCounter;
        this.validityCounter = validityCounter;
        this.messages = messages;
        this.testAmount = testAmount;
    }

    private String getMessageTime() {
        notify();
        String message = messages.firstElement();
        long timer = Long.parseLong(message);
        long time = System.currentTimeMillis();
        long result = time - timer;
        String totalTime = String.valueOf(result);
        return totalTime;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[65508];
        try {
            answerCounter.importJSONLog();
            validityCounter.importJSONLog();
        } catch (Exception e) {
            System.out.println("error one");
            System.out.println(e);
        }

        int counter = 0;
        boolean run = true;

        while (run) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                serverSocket.receive(packet);
                Message message = CommunicationFunctions.byteArrayToMessage(buffer);
                String answer = message.getValue("Answer");

//                System.out.println(getMessageTime());

                answerCounter.addAnswer(answer);
                validityCounter.addValidity("2");

                if (counter >= testAmount - 1) {
                    run = false;
                }

                counter++;

            } catch (Exception e) {
                System.out.println("error two");
                System.out.println(e);
            }
        }

        answerCounter.printAnswer();
        answerCounter.printMath();
        validityCounter.printValidity();
        validityCounter.printMath();

//        answerCounter.logAnswers();
//        validityCounter.logAnswers();
//        answerCounter.exportJSONLog();
//        validityCounter.exportJSONLog();
    }
}
