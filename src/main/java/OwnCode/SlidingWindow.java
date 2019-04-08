package OwnCode;

import javax.imageio.IIOException;
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

    public void setWindowSize(int size){
        this.windowSize = size;
    }

    public void setPacketSize(int size){
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
            double rawDataLenght = rawData.length;
            int numberPackets = (int) Math.ceil(rawDataLenght/(double) rawDataSpace);
            sendingPackets = new DatagramPacket[numberPackets];

            for(int i = 0; i < numberPackets-1; i++){//last packet gets another header
                byte[] rawDataPart = new byte[rawDataSpace];
                System.arraycopy(rawData, i*rawDataSpace, rawDataPart, 0,  rawDataSpace);

                byte[] packetInBytes = packetWithOwnHeader.commandoSix(processID, i, rawDataPart);
                DatagramPacket packet = new DatagramPacket(packetInBytes, packetInBytes.length);

                sendingPackets[i] = packet;
            }

            //last packet
            byte[] rawDataLastPart = new byte[rawDataSpace];//todo, rawDataSpace is wss groter dan laatste packetje groot hoeft te zijn
            System.arraycopy(rawData, (numberPackets-1)*rawDataSpace, rawDataLastPart, 0, rawDataSpace);
            byte[] lastPacketInBytes = packetWithOwnHeader.commandoEight(processID, numberPackets-1,rawDataLastPart);
            DatagramPacket lastPacket = new DatagramPacket(lastPacketInBytes, lastPacketInBytes.length);
            sendingPackets[numberPackets-1] = null;

        } catch (IOException e){
            print(e.getMessage());
        }
        return sendingPackets;
    }

    private static void print (String message){
        System.out.println(message);
    }

}

/*

*/
