import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class KeepAlive implements Runnable {
    private DatagramSocket mysocket;
    private HashMap<Integer, InetSocketAddress> addresses;
    private int mytype;
    KeepAlive(DatagramSocket socket, HashMap<Integer, InetSocketAddress> adr, int type){
        mysocket = socket;
        addresses = adr;
        mytype = type;
    }
    @Override
    public void run() {
        DatagramPacket ka;
        byte[] buf_s = Chat.getBuf(mytype, "KeepAlive");
        long time = System.currentTimeMillis();
        while (true)
        {
            if(System.currentTimeMillis()-time>2000)//раз в 2 секунды
            {
                try
                {
                    synchronized (addresses)
                    {
                        for (Map.Entry<Integer, InetSocketAddress> entry : addresses.entrySet()) {
                            ka = new DatagramPacket(buf_s, buf_s.length, entry.getValue().getAddress(), entry.getValue().getPort());
                            mysocket.send(ka);
                        }
                    }
                }
                catch (IOException ex){
                    ex.printStackTrace();
                }
                time = System.currentTimeMillis();
            }
        }
    }
    public void putNewAddress(int number, InetSocketAddress adr)
    {
        synchronized (addresses)
        {
            addresses.put(number, adr);
        }
    }
    public void deleteAddres(int number, InetSocketAddress adr){
        synchronized (addresses)
        {
            addresses.remove(number, adr);
        }
    }
}
