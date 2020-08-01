import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sender extends UDPChat implements Runnable  {
    private byte[] buffer;
    private UUID id;
    private boolean sendType;
    private InetSocketAddress address = null;
    private DatagramSocket socket = getSocket();
    private int type;
    @Override
    public void run() {//не ждать подтверждения, фикс количество потоков
       // synchronized (Sender.class) {
            if (sendType) {
                HashMap<UUID, InetSocketAddress> addresses = Addresses.getAddresses();
                for (Map.Entry<UUID, InetSocketAddress> entry : addresses.entrySet()) {
                    InetSocketAddress addr = entry.getValue();
                    if (!addr.equals(address)) {
                        sendToOne(addr);
                    }
                }
            } else {
                sendToOne(address);
            }
      //  }

    }

    public void putMessage(int type, UUID id,String data) {
        buffer = getBuffer(type, id, data);
        this.type = type;
        this.id = id;
    }
    public void sendEveryone(boolean bool){//true-send to everyone except address, false-send to address
        sendType = bool;
    }
    public void setAddress(InetSocketAddress address){
        this.address = address;
    }
    private void sendToOne(InetSocketAddress address) {
        DatagramPacket message = new DatagramPacket(buffer, buffer.length, address.getAddress(), address.getPort());
        if(type!=messack && type!=keepalive) {
            Acknowledgments.makeAcknowledgment(id);
            try {
                do {
                    try {
                        socket.send(message);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } while (!Acknowledgments.getAcknowledgment(id));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            Acknowledgments.deleteAcknowledgment(id);
        }
        else
        {
            try {
                socket.send(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
