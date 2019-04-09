package OwnCode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Receiver implements Runnable{
    private DatagramSocket socket;
    private SlidingWindow slidingWindow;
    private NetworkUser networkUser;

    public Receiver(DatagramSocket socket, SlidingWindow slidingWindow, NetworkUser networkUser){
        this.socket = socket;
        this.slidingWindow = slidingWindow;
        this.networkUser = networkUser;
    }

    @Override
    public void run() {
        try{
            while (true) { //receive
                byte[] buffer = new byte[slidingWindow.getPacketSize()];//packet grootte
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);
                InetAddress clientAddress = receivePacket.getAddress();
                print("received a packet from" + clientAddress.toString());
                print("packet:" + receivePacket.toString());//todo weghalen




                byte[] data = receivePacket.getData();//todo weghalen
                byte commandoByte = data[1];//todo weghalen
                Utils utils = new Utils();//todo weghalen
                int commandonumber = utils.fromByteToInteger(commandoByte);//todo weghalen
                print("commandonumber:");//todo weghalen




                networkUser.inputHandler(receivePacket);
            }
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }

    private void print (String message){ networkUser.print(message); }
}
