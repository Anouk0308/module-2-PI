package OwnCode;

import java.io.IOException;
import java.net.InetAddress;

public class MainClient {

    private MainClient() {}

    public static void main(String[] args) {
        Hardcoded hardcoded = new Hardcoded();//todo ervoor zorgen dat dit niet hardcoded hoeft
        try{
            String PIstring = "172.16.1.1";
            InetAddress PI = InetAddress.getByName(PIstring);//todo to pi
            InetAddress computer = hardcoded.getInetAdressComputer();//todo to commputer
            int portServer = 8888;
            int portClient = 8000;
            Client client = new Client(computer, portServer, portClient);
            Thread clientThread = new Thread(client);
            clientThread.start();
        } catch(IOException e){
            System.out.println(e.getMessage() + "Client");
        }

    }


}