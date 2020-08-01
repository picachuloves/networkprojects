import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class ReceiveHandler extends  UDPChat implements Runnable {
    @Override
    public void run() {
        DatagramSocket socket = getSocket();
        try {
            byte[] buf_r = new byte[getBufsize()];
            DatagramPacket receivemess = new DatagramPacket(buf_r, buf_r.length);
            socket.setSoTimeout(0);
            while (true) {
                socket.receive(receivemess);
                if (!isLost()) {
                    int type = getMessageType(receivemess.getData());
                    UUID id = getIDFromMessage(receivemess.getData());
                    //System.out.println("Got "+type+" "+new Date());
                    if (type == request) {
                        confirm(receivemess);
                        if(!Messages.contains(id)) {
                            makeNewNeighbour(receivemess);
                            Messages.putMessage(id);
                        }
                    }
                    if (type == reqack) {
                        confirm(receivemess);
                        if(!Messages.contains(id)) {
                            confirmNeighbour(receivemess);
                            Messages.putMessage(id);
                        }
                    }
                    if (type == messack) {
                        Acknowledgments.gotAcknowledgment(id);
                    }
                    if(Addresses.getID(new InetSocketAddress(receivemess.getAddress(), receivemess.getPort()))!=null) {
                        if (type == keepalive) {
                            //confirm(receivemess);
                            if(Addresses.getID(new InetSocketAddress(receivemess.getAddress(), receivemess.getPort()))!=null)
                            {
                                Times.putTime(Addresses.getID(new InetSocketAddress(receivemess.getAddress(), receivemess.getPort())), System.currentTimeMillis());
                            }
                        }
                        if (type == message) {
                            confirm(receivemess);
                            if (!Messages.contains(id)) {
                                handleMessage(receivemess);
                                Messages.putMessage(id);
                            }
                        }
                        if (type == depute) {
                            confirm(receivemess);
                            if (!Messages.contains(id)) {
                                Deputies.putDepute(Addresses.getID(new InetSocketAddress(receivemess.getAddress(), receivemess.getPort())), getAddress(receivemess));
                                Messages.putMessage(id);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void makeNewNeighbour(DatagramPacket packet) throws UnknownHostException {
        //System.out.println("GOT REQUEST");
        UUID id = getID();
        Addresses.putAddress(id, new InetSocketAddress(packet.getAddress(), packet.getPort()));
        Names.putName(id, getMessage(packet.getData()));
        Sender sender = new Sender();

        sender.putMessage(reqack, getIDFromMessage(packet.getData()), getMyName());
        sender.sendEveryone(false);
        sender.setAddress(new InetSocketAddress(packet.getAddress(), packet.getPort()));
        new Thread(sender).start();
       // System.out.println("SENT CONF REQUEST");
        System.out.println(getMessage(packet.getData())+ " CONNECTED");
        if(!haveDepute())//если замены нет , подключившийся-замена, рассылаем его всем кто уже подключен
        {
            setDepute(new InetSocketAddress(packet.getAddress(), packet.getPort()), getMessage(packet.getData()));
        }
        else
        {
            sendDepute(false, new InetSocketAddress(packet.getAddress(), packet.getPort()));
        }
    }
    private String getName(DatagramPacket packet){
        String[] data = getMessage(packet.getData()).split(";");
        return data[1];
    }
    private void confirmNeighbour(DatagramPacket packet)  throws UnknownHostException{
        UUID id = getID();
        Addresses.putAddress(id, new InetSocketAddress(packet.getAddress(), packet.getPort()));
        Names.putName(id, getMessage(packet.getData()));
        System.out.println("CONNECTED TO "+getMessage(packet.getData()));
        //сообщаем о замене если есть
        if(haveDepute())
        {
            sendDepute(false, new InetSocketAddress(packet.getAddress(), packet.getPort()));
        }
        //System.out.println("CONF REQUEST");
    }
    private void handleMessage(DatagramPacket packet)throws UnknownHostException {
        UUID id = getIDFromMessage(packet.getData());
        Messages.putMessage(id);

        String data = getMessage(packet.getData());
        System.out.println(data);

        Sender sender = new Sender();
        sender.putMessage(message, id,getMessage(packet.getData()));
        sender.sendEveryone(true);
        sender.setAddress(new InetSocketAddress(packet.getAddress(), packet.getPort()));
        new Thread(sender).start();
    }
    private InetSocketAddress getAddress(DatagramPacket packet)throws UnknownHostException{
        String[] data = getMessage(packet.getData()).split(";");
        String[] firstaddress = data[0].split("/");
        String[] address = firstaddress[1].split(":");
        String port = address[1].replaceAll("[^0-9]","");
        InetSocketAddress result = new InetSocketAddress(InetAddress.getByName(address[0]), Integer.parseInt(port));
        return result;
    }

    private boolean isLost() {
        boolean result = false;
        Random random = new Random();
        if (random.nextInt(100) < getMyLosses())
            result = true;
        return result;
    }
    private void confirm(DatagramPacket packet){
        Sender sender = new Sender();
        sender.putMessage(messack, getIDFromMessage(packet.getData()), "Delivered");
        sender.sendEveryone(false);
        sender.setAddress(new InetSocketAddress(packet.getAddress(), packet.getPort()));
        new Thread(sender).start();
    }

}
