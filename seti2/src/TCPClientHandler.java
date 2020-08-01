import java.io.*;
import java.net.Socket;

public class TCPClientHandler implements Runnable {
    private Socket client;
    private DataInputStream input;
    private DataOutputStream output;
    private long filesize;
    private DataOutputStream outputFile;
    private File file;
    private int averageCount = 0;
    private int count = 1, instantCount = 0;
    private long averageTime,allTime, allTimeEnd;
    TCPClientHandler(Socket socket) {
        client=socket;
    }
    @Override
    public void run(){
        try
        {
            input = new DataInputStream(client.getInputStream());
            output = new DataOutputStream(client.getOutputStream());
            makeFile();
            System.out.println("Start get file");
            getFile();
            System.out.println("Finish get file");
            sendAnswer();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally {
            close();
        }
    }
    private void makeFile() throws IOException
    {
        filesize = input.readLong();
        String filename = input.readUTF();
        File theDir = new File("uploads");
        if (!theDir.exists()) {
            try{
                theDir.mkdir();
            }
            catch(SecurityException se){
                System.out.println(se.getMessage());
            }
        }
        file = new File(theDir + "/" + filename);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void getFile() throws IOException
    {
        outputFile = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        byte[] buffer = new byte[8192];
        client.setSoTimeout(1000);
        averageTime = System.currentTimeMillis();
        allTime = System.currentTimeMillis();
        boolean showed = false;
        while (count > 0)
        {
            showed = false;
            try {
                count = input.read(buffer);
            }
            catch (Exception ex)
            {
                break;
            }
            if((System.currentTimeMillis()-averageTime) < 3000)
            {
                instantCount+=count;
                averageCount+=count;
            }
            else
            {
                printSpeed();
                showed = true;
            }
            outputFile.write(buffer, 0, count);
        }
        if(!showed)
            printSpeed();
    }
    private void sendAnswer() throws IOException
    {
        if(filesize==averageCount)
        {
            output.writeBoolean(true);
        }
        else output.writeBoolean(false);
    }
    private void close()
    {
        try {
            input.close();
            output.close();
            outputFile.close();
            client.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    private void printSpeed()
    {
        allTimeEnd = System.currentTimeMillis();
        System.out.println("Instant speed");
        System.out.println(instantCount/3 + " bytes per second");
        System.out.println("Average speed");
        double speed = (double)averageCount*1000/(double)(allTimeEnd-allTime);
        System.out.println(((int)speed+1) + " bytes per second");
        averageTime = System.currentTimeMillis();
        instantCount = 0;
    }
}
