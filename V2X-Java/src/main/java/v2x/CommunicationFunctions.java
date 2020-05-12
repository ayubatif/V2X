package v2x;

import java.io.*;

public class CommunicationFunctions {
    /**
     * Takes in message to turn into a byte array for sending through the network.
     *
     * @param message a message with keys and values inside
     * @return <code>byte[]</code> a byte array of the converted message
     * @throws IOException
     */
    public static byte[] messageToByteArray(Message message) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();
        byte[] data = byteArrayOutputStream.toByteArray();
        return data;
    }

    /**
     * Takes in a byte array and turns it into a message after it is arrives from the network.
     *
     * @param buffer a byte array to be turned into a message
     * @return <code>Message</code> a message with keys and values inside
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Message byteArrayToMessage(byte[] buffer) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        ObjectInput objectInput = new ObjectInputStream(byteArrayInputStream);
        Message message = (Message) objectInput.readObject();
        return message;
    }
}
