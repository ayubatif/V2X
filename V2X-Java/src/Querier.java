import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.CRL;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.concurrent.*;

public class Querier {
    static final int MULTICAST_PORT = 2020;
    static final int UNICAST_PORT = 2021;
    static final String OWN_CERTIFICATE_LOCATION = "../Authentication/OBU-A-certificate.crt";
    static final String CA_CERTIFICATE_LOCATION = "../Authentication/CA-certificate.crt";
    static final String OWN_PRIVATE_KEY_LOCATION = "../Authentication/OBU-A-private-key.der";
    static final String CRL_LOCATION = "../Authentication/CRL-A.crl";
    static final String OBU_X_CERTIFICATE_LOCATION = "../Authentication/OBU-X-certificate.crt";
    static final String DNS_CERTIFICATE_LOCATION = "../Authentication/DNS-certificate.crt";

    /**
     * Handles the initialization of the program to see which experiment it is running.
     *
     * @param args input from the command line when running the program
     */
    public static void main(String args[]) throws IOException, ClassNotFoundException, InterruptedException,
            NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException,
            NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException, CertificateException {
        int mode = Integer.parseInt(args[0]);
        int testAmount = Integer.parseInt(args[1]);
        switch (mode) {
            case 1:
                System.out.println("running test 1");
                runFirstTest(testAmount);
                break;
            case 2:
                System.out.println("running test 2");
                runSecondTest(testAmount);
                break;
            case 3:
                System.out.println("running test 3");
                runThirdTest(testAmount);
                break;
            case 0:
                System.out.println("running test 0");
                test(testAmount);
                break;
            case -1:
                System.out.println("running test -1");
                crlTest();
                break;
        }
    }

    // https://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
    // https://www.developer.com/java/data/how-to-multicast-using-java-sockets.html

    /**
     * Sends query message to the 2 OBUs for the first test.
     *
     * @throws IOException
     */
    private static void sendQueryTest1() throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress groupIP = InetAddress.getByName("225.0.0.0");
        multicastSocket.joinGroup(groupIP);
        Message query = new Message();
        query.putValue("Query", "Query");
        byte[] data = CommunicationFunctions.messageToByteArray(query);
        int randomPort = multicastSocket.getLocalPort();
        DatagramPacket queryPacket = new DatagramPacket(data, data.length, groupIP, randomPort);
        multicastSocket.send(queryPacket);
        System.out.println("query sent");
        multicastSocket.close();
    }

    /**
     * parses the message and returns the answer inside
     *
     * @param message A Message received by the OBU
     * @return <code>String</code> a string of the answer
     */
    private static String parseMessageTest1(Message message) {
        String answer = message.getValue("Answer");
        return answer;
    }

    // https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

    /**
     * Handles the first test.
     *
     * @param testAmount an integer specifying the amount of query to be sent
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    private static void runFirstTest(int testAmount) throws IOException, ClassNotFoundException, InterruptedException {
        int counter = 0;
        AnswerCounter answerCounter = new AnswerCounter();
        while (counter < testAmount) {
            sendQueryTest1();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Message> future = executorService.submit(new ReceiveAnswerOne());
            try {
                Message message = future.get(50, TimeUnit.MILLISECONDS);
                String answer = parseMessageTest1(message);
                answerCounter.addAnswer(answer);
                System.out.println(answer);
                counter++;
            } catch (Exception e) {
                System.out.println("timeout");
            }
            executorService.shutdownNow();
        }
        answerCounter.printAnswer();
    }

    /**
     * Sends query message, hash, and certificate to the 2 OBUs
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     */
    private static void sendQueryTest2()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        String userCertificate = AuthenticationFunctions.getCertificate(OWN_CERTIFICATE_LOCATION);
        PrivateKey userPrivateKey = AuthenticationFunctions.getPrivateKey(OWN_PRIVATE_KEY_LOCATION);
        String message = "Query";
        String hash = AuthenticationFunctions.hashMessage(message);
        String authentication = AuthenticationFunctions.encryptMessage(hash, userPrivateKey);
        MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress groupIP = InetAddress.getByName("225.0.0.0");
        multicastSocket.joinGroup(groupIP);
        Message query = new Message();
        query.putValue("Query", message);
        query.putValue("Certificate", userCertificate);
        query.putValue("Hash", authentication);
        byte[] data = CommunicationFunctions.messageToByteArray(query);
        int randomPort = multicastSocket.getLocalPort();
        DatagramPacket queryPacket = new DatagramPacket(data, data.length, groupIP, randomPort);
        multicastSocket.send(queryPacket);
        System.out.println("query sent");
        multicastSocket.close();
    }

    // https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

    /**
     * Handles the second test.
     *
     * @param testAmount an integer specifying the amount of query to be sent
     * @throws IOException
     * @throws InterruptedException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws ClassNotFoundException
     * @throws CertificateException
     */
    private static void runSecondTest(int testAmount)
            throws IOException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException,
            InvalidKeySpecException {
        int counter = 0;
        AnswerCounter answerCounter = new AnswerCounter();
        new PrintWriter(CRL_LOCATION).close(); // empty the file
        String blacklistCertifiate = AuthenticationFunctions.getCertificate(OBU_X_CERTIFICATE_LOCATION);
        AuthenticationFunctions.addToCRL(blacklistCertifiate, CRL_LOCATION);
        while (counter < testAmount) {
            sendQueryTest2();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<String> future = executorService.submit(new ReceiveAnswerTwo());
            try {
                String answer = future.get(100, TimeUnit.MILLISECONDS);
                answerCounter.addAnswer(answer);
                System.out.println(answer);
                counter++;
            } catch (Exception e) {
                System.out.println("timeout");
            }
            executorService.shutdownNow();
        }
        answerCounter.printAnswer();
    }

    /**
     * Sends query message, hash, and certificate to the 2 OBUs. Same as the second one.
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     */
    private static void sendQueryTest3()
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        String userCertificate = AuthenticationFunctions.getCertificate(OWN_CERTIFICATE_LOCATION);
        PrivateKey userPrivateKey = AuthenticationFunctions.getPrivateKey(OWN_PRIVATE_KEY_LOCATION);
        String message = "Query";
        String hash = AuthenticationFunctions.hashMessage(message);
        String authentication = AuthenticationFunctions.encryptMessage(hash, userPrivateKey);
        MulticastSocket multicastSocket = new MulticastSocket(MULTICAST_PORT);
        InetAddress groupIP = InetAddress.getByName("225.0.0.0");
        multicastSocket.joinGroup(groupIP);
        Message query = new Message();
        query.putValue("Query", message);
        query.putValue("Certificate", userCertificate);
        query.putValue("Hash", authentication);
        byte[] data = CommunicationFunctions.messageToByteArray(query);
        int randomPort = multicastSocket.getLocalPort();
        DatagramPacket queryPacket = new DatagramPacket(data, data.length, groupIP, randomPort);
        multicastSocket.send(queryPacket);
        System.out.println("query sent");
        multicastSocket.close();
    }

    // https://stackoverflow.com/questions/2275443/how-to-timeout-a-thread

    /**
     * Handles the third test.
     *
     * @param testAmount an integer specifying the amount of query to be sent
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidKeySpecException
     * @throws InterruptedException
     */
    private static void runThirdTest(int testAmount)
            throws IOException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException,
            InvalidKeySpecException, InterruptedException {
        int counter = 0;
        AnswerCounter answerCounter = new AnswerCounter();
        new PrintWriter(CRL_LOCATION).close(); // empty the file
        while (counter < testAmount) {
            sendQueryTest3();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            DatagramSocket serverSocket = new DatagramSocket(UNICAST_PORT);
            Future<String> future = executorService.submit(new ReceiveAnswerThree(serverSocket));
            try {
                String answer = future.get(200, TimeUnit.MILLISECONDS);
                answerCounter.addAnswer(answer);
                System.out.println("answer");
                System.out.println(answer);
                counter++;
            } catch (Exception e) {
                System.out.println("timeout");
                serverSocket.close();
                System.out.println(e);
            }
            executorService.shutdownNow();
            Thread.sleep(1000);
        }
        answerCounter.printAnswer();
    }

    private static void test(int testAmount) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            CertificateException, ClassNotFoundException {
    }

    // test a certificate file for revocation, then test adding a certificate to CRL file
    private static void crlTest() throws IOException {
        new PrintWriter(CRL_LOCATION).close(); // empty the file
        String n_certificate = AuthenticationFunctions.getCertificate("../Authentication/OBU-N-certificate.crt");
        String x_certificate = AuthenticationFunctions.getCertificate("../Authentication/OBU-X-certificate.crt");

        if (AuthenticationFunctions.checkRevocatedCertificate(n_certificate, CRL_LOCATION) == false) {
            if (AuthenticationFunctions.checkRevocatedCertificate(x_certificate, CRL_LOCATION) == false) {
                AuthenticationFunctions.addToCRL(x_certificate, CRL_LOCATION);
                if (AuthenticationFunctions.checkRevocatedCertificate(x_certificate, CRL_LOCATION) == true) {
                    System.out.println("it seems the revocation list worked..");
                    return;
                }
            }
        }
        System.out.println("it seems the revocation list did not work..");
    }
}