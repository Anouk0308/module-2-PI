package OwnCode;

import java.io.IOException;
import java.net.InetAddress;

public class MainClient {

    private MainClient() {}

    public static void main(String[] args) {
        try{
            String PIstring = "172.16.1.1";
            InetAddress PI = InetAddress.getByName(PIstring);
            int portServer = 8888;
            int portClient = 8000;
            Client client = new Client( PI, portServer, portClient);//todo to pi
            Thread clientThread = new Thread(client);
            clientThread.start();
        } catch(IOException e){
            System.out.println(e.getMessage() + "Client");
        }

    }


}