import java.util.HashMap;
import java.util.UUID;

public class Names {
    private static HashMap<UUID, String> names = new HashMap<>();
    public static synchronized HashMap<UUID, String> getNames(){
        return names;
    }
    public static synchronized void putName(UUID id, String name){
        names.put(id, name);
    }
    public static synchronized void deleteName(UUID id) {
        names.remove(id, names.get(id));
    }
    public static synchronized String getName(UUID id){
        return names.get(id);
    }
}
