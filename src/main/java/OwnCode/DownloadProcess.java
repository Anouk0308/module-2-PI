package OwnCode;

import java.io.File;
import java.net.DatagramPacket;

public class DownloadProcess implements Process{
    private int processID;
    private String fileName;
    private int packetSize;
    private int windowSize;
    private DatagramPacket[] downloadingPackets = new DatagramPacket[100000]; //todo, kijken hoe dit op te lossen
    private File file;

    private NetworkUser networkUser;
    private String filePath; // where are the files placed

    private SlidingWindow slidingWindow;
    private Utils utils;
    private PacketWithOwnHeader packetWithOwnHeader;

    private boolean acknowledgementToStop = false;

    public boolean isInterrupted = false;
    private boolean receivedAPacket = false;

    public DownloadProcess(int processID, String fileName, NetworkUser networkUser, String filePath, boolean isClient){
        this.processID = processID;
        this.fileName = fileName;
        this.networkUser = networkUser;
        this.packetSize = slidingWindow.getPacketSize();
        this.windowSize = slidingWindow.getWindowSize();
        this.filePath = filePath;
        if(isClient){
            handshake();
        }
    }

    public void handshake(){
        byte[] buffer = packetWithOwnHeader.commandoFour(processID, fileName);
        DatagramPacket startPacket = new DatagramPacket(buffer, buffer.length);
        networkUser.send(startPacket);
        print("Starting downloading process...");

        //set timer
        Utils.Timer timer = utils.new Timer(1000);
        try{
            while (!timer.isTooLate()) {
                Thread.sleep(10);
            }
            //timer went off
            if(receivedAPacket=false){//if there is still no packet received:
                handshake();
            }

        } catch (InterruptedException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public void receivePacket(DatagramPacket packet){

        receivedAPacket = true;//for timer in handshake()

        while(!isInterrupted){//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[4], packetData[5]);
            downloadingPackets[packetNumber] = packet;

            int packetNumberSuccessive = -1;
            for(int i = 0; i < downloadingPackets.length; i++){
                if(downloadingPackets[i+1] == null){
                    packetNumberSuccessive = i;
                    break;
                }
            }

            byte[] buffer = packetWithOwnHeader.commandoSeven(processID, packetNumberSuccessive);
            DatagramPacket acknowledgePacket = new DatagramPacket(buffer, buffer.length);
            networkUser.send(acknowledgePacket);
        }
    }

    public void receiveLastPacket(DatagramPacket packet){
        while(!isInterrupted) {//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[4], packetData[5]);
            downloadingPackets[packetNumber] = packet;

            int packetNumberSuccessive = -1;
            for (int i = 0; i < downloadingPackets.length; i++) {
                if (downloadingPackets[i + 1] == null) {
                    packetNumberSuccessive = i;
                    break;
                }
            }

            if (packetNumberSuccessive == packetNumber) {//everything is received
                //tell the other that everything is received
                byte[] buffer = packetWithOwnHeader.commandoNine(processID, packetNumberSuccessive);
                DatagramPacket acknowledgePacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(acknowledgePacket);

                //create file from packetlist
                int newPacketsArrayLenght = downloadingPackets.length;
                for (int i = 0; i < downloadingPackets.length; i++) {
                    if (downloadingPackets[i] == null) {
                        newPacketsArrayLenght--;
                    }
                }
                DatagramPacket[] newPacketArray = new DatagramPacket[newPacketsArrayLenght];
                System.arraycopy(downloadingPackets, 0, newPacketArray, 0, newPacketsArrayLenght);
                utils.packetsToFile(newPacketArray, filePath, slidingWindow.getRawDataSpace());

                //save file
                //todo savennnnn

                //print download is done
                print("Downloading " + fileName + " is finished.");
                kill();

            } else {
                byte[] buffer = packetWithOwnHeader.commandoSeven(processID, packetNumberSuccessive);
                DatagramPacket acknowledgePacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(acknowledgePacket);
            }
        }

    }

    public void kill(){
        networkUser.getProcessManager().stopSpecificProcess(processID);
    }

    public int getProcessID(){return processID;}

    public String getFileName(){return fileName;}

    public void setIsInterrupted(boolean b){
        isInterrupted = b;
    }

    private static void print (String message){
        System.out.println(message);
    }
}
