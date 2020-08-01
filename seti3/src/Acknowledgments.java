import java.util.HashMap;
import java.util.UUID;

public class Acknowledgments {
    private static HashMap<UUID, Boolean> acknowledgments = new HashMap<>();
    public static synchronized HashMap<UUID, Boolean> getAcknowledgments(){
        return acknowledgments;
    }

    public static synchronized void gotAcknowledgment(UUID id){
        acknowledgments.put(id, true);
      //  staticNotify();
    }

    public static synchronized void makeAcknowledgment(UUID id){
        acknowledgments.put(id, false);
    }
    public static synchronized void deleteAcknowledgment(UUID id){
        acknowledgments.remove(id, acknowledgments.get(id));
    }
    public static synchronized boolean getAcknowledgment(UUID id) throws InterruptedException{
       // staticWait();
        Thread.sleep(500);
        return acknowledgments.get(id);
    }
    static private Object obj = new Object();

    public static void staticWait() {
        synchronized (obj) {
            try {
                obj.wait();
            } catch (Exception e) {}
        }
    }

    public static void staticNotify() {
        synchronized (obj) {
            obj.notify();
        }
    }
}
