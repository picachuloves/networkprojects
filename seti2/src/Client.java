import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    private static File file;
    private static String filepath;
    private static String hostname;
    private static int port;
    private static Socket socket;
    private static long filesize;
    private static DataOutputStream output;
    private static DataInputStream inputFile;
    private static DataInputStream input;
    public static void main(String[] args) {
        filepath = args[0];
        hostname = args[1];
        port = Integer.parseInt(args[2]);
        try
        {
           socket  = new Socket(InetAddress.getByName(hostname), port);
           System.out.println("Start send file");
           sendFile();
           System.out.println("Finish send file");
           getAnswer();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally {
            close();
        }
    }
    private static void getFileSize() throws FileNotFoundException
    {
        if(file.exists()){
            filesize = file.length();
        }
        else throw new FileNotFoundException();
    }
    private static void sendFile() throws IOException
    {
        byte[] buffer = new byte[8192];
        int count;
        output = new DataOutputStream(socket.getOutputStream());
        file = new File(filepath);
        inputFile = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        getFileSize();
        output.writeLong(filesize);
        output.writeUTF(file.getName());
        while ((count = inputFile.read(buffer)) > 0)
        {
            output.write(buffer, 0, count);
        }
        output.flush();
    }
    private static void getAnswer() throws IOException{
        input = new DataInputStream(socket.getInputStream());
        if(input.readBoolean())
            System.out.println("Sending finished successful");
        else System.out.println("Sending finished with failure");
    }
    private static void close()
    {
        try {
            input.close();
            output.close();
            inputFile.close();
            socket.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
