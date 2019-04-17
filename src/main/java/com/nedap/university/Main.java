package com.nedap.university;

import OwnCode.Hardcoded;
import OwnCode.Server;

import java.net.InetAddress;

public class Main {

    private Main() {}

    public static void main(String[] args) {
        Hardcoded hardcoded = new Hardcoded();//todo ervoor zorgen dat dit niet hardcoded hoeft
        int portServer = 8888;
        int portClient = 8000;
        InetAddress computerAddress = hardcoded.getInetAdressComputer();
        Server server = new Server(computerAddress,portClient,portServer);
        Thread serverThread = new Thread(server);
        serverThread.start();
        System.out.println("The server has started");
    }


}
