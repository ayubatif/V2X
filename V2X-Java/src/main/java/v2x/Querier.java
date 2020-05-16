package v2x;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Querier {
    static final int MULTICAST_PORT = 2020;
    static final int UNICAST_PORT = 2021;
    static final String OWN_CERTIFICATE_LOCATION = "Authentication/OBU-A-certificate0.crt";
    static final String CA_CERTIFICATE_LOCATION = "Authentication/CA-certificate.crt";
    static final String OWN_PRIVATE_KEY_LOCATION = "Authentication/OBU-A-private-key0.der";
    static final String CRL_LOCATION = "Authentication/CRL-A.crl";
    static final String OBU_X_CERTIFICATE_LOCATION = "Authentication/OBU-X-certificate.crt";
    static final String DNS_CERTIFICATE_LOCATION = "Authentication/DNS-certificate.crt";
    static final String BLOOM_FILTER_LOCATION = "Authentication/DNS-bloom-filter.bf";

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
        System.out.println(System.getProperty("user.dir"));
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
                runThirdTest(testAmount, Integer.parseInt(args[2]));
                break;
            case 4:
                System.out.println("running test 4");
                runFourthTest(testAmount, Integer.parseInt(args[2]));
                break;
            case 0:
                System.out.println("running test 0");
                test(testAmount);
                break;
            case -1:
                System.out.println("running test -1");
                crlTest();
                break;
            case -2:
                System.out.println("running test -2");
                bloomFilterTest();
                break;
            case -3:
                System.out.println(System.getProperty("user.dir"));
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
        AnswerCounter answerCounter = new AnswerCounter(1);
        ValidityCounter validityCounter = new ValidityCounter(1);
        answerCounter.importJSONLog();
        validityCounter.importJSONLog();
        while (counter < testAmount) {
            sendQueryTest1();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Message> future = executorService.submit(new ReceiveAnswerOne());
            try {
                Message message = future.get(50, TimeUnit.MILLISECONDS);
                String answer = parseMessageTest1(message);
                answerCounter.addAnswer(answer);
                validityCounter.addValidity("2");
                System.out.println(answer);
                counter++;
            } catch (Exception e) {
                System.out.println("timeout");
            }
            executorService.shutdownNow();
        }
        answerCounter.printAnswer();
        answerCounter.printMath();
        validityCounter.printValidity();
        validityCounter.printMath();
        answerCounter.logAnswers();
        validityCounter.logAnswers();
        answerCounter.exportJSONLog();
        validityCounter.exportJSONLog();
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
        AnswerCounter answerCounter = new AnswerCounter(2);
        ValidityCounter validityCounter = new ValidityCounter(2);
        answerCounter.importJSONLog();
        validityCounter.importJSONLog();
        new PrintWriter(CRL_LOCATION).close(); // empty the file
        String blacklistCertifiate = AuthenticationFunctions.getCertificate(OBU_X_CERTIFICATE_LOCATION);
        AuthenticationFunctions.addToCRL(blacklistCertifiate, CRL_LOCATION);
        while (counter < testAmount) {
            sendQueryTest2();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            DatagramSocket serverSocket = new DatagramSocket(UNICAST_PORT);
            Future<String[]> future = executorService.submit(new ReceiveAnswerTwo(serverSocket));
            try {
                String[] answer = future.get(1000, TimeUnit.MILLISECONDS);
                answerCounter.addAnswer(answer[0]);
                for (int i = 0; i < answer.length; i++) {
                    if (answer[i].equals("-2")) {
                        break;
                    }
                    validityCounter.addValidity(answer[i]);
                }
                System.out.println(answer[0]);
                counter++;
                serverSocket.close();
            } catch (Exception e) {
                System.out.println("timeout");
                serverSocket.close();
            }
            executorService.shutdownNow();
        }
        answerCounter.printAnswer();
        answerCounter.printMath();
        validityCounter.printValidity();
        validityCounter.printMath();
        answerCounter.logAnswers();
        validityCounter.logAnswers();
        answerCounter.exportJSONLog();
        validityCounter.exportJSONLog();
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
    private static void runThirdTest(int testAmount, int rate)
            throws IOException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException,
            InvalidKeySpecException, InterruptedException {
        int counter = 0;
        AnswerCounter answerCounter = new AnswerCounter(3, rate);
        ValidityCounter validityCounter = new ValidityCounter(3, rate);
        answerCounter.importJSONLog();
        validityCounter.importJSONLog();
        new PrintWriter(CRL_LOCATION).close(); // empty the file
        while (counter < testAmount) {
            sendQueryTest3();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            DatagramSocket serverSocket = new DatagramSocket(UNICAST_PORT);
            Future<String[]> future = executorService.submit(new ReceiveAnswerThree(serverSocket));
            try {
                String[] answer = future.get(1000, TimeUnit.MILLISECONDS);
                answerCounter.addAnswer(answer[0]);
                for (int i = 0; i < answer.length; i++) {
                    if (answer[i].equals("-2")) {
                        break;
                    }
                    validityCounter.addValidity(answer[i]);
                }
                System.out.println("answer");
                System.out.println(answer[0]);
                counter++;
                serverSocket.close();
            } catch (Exception e) {
                System.out.println("timeout");
                serverSocket.close();
                System.out.println(e);
            }
            executorService.shutdownNow();
            Thread.sleep(1000);
        }
        answerCounter.printAnswer();
        answerCounter.printMath();
        validityCounter.printValidity();
        validityCounter.printMath();
        answerCounter.logAnswers();
        validityCounter.logAnswers();
        answerCounter.exportJSONLog();
        validityCounter.exportJSONLog();
    }

    /**
     * Sends query message, hash, and certificate to the 2 OBUs. Same as the second & third ones.
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     */
    private static void sendQueryTest4()
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

    /**
     * Handles the fourth test
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
    private static void runFourthTest(int testAmount, int rate)
            throws IOException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidKeyException,
            InvalidKeySpecException, InterruptedException {
        int counter = 0;
        AnswerCounter answerCounter = new AnswerCounter(4, rate);
        ValidityCounter validityCounter = new ValidityCounter(4, rate);
        answerCounter.importJSONLog();
        validityCounter.importJSONLog();
        new PrintWriter(CRL_LOCATION).close(); // empty the file
        DNSBloomFilter dnsBloomFilter = DNSBloomFilterFunctions.generateRandomBloomFilter(1000);
        while (counter < testAmount) {
            sendQueryTest4();
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            DatagramSocket serverSocket = new DatagramSocket(UNICAST_PORT);
            Future<String[]> future = executorService.submit(new ReceiveAnswerFour(serverSocket));
            try {
                String[] answer = future.get(1000, TimeUnit.MILLISECONDS);
                answerCounter.addAnswer(answer[0]);
                for (int i = 0; i < answer.length; i++) {
                    if (answer[i].equals("-2")) {
                        break;
                    }
                    validityCounter.addValidity(answer[i]);
                }
                System.out.println("answer");
                System.out.println(answer[0]);
                counter++;
                serverSocket.close();
            } catch (Exception e) {
                System.out.println("timeout");
                serverSocket.close();
                System.out.println(e);
            }
            executorService.shutdownNow();
            Thread.sleep(1000);
        }
        answerCounter.printAnswer();
        answerCounter.printMath();
        validityCounter.printValidity();
        validityCounter.printMath();
        answerCounter.logAnswers();
        validityCounter.logAnswers();
        answerCounter.exportJSONLog();
        validityCounter.exportJSONLog();
    }

    private static void test(int testAmount) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException,
            IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            CertificateException, ClassNotFoundException {
        DNSBloomFilter signedIPsRSU = DNSBloomFilterFunctions.generateRandomBloomFilter(10); // BF created by C&C
        signedIPsRSU.exportBloomFilter(BLOOM_FILTER_LOCATION);
        DNSBloomFilter signedIPsOBU = AuthenticationFunctions.getBloomFilter(BLOOM_FILTER_LOCATION); // BF obtained for test
        if (!AuthenticationFunctions.checkSignedAAAARecord("KTH.Thesis.V2X", signedIPsOBU)) {
            System.out.println("it seems the bloom filter worked..2");
            if (!AuthenticationFunctions.checkSignedAAAARecord("0000:1111:2222:3333:4444:5555:6666:7777", signedIPsOBU)) {
                System.out.println("it seems the bloom filter worked..1");
                if (AuthenticationFunctions.checkSignedAAAARecord("KTH.Thesis.V2X=0000:1111:2222:3333:4444:5555:6666:7777", signedIPsOBU)) {
                    System.out.println("it seems the bloom filter worked..0");
                    return;
                }
            }
        }
        System.out.println("it seems the bloom filter did not work..");
    }

    // test a certificate file for revocation, then test adding a certificate to CRL file
    private static void crlTest() throws IOException {
        String n_certificate = AuthenticationFunctions.getCertificate("Authentication/OBU-N-certificate.crt");
        String x_certificate = AuthenticationFunctions.getCertificate("Authentication/OBU-X-certificate.crt");
        new PrintWriter(CRL_LOCATION).close(); // empty the file
        if (!AuthenticationFunctions.checkRevocatedCertificate(n_certificate, CRL_LOCATION)) {
            System.out.println("it seems the bloom filter worked..2");
            if (!AuthenticationFunctions.checkRevocatedCertificate(x_certificate, CRL_LOCATION)) {
                System.out.println("it seems the bloom filter worked..1");

                AuthenticationFunctions.addToCRL(x_certificate, CRL_LOCATION);

                if (AuthenticationFunctions.checkRevocatedCertificate(x_certificate, CRL_LOCATION)) {
                    System.out.println("it seems the revocation list worked..0.5");
                }

                if (!AuthenticationFunctions.checkRevocatedCertificate(x_certificate, CRL_LOCATION)) {
                    System.out.println("0.5 not working");
                }

                AuthenticationFunctions.addToCRL(n_certificate, CRL_LOCATION);

                if (AuthenticationFunctions.checkRevocatedCertificate(n_certificate, CRL_LOCATION)) {
                    System.out.println("it seems the revocation list worked..0.25");
                }

                if (!AuthenticationFunctions.checkRevocatedCertificate(x_certificate, CRL_LOCATION)) {
                    System.out.println("0.25 not working");
                }

                if (AuthenticationFunctions.checkRevocatedCertificate(x_certificate, CRL_LOCATION)) {
                    System.out.println("it seems the revocation list worked..0.125");
                }

                if (!AuthenticationFunctions.checkRevocatedCertificate(x_certificate, CRL_LOCATION)) {
                    System.out.println("0.125 not working");
                }

                if (AuthenticationFunctions.checkRevocatedCertificate(x_certificate, CRL_LOCATION) && AuthenticationFunctions.checkRevocatedCertificate(n_certificate, CRL_LOCATION)) {
                    System.out.println("it seems the revocation list worked..0");
                }

                if (AuthenticationFunctions.checkRevocatedCertificate(x_certificate + n_certificate, CRL_LOCATION)) {
                    System.out.println("Y'all have a good one");
                }
            }
        }
        System.out.println("it seems the revocation list did not work..");
    }

    // test a bloom filter with one entry against 2 missing records and the one exisiting record
    private static void bloomFilterTest() throws IOException {
        DNSBloomFilter signedIPsRSU = new DNSBloomFilter(DNSBloomFilter.NUM_AAAA_RECORDS); // BF created by C&C
        signedIPsRSU.add(DNSBloomFilter.exampleAAAA);
        signedIPsRSU.exportBloomFilter(BLOOM_FILTER_LOCATION);
        DNSBloomFilter signedIPsOBU = AuthenticationFunctions.getBloomFilter(BLOOM_FILTER_LOCATION); // BF obtained for test
        if (!AuthenticationFunctions.checkSignedAAAARecord(DNSBloomFilter.exampleHostname, signedIPsOBU)) {
            System.out.println("it seems the bloom filter worked..2");
            if (!AuthenticationFunctions.checkSignedAAAARecord(DNSBloomFilter.exampleIPv6Addr, signedIPsOBU)) {
                System.out.println("it seems the bloom filter worked..1");
                if (AuthenticationFunctions.checkSignedAAAARecord(DNSBloomFilter.exampleAAAA, signedIPsOBU)) {
                    System.out.println("it seems the bloom filter worked..0");
                    return;
                }
            }
        }
        System.out.println("it seems the bloom filter did not work..");
    }
}