package OwnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;

public class ZZZtodoLijstVoorMezelf {

    //timer:
    //upload sent last packet
    //download zelfde probleem als upload. probleem met timer
    //op andere plekken ook naar kijken


    //alle print langs gaan of het is wat ik wil laten zien


    //statistics, kijken of start en stop lukt

    //na uploaden of dowloaden nog meer opties kunnen kiezen
    //dan kijken of je verschillende processen kan zien, en dingen kan pauzeren of niet

    //hardcoded zooi eruit slopen


    //broadcast


}
client:

    public static void main(String[] args) {

        System.out.println("Start client");
        // make socket, broadcastIP, a packet to send and a package to receive
        try {
            socket = new DatagramSocket(clientPort);
            socket.setBroadcast(true);
            broadcastIP = InetAddress.getByName("255.255.255.255");
        } catch (SocketException e) {
            System.out.println("ERROR: couldn't construct a DatagramSocket object!");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.out.println("ERROR: no valid hostname!");
            e.printStackTrace();
        }

        String broadcastMessage = "SYN";
        byte[] broadcast = broadcastMessage.getBytes();
        DatagramPacket broadcastPacket = new DatagramPacket(broadcast, broadcast.length, broadcastIP, serverPort);
        byte[] responseBuffer = new byte[512];
        DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
        System.out.println("Client made broadcast message");
        boolean sending = true;

        // send broadcast message and receive response from the server
        while (sending) {
            try {
                socket.send(broadcastPacket);
                System.out.println("Client send broadcast message");
                socket.setSoTimeout(1000); // set timeout
                socket.receive(responsePacket);
                socket.setSoTimeout(0); // cancel timeout
                sending = false;
            } catch (SocketTimeoutException e) {
                //just continue the loop
            } catch (IOException e) {
                System.out.println("ERROR: couldn't send or receive broadcast message!");
                e.printStackTrace();
            }
        }


        // get info from response packet from the server
        System.out.println("Client received message");
        serverAddress = responsePacket.getAddress();
        serverPort = responsePacket.getPort();

        // Send ack
        String ackMessage = "ACK";
        byte[] ack = ackMessage.getBytes();
        DatagramPacket ackPacket = new DatagramPacket(ack, ack.length, serverAddress, serverPort);
        try {
            socket.send(ackPacket);
            System.out.println("Client send ack");
        } catch (IOException e) {
            System.out.println("ERROR: couldn't send ack message!");
            e.printStackTrace();
        }

        // construct new client object and start the user and server input threads
        Client client = new Client();
        System.out.println("Client constructed");
        client.startUserInput();
        client.startServerInput();
    }

    server:
public static void main(String[] args) {
        System.out.println("Start server");

        // receive broadcast message from client
        byte[] buffer = new byte[512];
        DatagramPacket bufferPacket = new DatagramPacket(buffer, buffer.length);
        try {
        socket = new DatagramSocket(serverPort);
        socket.setBroadcast(true);
        System.out.println("Server created socket");
        socket.receive(bufferPacket);
        System.out.println("Server received broadcast");
        } catch (IOException e) {
        System.out.println("ERROR: couldn't receive broadcast message!");
        e.printStackTrace();
        }

        // get info from response packet from the server
        clientAddress = bufferPacket.getAddress();
        clientPort = bufferPacket.getPort();

        // send response + ack and wait for ack
        String responseMessage = "SYN + ACK";
        byte[] response = responseMessage.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(response, response.length, clientAddress, clientPort);
        boolean sending = true;

        while (sending) {
        try {
        socket.send(responsePacket);
        System.out.println("Server send syn+ack");

        socket.setSoTimeout(1000); // set timeout
        socket.receive(bufferPacket);
        socket.setSoTimeout(0); // cancel timeout
        sending = false;
        } catch (SocketTimeoutException e) {
        //just continue the loop
        } catch (IOException e) {
        System.out.println("ERROR: couldn't send SYN + ACK message!");
        e.printStackTrace();
        }
        }

        // construct new server object and start the client input loop
        System.out.println("Server received message");
        Server server = new Server();
        System.out.println("Server constructed");
        server.startClientInputLoop();
        }