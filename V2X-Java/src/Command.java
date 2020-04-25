import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Command {
    static final int PORT = 2020;

    public static void main(String args[]) throws IOException {
        int bool = 1;
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket clientSocket = serverSocket.accept();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        while (true)
        {
            String output = bufferedReader.readLine();
            if (output != null)
            {
                System.out.println(output);
            }
        }
    }
}
