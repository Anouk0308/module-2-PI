package OwnCode;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class Hardcoded {
    public InetAddress getInetAdressComputer(){
        try{
            String computerString = "172.16.1.87";//todo ervoor zorgen dat dit niet hardcoded hoeft
            InetAddress computerAddress = InetAddress.getByName(computerString);
            return computerAddress;
        } catch (UnknownHostException e){
            System.out.println(e.getMessage());
        }
       return null;
    }

    public static void main(String[] args) {
        try{
            Enumeration en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) en.nextElement();
                Enumeration ee = ni.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress ia = (InetAddress) ee.nextElement();
                    System.out.println(ia.getHostAddress());
                }
            }

/*
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e){
            System.out.println(e.getMessage());*/
        } catch(SocketException e){
            System.out.println("Hardcoded otherWay error " + e.getMessage());
        }
    }
}
