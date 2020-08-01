import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Addresses {
    private static final HashMap<UUID, InetSocketAddress> addresses = new HashMap<>();//потокобезопасность

    public static synchronized HashMap<UUID, InetSocketAddress> getAddresses(){
        return addresses;
    }
    public static synchronized void putAddress(UUID id, InetSocketAddress address){
        addresses.put(id, address);
    }
    public static synchronized void deleteAddress(UUID id){
        addresses.remove(id, addresses.get(id));
    }
    public static synchronized InetSocketAddress getAddress(UUID id){
        return addresses.get(id);
    }
    public static synchronized UUID getID(InetSocketAddress address){
        UUID id = null;
        for (Map.Entry<UUID, InetSocketAddress> entry : addresses.entrySet())
        {
            if(address.equals(entry.getValue()))
                id = entry.getKey();
        }
        return id;
    }
    public static synchronized UUID getAnyID(){
        UUID id = null;
        for(UUID key: addresses.keySet()){
            id=key;
            break;
        }
        return id;
    }
}
