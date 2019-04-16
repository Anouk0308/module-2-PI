package OwnCode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receiver implements Runnable{
    private DatagramSocket socket;
    private SlidingWindow slidingWindow;
    private NetworkUser networkUser;
    private PacketWithOwnHeader packetWithOwnHeader;

    public Receiver(DatagramSocket socket, SlidingWindow slidingWindow, NetworkUser networkUser){
        this.socket = socket;
        this.slidingWindow = slidingWindow;
        this.networkUser = networkUser;
        packetWithOwnHeader = new PacketWithOwnHeader();
        print("Receiver aan");//todo weghalen
    }

    @Override
    public void run() {
        try{
            while (true) { //receive
                byte[] buffer = new byte[slidingWindow.getPacketSize()];
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);

                int usefulDataLength = receivePacket.getLength();
                if(usefulDataLength < slidingWindow.getPacketSize()){
                    byte[] packetData = receivePacket.getData();
                    byte[] usefulPacket = new byte[usefulDataLength];
                    System.arraycopy(packetData,0,usefulPacket,0,usefulDataLength);
                    DatagramPacket packet = new DatagramPacket(usefulPacket,usefulDataLength);

                    print("received packet with commando"+packet.getData()[packetWithOwnHeader.commandoPosition]);//todo weghalen

                    networkUser.inputHandler(packet);
                } else{
                    networkUser.inputHandler(receivePacket);
                }
            }
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }

    private void print (String message){ networkUser.print(message); }
}
