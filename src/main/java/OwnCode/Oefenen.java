package OwnCode;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class Oefenen {
    Utils utils;
    Utils.Timer timer;
    Checksum checksum;
    Server server;
    PacketWithOwnHeader packetWithOwnHeader;
    SlidingWindow slidingWindow;

    public static void main(String[] args) {
        Oefenen oefenen = new Oefenen();
    }

    public Oefenen(){
        startUp();
       byte[] fakeFile = new byte[35000000];//todo dit is fake
        for(int i = 0; i < fakeFile.length; i++){
            fakeFile[i]= 2;
        }

        DatagramPacket[] packets = slidingWindow.slice(fakeFile, 1);

        for(int i = 0; i < packets.length; i++){
            int packetnumber = utils.limitBytesToInteger(packets[i].getData()[packetWithOwnHeader.packetNumberPosition], packets[i].getData()[packetWithOwnHeader.packetNumberPosition+1]);

                System.out.println("packetnumber:" + packetnumber);

        }

        System.out.println("");









        /*
        byte[] b = new byte[5];
        b[0]=1;
        b[1] = 2;
        b[2]=9;
        b[3]=10;

        String s = utils.fromByteArrToStringBit(b);
       // print(s);

        //byte[] packetBytes = packetWithOwnHeader.commandoTwo(b);

        byte[] packetBytes = packetWithOwnHeader.commandoTwo(b);


        String ss = utils.fromByteArrToStringBit(packetBytes);
        print(ss);

        DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);

        for (int i = 0; i < 10; i++){
            System.out.print(Byte.toString(packet.getData()[i]));
        }
        System.out.println("");

        DatagramPacket newPacket = checksum.checkingChecksum(packet);

        for (int i = 0; i < 10; i++){
            System.out.print(Byte.toString(newPacket.getData()[i]));
        }
        */
    }

    public void startUp(){
        utils = new Utils();
        checksum = new Checksum();
        packetWithOwnHeader = new PacketWithOwnHeader();
        int portServer = 8888;
        int portClient = 8000;
        Hardcoded hardcoded = new Hardcoded();
        server = new Server(hardcoded.getInetAdressComputer(), portClient, portServer);
        slidingWindow = new SlidingWindow();
    }





    private static void print (String message){
        System.out.println(message);
    }
}
