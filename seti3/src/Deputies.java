import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.UUID;

public class Deputies {
    private static HashMap<UUID, InetSocketAddress> deputies = new HashMap<>();
    //private static HashMap<UUID, String> names = new HashMap<>();
    public static synchronized HashMap<UUID, InetSocketAddress> getDeputies(){
        return deputies;
    }
    //public static synchronized HashMap<UUID, String> getNames(){
    //    return names;
    //}
    public static synchronized void putDepute(UUID id, InetSocketAddress address) {
        deputies.put(id, address);
      //  names.put(id, name);
    }
    public static synchronized void deleteDepute(UUID id){
        deputies.remove(id, deputies.get(id));
       // names.remove(id, names.get(id));
    }
    public static synchronized InetSocketAddress getAddress(UUID id){
        return deputies.get(id);
    }
   // public static synchronized String getName(UUID id){
    //    return names.get(id);
   // }
    public static synchronized boolean hasDeputy(UUID id){
        boolean result = false;
        if(deputies.containsKey(id))
            result = true;
        return result;
    }
}
