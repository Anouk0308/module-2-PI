package OwnCode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class SlidingWindow {
    private int windowSize = 3;
    private int packetSize = 1024;
    private static int headerSpace = 9;
    private int rawDataSpace = packetSize - headerSpace;
    private PacketWithOwnHeader packetWithOwnHeader = new PacketWithOwnHeader();

    public int getWindowSize() {
        return windowSize;
    }

    public int getPacketSize() {
        return packetSize;
    }


    public DatagramPacket[] slice(File file, int processID){
        DatagramPacket[] sendingPackets = null;
        int rawDataLength = (int)file.length();
        int numberPackets = (int) Math.ceil((double) rawDataLength / (double) rawDataSpace);
        sendingPackets = new DatagramPacket[numberPackets];

        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            for (int i = 0; i < numberPackets - 1; i++) {//last packet gets another header
                byte[] rawDataPart = new byte[rawDataSpace];
                inputStream.read(rawDataPart);
                byte[] packetInBytes = packetWithOwnHeader.commandoSix(processID, i, rawDataPart);
                DatagramPacket packet = new DatagramPacket(packetInBytes, packetInBytes.length);

                sendingPackets[i] = packet;
            }

            //last packet
            int lenghtLastPart = rawDataLength - ((numberPackets - 1) * rawDataSpace);
            byte[] rawDataLastPart = new byte[lenghtLastPart];
            inputStream.read(rawDataLastPart);

            byte[] lastPacketInBytes = packetWithOwnHeader.commandoEight(processID, numberPackets - 1, rawDataLastPart);
            DatagramPacket lastPacket = new DatagramPacket(lastPacketInBytes, lastPacketInBytes.length);

            sendingPackets[numberPackets - 1] = lastPacket;
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

        return sendingPackets;
    }
}
