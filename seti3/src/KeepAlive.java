import java.net.*;

public class KeepAlive extends UDPChat implements Runnable {
    @Override
    public void run() {
        long time = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - time > 500) {
                try{
                Sender sender = new Sender();
                sender.sendEveryone(true);
                sender.setAddress(new InetSocketAddress(InetAddress.getLocalHost(), getMyPort()));
                sender.putMessage(keepalive, getID(), "I'm alive");
                new Thread(sender).start();
                } catch (UnknownHostException ex){
                    ex.printStackTrace();
                }
                time=System.currentTimeMillis();
            }

        }
    }
}
