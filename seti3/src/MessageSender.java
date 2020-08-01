import javax.swing.event.ChangeListener;
import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class MessageSender implements Runnable {
    private DatagramSocket mysocket;
    private String myname;
    private HashMap<Integer, InetSocketAddress> addresses;
    private int mytype;
    MessageSender(DatagramSocket socket, String name, HashMap<Integer, InetSocketAddress> adr, int type){
        mysocket = socket;
        myname = name;
        addresses = adr;
        mytype = type;
    }
    @Override
    public void run() {
        DatagramPacket mess;
        try
        {
            while(true) {
                System.out.print(myname+": ");
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(System.in));
                String message = reader.readLine();
                byte[] buf_acc = Chat.getBuf(3, "Acc");
                DatagramPacket acc = new DatagramPacket(buf_acc, buf_acc.length);
                synchronized (addresses)
                {
                    for (Map.Entry<Integer, InetSocketAddress> entry : addresses.entrySet()) {
                        boolean got_acc = false;
                        while (!got_acc) {
                            byte[] buf_s = Chat.getBuf(mytype, message);
                            InetSocketAddress address = entry.getValue();
                            mess = new DatagramPacket(buf_s, buf_s.length, address.getAddress(), address.getPort());
                            mysocket.send(mess);
                            for(int i = 0; i < 10 && !got_acc;i++) {
                                try {
                                    mysocket.receive(acc);
                                } catch (SocketTimeoutException ex) {
                                    continue;
                                }
                                int got = Chat.getMessageType(acc.getData());
                                if (got == 4) {
                                    got_acc = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
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
