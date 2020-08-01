import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Chat {
    private static String name;
    private static int losses;
    private static int myport;
    private static boolean connected = false;
    private static InetAddress hostname;
    private static int port;
    private static DatagramSocket socket;
    private static HashMap<Integer, InetSocketAddress> neighbours = new HashMap<>();
    private static HashMap<Integer, String> names = new HashMap<>();
    private static int count = 0;
    private static HashMap<Integer, Long> isalive = new HashMap<>();
    private static HashMap<Integer, InetSocketAddress> deputies = new HashMap<>();
    private static HashMap<Integer, String> depnames = new HashMap<>();
    private static InetSocketAddress mydeputeadr;
    private static String mydeputename;
    private static boolean ihavedepute = false;
    private static int bufsize = 1024;
    private static final int request = 1, reqacc = 2, message = 3, messacc = 4, keepalive = 5, depute = 6;
    private static MessageSender sender;
    private static KeepAlive keeper;
    public static void main(String[] args) throws UnknownHostException
    {
        name = args[0];
        losses = Integer.parseInt(args[1]);
        myport = Integer.parseInt(args[2]);
        if (args.length > 3) {
            connected = true;
            hostname = InetAddress.getByName(args[3]);
            port = Integer.parseInt(args[4]);
        }
        try
        {
            socket = new DatagramSocket(myport);
        }
        catch (SocketException ex)
        {
            ex.printStackTrace();
        }

        if(connected)//connect to neighbour
        {
            byte[] buf_s = new byte[bufsize];
            buf_s = getBuf(request, name);
            DatagramPacket myrequest = new DatagramPacket(buf_s, buf_s.length, hostname, port);

            byte[] buf_r = new byte[bufsize];
            DatagramPacket acc = new DatagramPacket(buf_r, buf_r.length);
            boolean got_acc = false;
            try
            {socket.setSoTimeout(1000);}
            catch (SocketException ex)
            {
                ex.printStackTrace();
            }
            for(int i = 0;i<10 && !got_acc; i++)//10 попыток на соединение
            {
                try
                {
                    socket.send(myrequest);
                    try {
                        socket.receive(acc);
                    }
                    catch (SocketTimeoutException ex)
                    {
                        continue;
                    }
                    int got = getMessageType(acc.getData());
                    if (got == reqacc)
                        got_acc = true;
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
            if(got_acc)
            {
                count++;
                neighbours.put(count,new InetSocketAddress(acc.getAddress(), acc.getPort()));
                names.put(count, getMessage(acc.getData()));
            }
            else connected = false;
        }

        //start sender
        sender = new MessageSender(socket, name, neighbours, message);
        new Thread(sender).start();

        //start keep alive
        keeper = new KeepAlive(socket, neighbours, keepalive);
        new Thread(keeper).start();

        //wait for messages
        byte[] buf_r = new byte[bufsize];
        DatagramPacket receive = new DatagramPacket(buf_r, buf_r.length);
        while (true)
        {
            checkAlives();
            try
            {
                socket.setSoTimeout(0);
                socket.receive(receive);
                long time = System.currentTimeMillis();
                int type = getMessageType(receive.getData());
                    if(type==request)
                    {
                        makeNewNeighbour(receive);
                    }
                    if(type==message) {
                        sendMessage(receive);
                    }
                    if(type==keepalive) {
                        keepAlive(receive, time);
                    }
                    if(type==depute) {
                        makeNewDepute(receive);
                    }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    private static int ID() {
        Random random = new Random();
        return random.nextInt()*(int)System.currentTimeMillis();
    }
    public static int getMessageType(byte[] mess){
        byte[] type = new byte[4];
        for (int i = 0; i < 4; i++) {
            type[i]= mess[i];
        }
        return ByteBuffer.wrap(type).getInt();
    }
    private static String getMessage(byte[] mess){
        byte[] message = new byte[bufsize - 4];
        for (int i = 4; i < bufsize; i++) {
            message[i-4] = mess[i];
        }
        return new String(message);
    }
    public static byte[] getBuf(int type, String mess){
        byte[] mytype = ByteBuffer.allocate(4).putInt(type).array();
        byte[] mymess = mess.getBytes();
        byte[] buf = new byte[bufsize];
        System.arraycopy(mytype, 0, buf, 0, mytype.length);
        System.arraycopy(mymess, 0, buf, mytype.length, mymess.length);
        return  buf;
    }
    private static void makeNewNeighbour(DatagramPacket packet) throws IOException{
        count++;
        InetSocketAddress address = new InetSocketAddress(packet.getAddress(), packet.getPort());
        neighbours.put(count, address);
        String r_name = getMessage(packet.getData());
        names.put(count, r_name);
        sender.putNewAddress(count, address);
        keeper.putNewAddress(count, address);
        DatagramPacket acc = new DatagramPacket(getBuf(reqacc, name), bufsize, packet.getAddress(), packet.getPort());
        socket.send(acc);
        if(!ihavedepute)
        {
            mydeputeadr=address;
            mydeputename=r_name;
            sendDepute();
            ihavedepute = true;
        }
    }
    private static void sendMessage(DatagramPacket packet){
        //подвтердить ,вывести и отправить всем кроме отправителя
        byte[] buf_acc = getBuf(messacc, "Acc");
        try
        {
            socket.send(new DatagramPacket(buf_acc, buf_acc.length, packet.getAddress(), packet.getPort()));
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

        InetSocketAddress address = new InetSocketAddress(packet.getAddress(), packet.getPort());
        int number = getNumber(address);
        String namefrom = names.get(number);
        System.out.println(namefrom + ": " + getMessage(packet.getData()));

        byte[] buf_s  = new String(getMessage(packet.getData())).getBytes();
        DatagramPacket mess_send;
        boolean got_acc = false;
        byte[] buf_r = new byte[bufsize];
        DatagramPacket mess_acc = new DatagramPacket(buf_r, buf_r.length);
        try {
            for (Map.Entry<Integer, InetSocketAddress> entry : neighbours.entrySet()) {
                if (!address.equals(entry.getValue())) {
                    mess_send = new DatagramPacket(buf_s, buf_s.length, entry.getValue().getAddress(), entry.getValue().getPort());
                    while (!got_acc) {
                        socket.send(mess_send);
                        try {
                            socket.receive(mess_acc);
                        } catch (SocketTimeoutException ex) {
                            continue;
                        }
                        if(getMessageType(mess_acc.getData()) == messacc){
                            got_acc = true;
                        }
                    }
                }
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

    }
    private static int getNumber(InetSocketAddress adr){
        int number = 0;
        for (Map.Entry<Integer, InetSocketAddress> entry : neighbours.entrySet())
        {
            if(adr.equals(entry.getValue()))
                number = entry.getKey();
        }
        return number;
    }
    private static void keepAlive(DatagramPacket packet, long time){
        //обновить данные в isalive
        int number = getNumber(new InetSocketAddress(packet.getAddress(), packet.getPort()));
        isalive.put(number, time);
    }
    private static void checkAlives() {
        if(!isalive.isEmpty()) {
            Iterator<Map.Entry<Integer, Long>> entryIt = isalive.entrySet().iterator();
            while (entryIt.hasNext()) {
                Map.Entry<Integer, Long> entry = entryIt.next();
                if (System.currentTimeMillis() - entry.getValue() > 10000) {
                    if (!hasDepute(entry.getKey())) {
                        //если замены нет, удаляем
                        entryIt.remove();
                        neighbours.remove(entry.getKey(), neighbours.get(entry.getKey()));
                        names.remove(entry.getKey(), names.get(entry.getKey()));
                        sender.deleteAddres(entry.getKey(), neighbours.get(entry.getKey()));
                        keeper.deleteAddres(entry.getKey(), neighbours.get(entry.getKey()));
                        if (mydeputeadr.equals(neighbours.get(entry.getKey()))) {//если был моей заменой
                            ihavedepute = false;
                            mydeputename = null;
                            mydeputeadr = null;
                        }
                    }
                }
            }
        }
    }
    private static boolean hasDepute(int number){
        if(deputies.containsKey(number))
        {
            //делем замену и удаляем изначальные
            InetSocketAddress newadress = deputies.get(number);
            String newname = depnames.get(number);
            names.put(number, newname);
            neighbours.put(number, newadress);
            deputies.remove(number, newadress);
            depnames.remove(number, newname);
            return true;
        }
        else return false;
    }
    private static void makeNewDepute(DatagramPacket packet) throws UnknownHostException {
        int number = getNumber(new InetSocketAddress(packet.getAddress(), packet.getPort()));
        String newhost = getHostname(packet.getData());
        int newport = getPort(packet.getData());
        String newname = getName(packet.getData());
        deputies.put(number, new InetSocketAddress(InetAddress.getByName(newhost), newport));
        depnames.put(number, newname);
    }
    private static String getHostname(byte[] adr){
        byte[] hostname = new byte[16];
        for (int i = 8; i < 32; i++) {
            hostname[i-8] = adr[i];
        }
        return new String(hostname);
    }
    private static String getName(byte[] adr){
        byte[] hostname = new byte[bufsize-16];
        for (int i = 32; i < bufsize; i++) {
            hostname[i-32] = adr[i];
        }
        return new String(hostname);
    }
    private static int getPort(byte[] adr){
        byte[] port = new byte[4];
        for (int i = 4; i < 8; i++) {
            port[i]= adr[i];
        }
        return ByteBuffer.wrap(port).getInt();
    }
    private static void sendDepute() throws IOException{
        byte[] buf_s = new byte[bufsize];
        byte[] depaddress = mydeputeadr.getAddress().getHostAddress().getBytes();
        byte[] depport = ByteBuffer.allocate(4).putInt(mydeputeadr.getPort()).array();
        byte[] depname = name.getBytes();
        System.arraycopy(depport, 0, buf_s, 0, depport.length);
        System.arraycopy(depaddress, 0, buf_s, depport.length, depaddress.length);
        System.arraycopy(depname, 0, buf_s, depport.length + depaddress.length, depname.length);
        DatagramPacket mess;
        for (Map.Entry<Integer, InetSocketAddress> entry : neighbours.entrySet()) {
            InetSocketAddress address = entry.getValue();
            if(!mydeputeadr.equals(address)) {
                mess = new DatagramPacket(buf_s, buf_s.length, address.getAddress(), address.getPort());
                socket.send(mess);
            }
            }
    }
}
