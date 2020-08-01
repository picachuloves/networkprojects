import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.UUID;

public class Messages {
    /*private static HashMap<UUID, InetSocketAddress> messages = new HashMap<>();
    public static synchronized HashMap<UUID, InetSocketAddress> getNames(){
        return messages;
    }
    public static synchronized void putMessage(UUID id, InetSocketAddress address){
        messages.put(id, address);
    }
    public static synchronized void deleteMessage(UUID id) {
        messages.remove(id, messages.get(id));
    }
    public static synchronized InetSocketAddress getMessage(UUID id){
        return messages.get(id);
    }*/
    private static UUID[] messages = new UUID[100];
    private static int i = 0;
    public static synchronized void putMessage(UUID id){
        messages[i] = id;
        i++;
        i%=100;
    }
    public static synchronized boolean contains(UUID id){
        boolean result = false;
        for (int j = 0; j < 100; j++) {
            if (messages[j]!=null && messages[j].equals(id)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
