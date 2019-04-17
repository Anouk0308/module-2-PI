package OwnCode;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

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

        int negen = 9;
        int drie = 3;
        int i = negen/drie;
        System.out.println(i);
/*
        byte[] bytesFile = null;
        String file1String = "/Users/anouk.schoenmakers/Desktop/ClientFiles/9b.txt";
        File file1 = new File(file1String);
        Path pathFile1 = file1.toPath();
        byte[] gedeelte1 = new byte[3];
        byte[] gedeelte2 = new byte[3];
        byte[] gedeelte3 = new byte[3];

        try {
           bytesFile = Files.readAllBytes(pathFile1);
            System.out.println(Arrays.toString(bytesFile));

*/
           /*
            byte[] gedeelte1 = new byte[3];
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(gedeelte1);
            System.out.println(Arrays.toString(gedeelte1));
            System.out.println(fileInputStream.available());
            fileInputStream.read(gedeelte1);
            System.out.println(Arrays.toString(gedeelte1));
            System.out.println(fileInputStream.available());
            fileInputStream.read(gedeelte1);
            System.out.println(Arrays.toString(gedeelte1));
*//*
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file1));
            inputStream.read(gedeelte1);
            System.out.println(Arrays.toString(gedeelte1));
            System.out.println(inputStream.available());
            inputStream.read(gedeelte2);
            System.out.println(Arrays.toString(gedeelte2));
            System.out.println(inputStream.available());
            inputStream.read(gedeelte3);
            System.out.println(Arrays.toString(gedeelte3));


        } catch(IOException e){

        }

        String file2String = "/Users/anouk.schoenmakers/Desktop/ServerFiles/9b.txt";
        File file2 = new File(file2String);
        Path pathFile2 = file2.toPath();

        try{
            FileOutputStream fileOutputStream = new FileOutputStream(file2);
            fileOutputStream.write(gedeelte1);
            System.out.println(Arrays.toString(Files.readAllBytes(pathFile2)));
            fileOutputStream.write(gedeelte2);
            System.out.println(Arrays.toString(Files.readAllBytes(pathFile2)));
            fileOutputStream.write(gedeelte3);
            System.out.println(Arrays.toString(Files.readAllBytes(pathFile2)));

        } catch (IOException e){
            print("fout:"+e.getMessage());
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
