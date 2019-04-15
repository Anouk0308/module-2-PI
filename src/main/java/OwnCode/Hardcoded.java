package OwnCode;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Hardcoded {
    public InetAddress getInetAdressComputer(){
        try{
            String computerString = "10.6.16.27";//todo ervoor zorgen dat dit niet hardcoded hoeft
            InetAddress computerAddress = InetAddress.getByName(computerString);
            return computerAddress;
        } catch (UnknownHostException e){
            System.out.println(e.getMessage());
        }
       return null;
    }

    public static void main(String[] args) {
        try{
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e){
            System.out.println(e.getMessage());
        }
    }
}
