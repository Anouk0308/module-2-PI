package OwnCode;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SlidingWindow {
    private int windowSize = 10;
    private int packetSize = 512;
    private static int headerSpace = 6;
    private int rawDataSpace = packetSize - headerSpace;
    private PacketWithOwnHeader packetWithOwnHeader = new PacketWithOwnHeader();

    public void setWindowSize(int size){//todo, voor als ik wil testen wat beste werkt qua grote
        this.windowSize = size;
    }

    public void setPacketSize(int size){//todo, voor als ik wil testen wat beste werkt qua grote
        this.packetSize = size;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public int getRawDataSpace() {
        return rawDataSpace;
    }

    public DatagramPacket[] slice(File file, int processID){
        DatagramPacket[] sendingPackets = null;


        Path path = Paths.get(file.getAbsolutePath());
        try{
            byte[] rawData = Files.readAllBytes(path);
            int rawDataLenght = rawData.length;
            int numberPackets = (int) Math.ceil((double)rawDataLenght/(double) rawDataSpace);
            sendingPackets = new DatagramPacket[numberPackets];

            for(int i = 0; i < numberPackets-1; i++){//last packet gets another header
                byte[] rawDataPart = new byte[rawDataSpace];
                System.arraycopy(rawData, i*rawDataSpace, rawDataPart, 0,  rawDataSpace);

                byte[] packetInBytes = packetWithOwnHeader.commandoSix(processID, i, rawDataPart);
                DatagramPacket packet = new DatagramPacket(packetInBytes, packetInBytes.length);

                sendingPackets[i] = packet;
            }

            //last packet
            int lenghtLastPart = rawDataLenght%numberPackets;
            byte[] rawDataLastPart = new byte[lenghtLastPart];//todo, rawDataSpace is wss groter dan laatste packetje groot hoeft te zijn
            System.arraycopy(rawData, (numberPackets-1)*lenghtLastPart, rawDataLastPart, 0, lenghtLastPart);
            byte[] lastPacketInBytes = packetWithOwnHeader.commandoEight(processID, numberPackets-1,rawDataLastPart);
            DatagramPacket lastPacket = new DatagramPacket(lastPacketInBytes, lastPacketInBytes.length);
            sendingPackets[numberPackets-1] = lastPacket;

        } catch (IOException e){
            print(e.getMessage());
        }
        return sendingPackets;
    }

    public DatagramPacket[] fakeSlice(byte[] byteArr, int processID){//todo fake
        DatagramPacket[] sendingPackets = null;
        byte[]rawData = byteArr;

        int rawDataLenght = rawData.length;
        int numberPackets = (int) Math.ceil((double)rawDataLenght/(double) rawDataSpace);
        System.out.println("number of packets: "+numberPackets);//todo weghalen

        sendingPackets = new DatagramPacket[numberPackets];

        for(int i = 0; i < numberPackets-1; i++){//last packet gets another header
            byte[] rawDataPart = new byte[rawDataSpace];
            System.arraycopy(rawData, i*rawDataSpace, rawDataPart, 0,  rawDataSpace);

            for(int in = 0; in < rawDataPart.length; in++){
                System.out.println("byte in packet on place " + in + ":"+ rawDataPart[in]); //todo weghalen
            }

            byte[] packetInBytes = packetWithOwnHeader.commandoSix(processID, i, rawDataPart);
            DatagramPacket packet = new DatagramPacket(packetInBytes, packetInBytes.length);
            sendingPackets[i] = packet;
        }
        for(int i = 0; i < sendingPackets.length; i++){
            System.out.println("packet to send on place"+i+":"+sendingPackets[i]);//todo weghalen
        }


        //last packet
        int lenghtLastPart = rawDataLenght%numberPackets;
        System.out.println("lenght last part" + lenghtLastPart);

        byte[] rawDataLastPart = new byte[lenghtLastPart];
        System.arraycopy(rawData, (numberPackets-1)*lenghtLastPart, rawDataLastPart, 0, lenghtLastPart);

        for(int in = 0; in < rawDataLastPart.length; in++){
            System.out.println("byte in packet on place " + in + ":"+ rawDataLastPart[in]); //todo weghalen
        }

        byte[] lastPacketInBytes = packetWithOwnHeader.commandoEight(processID, numberPackets-1,rawDataLastPart);
        DatagramPacket lastPacket = new DatagramPacket(lastPacketInBytes, lastPacketInBytes.length);
        System.out.println(lastPacket);//todo weghalen
        sendingPackets[numberPackets-1] = lastPacket;

        for(int i = 0; i < sendingPackets.length; i++){
            System.out.println("packet to send on place"+i+":"+sendingPackets[i]);//todo weghalen
        }

        return sendingPackets;
    }

    private static void print (String message){
        System.out.println(message);
    }

}
