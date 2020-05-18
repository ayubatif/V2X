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
    private TimeCounter timeCounter;
    private int testAmount;
    private ThreadCommunication threadCommunication;

    public ReceiveAnswerOne(DatagramSocket serverSocket,
                            AnswerCounter answerCounter,
                            ValidityCounter validityCounter,
                            TimeCounter timeCounter, int testAmount,
                            ThreadCommunication threadCommunication) {
        this.serverSocket = serverSocket;
        this.answerCounter = answerCounter;
        this.validityCounter = validityCounter;
        this.timeCounter = timeCounter;
        this.testAmount = testAmount;
        this.threadCommunication = threadCommunication;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[65508];
        try {
            answerCounter.importJSONLog();
            validityCounter.importJSONLog();
        } catch (Exception e) {
            System.out.println("error one");
            e.printStackTrace();
        }

        int counter = 0;
        boolean run = true;

        while (run) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                serverSocket.receive(packet);
                Message message = CommunicationFunctions.byteArrayToMessage(buffer);
                String answer = message.getValue("Answer");


                if (answer.equals("0")) {
                    long endTime = System.currentTimeMillis();
                    String time = message.getValue("Time");
                    long startTime = Long.parseLong(time);
                    long totalTime = endTime - startTime;

//                    System.out.println("start time" + startTime);
//                    System.out.println("end time" + endTime);
                    System.out.println("total time" + totalTime);
                    timeCounter.addTime(totalTime);
                }

                answerCounter.addAnswer(answer);
                validityCounter.addValidity("2");


//                if (counter >= testAmount - 1) {
//                    run = false;
//                }

                counter++;
//                buffer = new byte[65508];
                run = false;
                serverSocket.close();
                threadCommunication.setReady(true);
            } catch (Exception e) {
                System.out.println("error two");
                e.printStackTrace();
            }
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
//
//        serverSocket.close();
    }
}
