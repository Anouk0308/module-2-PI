package com.nedap.university;

import OwnCode.Hardcoded;
import OwnCode.Server;

import java.io.IOException;
import java.net.*;

public class Main {

    private Main() {}

    public static void main(String[] args) {
        int serverPort = 8888;
        InetAddress clientAddress;
        int clientPort = 8000;

        //without broadcast:
        Hardcoded hardcoded = new Hardcoded();
        clientAddress = hardcoded.getInetAdressComputer();


        //broadcast:
        /*
        boolean sending = true;
        DatagramSocket socket = null;
        DatagramPacket broadcastPacket = null;
        DatagramPacket responcePacket = null;
        try{
            socket = new DatagramSocket(serverPort);
            InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

            broadcastPacket = new DatagramPacket(new byte[555], 555, broadcastAddress, clientPort);
            responcePacket = new DatagramPacket(new byte[555], 555);
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

        while (sending){
            try{
                socket.send(broadcastPacket);
                socket.setSoTimeout(1000);
                socket.receive(responcePacket);
                socket.setSoTimeout(0);
                sending = false;
            } catch (IOException e){
            }
        }
        clientAddress = responcePacket.getAddress();
        */

        Server server = new Server(clientAddress, clientPort, serverPort);
        Thread serverThread = new Thread(server);
        serverThread.start();
        System.out.println("The server has started");
    }
}
