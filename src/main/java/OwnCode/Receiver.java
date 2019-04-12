package OwnCode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

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
    }

    @Override
    public void run() {
        print("Receiver started");//todo weghalen
        try{
            while (true) { //receive
                byte[] buffer = new byte[slidingWindow.getPacketSize()];//packet grootte
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);
                print(Arrays.toString(receivePacket.getData()));
                /*
                InetAddress sourceAddress = receivePacket.getAddress();
                print("Received packet from: " + sourceAddress);
                print("commando number: " + receivePacket.getData()[packetWithOwnHeader.commandoPosition]);
                */

                /*
                for (int i = 0; i < 10; i++){
                    print(Byte.toString(receivePacket.getData()[i]));
                }
                    */

                int usefulDataLength = receivePacket.getLength();
                if(usefulDataLength < slidingWindow.getPacketSize()){
                    byte[] packetData = receivePacket.getData();
                    byte[] usefulPacket = new byte[usefulDataLength];
                    System.arraycopy(packetData,0,usefulPacket,0,usefulDataLength);
                    DatagramPacket packet = new DatagramPacket(usefulPacket,usefulDataLength);
                    print("packet received with commandonumber" + Byte.toString(packetData[packetWithOwnHeader.commandoPosition]));

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
