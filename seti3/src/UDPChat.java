import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.UUID;

public class UDPChat {
    private static String myName;
    private static int myLosses;
    private static int myPort;
    private static DatagramSocket mySocket;

    private static InetSocketAddress mydeputeadr;
    private static String mydeputename;
    private static boolean ihavedepute = false;

    private static final int bufsize = 8192;
    static final int request = 1, reqack = 2, message = 3, messack = 4, keepalive = 5, depute = 6;

    public static void main(String[] args) {
        myName = args[0];
        myLosses = Integer.parseInt(args[1]);
        myPort = Integer.parseInt(args[2]);

        try
        { mySocket = new DatagramSocket(myPort); }
        catch (SocketException ex)
        { ex.printStackTrace(); }

        if (args.length > 3) {
           try{
               connect(InetAddress.getByName(args[3]), Integer.parseInt(args[4]));//пул
           }
           catch (IOException ex){
               ex.printStackTrace();
           }
        }

        Sender sender = new Sender();
        ReceiveHandler receiveHandler = new ReceiveHandler();
        KeepAlive keepAlive = new KeepAlive();
        CheckTimes checkTimes = new CheckTimes();
        new Thread(keepAlive).start();
        new Thread(receiveHandler).start();
        new Thread(checkTimes).start();

        try
        {
            System.out.println("READY TO WORK");
            while(true) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(System.in));
                String mess = reader.readLine();
                String data = myName+": "+mess;
                System.out.println(data);
                UUID id = getID();
                sender.putMessage(message, id, data);
                sender.sendEveryone(true);
                sender.setAddress(new InetSocketAddress(InetAddress.getLocalHost(), myPort));
                new Thread(sender).start();
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
        }

    }
    public static void connect(InetAddress host, int port) throws IOException{
        UUID id = getID();
        Sender sender = new Sender();
        sender.sendEveryone(false);
        sender.setAddress(new InetSocketAddress(host, port));
        sender.putMessage(request, id, myName);
        new Thread(sender).start();
       // System.out.println("SENT REQUEST");
    }
    public static UUID getID(){
        return UUID.randomUUID();
    }
    public static byte[] getBuffer(int type, UUID id, String data){
        byte[] mytype = ByteBuffer.allocate(4).putInt(type).array();
        byte[] myid = id.toString().getBytes();
        byte[] mydata = data.getBytes();
        byte[] buf = new byte[bufsize];
        System.arraycopy(mytype, 0, buf, 0, mytype.length);
        System.arraycopy(myid, 0, buf, mytype.length, myid.length);
        System.arraycopy(mydata, 0, buf, mytype.length+myid.length, mydata.length);
        return  buf;
    }
    public static int getMessageType(byte[] mess){
        byte[] type = new byte[4];
        for (int i = 0; i < 4; i++) {
            type[i]= mess[i];
        }
        return ByteBuffer.wrap(type).getInt();
    }
    public static UUID getIDFromMessage(byte[] mess){
        byte[] id = new byte[36];
        for (int i = 4; i < 40; i++) {
            id[i-4] = mess[i];
        }
        return UUID.fromString(new String(id));
    }
    public static String getMessage(byte[] mess){
        byte[] message = new byte[bufsize - 40];
        for (int i = 40; i < bufsize; i++) {
            message[i-40] = mess[i];
        }
        return new String(message);
    }
    public static DatagramSocket getSocket(){ return mySocket; }
    public static int getBufsize(){ return bufsize;}
    public static String getMyName(){ return myName;}
    public static boolean haveDepute(){return ihavedepute;}
    public static void setDepute(InetSocketAddress address, String name) throws UnknownHostException{
        mydeputeadr = address;
        mydeputename = name;
        ihavedepute = true;
        sendDepute(true, address);
    }
    public static void sendDepute(boolean bool, InetSocketAddress address) throws UnknownHostException{
        Sender sender = new Sender();
        UUID id = getID();
        sender.putMessage(depute, id, mydeputeadr.toString());
        if(bool) {
            sender.setAddress(address);
            sender.sendEveryone(true);
        }
        else{
            sender.setAddress(address);
            sender.sendEveryone(false);
        }
        new Thread(sender).start();
    }
    public static int getMyPort(){
        return myPort;
    }
    public static int getMyLosses(){
        return myLosses;
    }
    public static InetSocketAddress getMyDepute(){return mydeputeadr;}

}
