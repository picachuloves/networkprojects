import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class CopiesFinder {
    private byte[] buf_s = new byte[10];
    private byte[] buf_r = new byte[10];
    private MulticastSocket socket;
    private DatagramPacket mes_s, mes_r;
    private HashMap<Long, InetAddress> data = new HashMap<>();
    private HashMap<Long, Long> exist = new HashMap<>();
    private long id;
    CopiesFinder(String addres, int PORT) throws IOException {
        socket = new MulticastSocket(PORT);
        InetAddress group = InetAddress.getByName(addres);
        socket.joinGroup(group);
        socket.setSoTimeout(1000);
        buf_s = ByteBuffer.allocate(8).putLong(getID()).array();
        mes_s = new DatagramPacket(buf_s, buf_s.length, InetAddress.getByName(addres), PORT);
        mes_r = new DatagramPacket(buf_r, buf_r.length);
    }
    public void findCopies() throws IOException {
        while(true)
        {
            socket.send(mes_s);
            long last_time = System.currentTimeMillis();
            while(System.currentTimeMillis()-last_time < 1000)
            {
                try
                {
                    socket.receive(mes_r);
                }
                catch (SocketTimeoutException ex)
                {
                    continue;
                }
                last_time = System.currentTimeMillis();
                long id_r = convertToLong(mes_r.getData());
                if(data.put(id_r, mes_r.getAddress())==null)
                {
                    exist.put(id_r, last_time);
                    System.out.println("************************");
                    System.out.println("Got a new copy");
                    print();
                }
                else {
                    exist.put(id_r, last_time);
                }
                boolean lost = false;
                Iterator<Map.Entry<Long, Long>> entryIt = exist.entrySet().iterator();
                while(entryIt.hasNext()){
                    Map.Entry<Long, Long> entry = entryIt.next();
                    if(System.currentTimeMillis() - entry.getValue() > 5000){
                        entryIt.remove();
                        data.remove(entry.getKey(), data.get(entry.getKey()));
                        lost = true;
                    }
                }
                if(lost) {
                    System.out.println("************************");
                    System.out.println("We lost somebody");
                    print();
                }
            }
        }
    }
    private long getID() {
        Random random = new Random();
        return id = System.currentTimeMillis()*random.nextInt();
    }
    private long convertToLong(byte[] array) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        return buffer.getLong();
    }
    private void print(){
        System.out.println("Found IP:");
        for (Map.Entry<Long, InetAddress> entry: data.entrySet()) {
            System.out.println(entry.getValue());
        }
    }
}
