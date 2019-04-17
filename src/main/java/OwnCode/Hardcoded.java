package OwnCode;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Hardcoded {
    public InetAddress getInetAdressComputer() {
        try {
            String computerString = "192.168.178.27";
            InetAddress computerAddress = InetAddress.getByName(computerString);
            return computerAddress;
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


    public static void main(String[] args) {
        try {
            String computerAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println(computerAddress);
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }

    }
}




