package OwnCode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainClient {

    private MainClient() {}

    public static void main(String[] args) {
        int clientPort = 8000;
        InetAddress serverAddress;
        int serverPort = 8888;

        //without broadcast:
/*
        try {
            String PIstring = "172.16.1.1";//broadcast//to pi
            serverAddress = InetAddress.getByName(PIstring);//to pi

            Client client = new Client(serverAddress, serverPort, clientPort);
            Thread clientThread = new Thread(client);
            clientThread.start();
            System.out.println("The client has started");
        } catch (UnknownHostException e){
            System.out.println(e.getMessage());
        }
        */


        Hardcoded hardcoded = new Hardcoded();//to computer
        serverAddress = hardcoded.getInetAdressComputer();// to commputer

        Client client = new Client(serverAddress, serverPort, clientPort);
        Thread clientThread = new Thread(client);
        clientThread.start();
        System.out.println("The client has started");


        //broadcast:
        /*
        try{
            DatagramSocket socket = new DatagramSocket(clientPort);

            DatagramPacket broadcastPacket = new DatagramPacket(new byte[555], 555);

            socket.receive(broadcastPacket);
            serverAddress = broadcastPacket.getAddress();
            DatagramPacket responsePacket = new DatagramPacket(new byte[555], 555, serverAddess, serverPort);
            socket.send(responsePacket);
        } catch(IOException e){
            System.out.println(e.getMessage() + "Client");
        }

        Client client = new Client(serverAddress, serverPort, clientPort);
        Thread clientThread = new Thread(client);
        clientThread.start();
        System.out.println("The client has started");
        */
    }
}
