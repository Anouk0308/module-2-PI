package OwnCode;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Oefenen {
    Utils utils;
    Utils.Timer timer;
    Checksum checksum;
    Server server;
    PacketWithOwnHeader packetWithOwnHeader;
    SlidingWindow slidingWindow;
    Hardcoded hardcoded;

    public static void main(String[] args) {
        Oefenen oefenen = new Oefenen();
    }

    public Oefenen(){
        startUp();

        byte[] bytesFile = null;
        String fileString = "/Users/anouk.schoenmakers/Desktop/ClientFiles/9b.txt";
        File file = new File(fileString);
        Path pathFile = file.toPath();

        try {
            bytesFile = Files.readAllBytes(pathFile);
            System.out.println("jeej 1");
        } catch (IOException e){
            System.out.println(e.getMessage());
        }

        String file1String = "/Users/anouk.schoenmakers/Desktop/ServerFiles/9b.txt";
        File file1 = new File(file1String);
        System.out.println(file1);
        Path pathFile1 = file1.toPath();
        System.out.println(pathFile1);

        try{
            Files.write(pathFile1, bytesFile, StandardOpenOption.APPEND);
            System.out.println("jeej2");
        } catch (IOException e){
            print("fout:"+e.getMessage());
        }


        /*
        InetAddress ownAdress = hardcoded.getInetAdressComputer();
        byte[] ownAdressBytes = ownAdress.getAddress();


        byte[] buffer = packetWithOwnHeader.commandoOne();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        DatagramPacket checkedPacket = checksum.checkingChecksum(packet);

        if(checkedPacket.equals(packet)){
            System.out.println("jeej");
        } else{
            System.out.println("nooo");
        }


*/







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
        hardcoded = new Hardcoded();
        server = new Server(hardcoded.getInetAdressComputer(), portClient, portServer);
        slidingWindow = new SlidingWindow();
    }





    private static void print (String message){
        System.out.println(message);
    }
}
