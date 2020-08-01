import java.io.IOException;
import java.net.ServerSocket;

public class TCPServer {
    private static ServerSocket serverSocket;
    private static TCPClientHandler clientHandler;
    public static void main(String[] args)
    {
        try {
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            System.out.println("Server start");
            while (true)
            {
                clientHandler = new TCPClientHandler(serverSocket.accept());
                System.out.println("A new client is connected");
                new Thread(clientHandler).start();
            }
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}


