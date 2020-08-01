import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class test {
    public static void main(String[] args) throws UnknownHostException {
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByName("vk.com"), 5555);
        String name = "Name";
        String result = address.toString()+";"+name;
        String[] array = result.split(";");
    }
}
