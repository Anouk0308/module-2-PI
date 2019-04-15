package OwnCode;

import java.net.DatagramPacket;

public class SlidingWindow {
    private int windowSize = 20;
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


    public DatagramPacket[] slice(byte[] rawData, int processID){
        DatagramPacket[] sendingPackets = null;

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
        int lenghtLastPart = rawDataLenght-((numberPackets-1)*rawDataSpace);
        byte[] rawDataLastPart = new byte[lenghtLastPart];
        System.arraycopy(rawData, ((numberPackets-1)*rawDataSpace), rawDataLastPart, 0, lenghtLastPart);

        byte[] lastPacketInBytes = packetWithOwnHeader.commandoEight(processID, numberPackets-1,rawDataLastPart);
        DatagramPacket lastPacket = new DatagramPacket(lastPacketInBytes, lastPacketInBytes.length);

        sendingPackets[numberPackets-1] = lastPacket;

        return sendingPackets;
    }
}
