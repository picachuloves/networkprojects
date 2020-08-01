import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

public class Connection implements Runnable {
    private DatagramSocket mysocket;
    private HashMap<Integer, InetSocketAddress> adresses;
    private HashMap<Integer, String> names;
    private String myname;
    private int count;
    private int service = 20;
    Connection(DatagramSocket s, HashMap<Integer, InetSocketAddress> adr, HashMap<Integer, String> n, String name, boolean con)
    {
        mysocket = s;
        adresses = adr;
        names = n;
        myname = name;
        if(con)
            count = 1;
        else count = 0;
    }
    @Override
    public void run()
    {
        byte[] buf_r = new byte[service];
        DatagramPacket request = new DatagramPacket(buf_r, buf_r.length);

        byte[] buf_s = new byte[service];
        buf_s = myname.getBytes();

        while(true)
        {
            try
            {
                synchronized (mysocket)
                {
                    mysocket.receive(request);
                    DatagramPacket acc = new DatagramPacket(buf_s, buf_s.length, request.getAddress(), request.getPort());
                    mysocket.send(acc);
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
            count++;
            //adresses.put(number, new InetSocketAddress(request.getAddress(), request.getPort()));
            //names.put(number, Arrays.toString(request.getData()));
        }
    }
}
