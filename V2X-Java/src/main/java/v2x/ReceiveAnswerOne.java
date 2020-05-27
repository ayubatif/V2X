package v2x;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
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
    private int counter;
    private ThreadCommunication threadCommunication;

    public ReceiveAnswerOne(DatagramSocket serverSocket,
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
        long TPRStart;
        long TPREnd;

        while (run) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                serverSocket.receive(packet);
                TPRStart = System.currentTimeMillis();
                Message message = CommunicationFunctions.byteArrayToMessage(buffer);
                String answer = message.getValue("Answer");


                if (answer.equals("0")) {
                    long endTime = System.currentTimeMillis();
                    String time = message.getValue("Time");
                    long startTime = Long.parseLong(time);
                    long totalTime = endTime - startTime;

//                    System.out.println("start time" + startTime);
//                    System.out.println("end time" + endTime);
                    //System.out.println("total time " + totalTime);
                    timeCounter.addTimeToQueryResolve(totalTime);
                    timeCounter.addTimeToRawTQRData(totalTime);

                    TPREnd = System.currentTimeMillis();
                    timeCounter.addTimeToProcessResponse(TPREnd - TPRStart);
                    timeCounter.addTimeToRawTPRData(TPREnd - TPRStart);
                }

                answerCounter.addAnswer(answer);
                validityCounter.addValidity("2");

                counter++;
                run = false;
                serverSocket.close();
                threadCommunication.setReady(true);
            } catch (SocketException e) {
                //System.out.println("Thread ended");
                run = false;
            } catch (Exception e) {
                //System.out.println("error two");
                e.printStackTrace();
            }
        }
    }
}
