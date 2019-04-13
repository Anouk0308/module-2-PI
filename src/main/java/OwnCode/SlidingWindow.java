package OwnCode;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SlidingWindow {
    private int windowSize = 3;
    private int packetSize = 512;
    private static int headerSpace = 9;
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

        sendingPackets = new DatagramPacket[numberPackets];

        for(int i = 0; i < numberPackets-1; i++){//last packet gets another header
            byte[] rawDataPart = new byte[rawDataSpace];
            System.arraycopy(rawData, i*rawDataSpace, rawDataPart, 0,  rawDataSpace);

            byte[] packetInBytes = packetWithOwnHeader.commandoSix(processID, i, rawDataPart);
            DatagramPacket packet = new DatagramPacket(packetInBytes, packetInBytes.length);
            sendingPackets[i] = packet;
        }

        //last packet
        int lenghtLastPart = rawDataLenght-((numberPackets-1) * rawDataSpace);
        byte[]rawDataExtra = byteArr;

        byte[] rawDataLastPart = new byte[lenghtLastPart];
        System.arraycopy(rawDataExtra, (numberPackets-1)*lenghtLastPart, rawDataLastPart, 0, lenghtLastPart);

        byte[] lastPacketInBytes = packetWithOwnHeader.commandoEight(processID, numberPackets-1,rawDataLastPart);
        DatagramPacket lastPacket = new DatagramPacket(lastPacketInBytes, lastPacketInBytes.length);
        sendingPackets[numberPackets-1] = lastPacket;

        return sendingPackets;
    }

    private static void print (String message){
        System.out.println(message);
    }

}
