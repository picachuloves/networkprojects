import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CheckTimes extends UDPChat implements Runnable {
    @Override
    public void run() {
        UUID id;
        HashMap<UUID, InetSocketAddress> addresses;
        long time1 = System.currentTimeMillis();
        while (true) {
            long time2 = System.currentTimeMillis();
            if (time2 - time1 > 1000) {
                addresses = Addresses.getAddresses();
                Iterator<Map.Entry<UUID, InetSocketAddress>> entryIt = addresses.entrySet().iterator();
                while (entryIt.hasNext()) {
                    Map.Entry<UUID, InetSocketAddress> entry = entryIt.next();
                    id = entry.getKey();
                    if ((Times.hasTime(id)) && (time2 - Times.getTime(id) > 5000)) {
                        System.out.println(Names.getName(id) + " DISCONNECTED");
                        if (Deputies.hasDeputy(id)) {
                            try {
                                connect(Deputies.getAddress(id).getAddress(), Deputies.getAddress(id).getPort());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        if(getMyDepute()!= null && getMyDepute().equals(entry.getValue())){
                            //если моя замена умерла
                            UUID newdepute=Addresses.getAnyID();
                            if(newdepute!=null)
                            {
                                try {
                                    setDepute(Addresses.getAddress(newdepute), Names.getName(newdepute));
                                }
                                catch (UnknownHostException ex){
                                    ex.printStackTrace();
                                }
                            }
                        }
                        Names.deleteName(id);
                        entryIt.remove();
                        Times.deleteTime(id);
                    }
                }
                time1=System.currentTimeMillis();
            }
        }
    }
}
