import java.util.HashMap;
import java.util.UUID;

public class Times {
    private static HashMap<UUID, Long> times = new HashMap<>();

    public static synchronized HashMap<UUID, Long> getTimes() {
        return times;
    }

    public static synchronized void putTime(UUID id, long time) {
        times.put(id, time);
    }

    public static synchronized void deleteTime(UUID id) {
        times.remove(id, times.get(id));
    }

    public static synchronized long getTime(UUID id) {
        return times.get(id);
    }

    public static synchronized boolean hasTime(UUID id){
        boolean result = false;
        if (times.containsKey(id)) {
            result = true;
        }
        return result;
    }

}
