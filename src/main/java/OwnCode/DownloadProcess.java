package OwnCode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.util.Arrays;

public class DownloadProcess implements Process, Runnable{
    private int processID;
    private String fileName;
    private int numberOfBytesToLoad;//todo dit nog uitlezen
    private String fileNameAndNumberOfBytesToLoad;

    private DatagramPacket[] downloadingPackets = new DatagramPacket[100000]; //todo, kijken hoe dit op te lossen

    private NetworkUser networkUser;
    private String folderPath; // where are the files placed

    private SlidingWindow slidingWindow;
    private Utils utils;
    private PacketWithOwnHeader packetWithOwnHeader;

    public boolean isInterrupted = false;
    private boolean receivedAPacket = false; // for timer
    private boolean isClient;

    public DownloadProcess(int processID, String fileName, NetworkUser networkUser, String folderPath, boolean isClient, SlidingWindow slidingWindow, int numberOfBytesToLoad){
        this.processID = processID;
        this.fileName = fileName;
        this.networkUser = networkUser;
        this.folderPath = folderPath;
        this.slidingWindow = slidingWindow;
        utils = new Utils();
        packetWithOwnHeader = new PacketWithOwnHeader();
        this.isClient = isClient;
        this.numberOfBytesToLoad = numberOfBytesToLoad;
        this.fileNameAndNumberOfBytesToLoad = fileName + "+" + Integer.toString(numberOfBytesToLoad);
    }

    @Override
    public void run() {
        if(isClient){
            handshake();
        }
    }


    public void handshake(){
        byte[] buffer = packetWithOwnHeader.commandoFour(processID, fileNameAndNumberOfBytesToLoad);
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
            if(!receivedAPacket){//if there is still no packet received:
                handshake();
            }

        } catch (InterruptedException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public void receivePacket(DatagramPacket packet){
        receivedAPacket = true;//for timer in handshake()

        if(!isInterrupted){//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);
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
        if(!isInterrupted) {//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();

            int packetNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);

            downloadingPackets[packetNumber] = packet;

            int packetNumberSuccessive = -1;
            for (int i = 0; i < downloadingPackets.length; i++) {
                if (downloadingPackets[i + 1] == null) {
                    packetNumberSuccessive = i;
                    break;
                }
            }

            if (packetNumberSuccessive == packetNumber) {//everything is received
                //todo timer. als ander niet commando 9 binnen krijgt, dan moet je die nog ene keer sturen
                createFile();

                //tell the other that everything is received
                byte[] buffer = packetWithOwnHeader.commandoNine(processID, packetNumberSuccessive);
                DatagramPacket acknowledgeLastPacket = new DatagramPacket(buffer, buffer.length);
                print(Arrays.toString(acknowledgeLastPacket.getData()));
                networkUser.send(acknowledgeLastPacket);

                //stop procces self
                networkUser.getProcessManager().stopSpecificProcess(processID);

            } else {
                byte[] buffer = packetWithOwnHeader.commandoSeven(processID, packetNumberSuccessive);
                DatagramPacket acknowledgePacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(acknowledgePacket);
            }
        }
    }

    public void createFile(){
        System.out.println("Creating the file, wait a moment please");

        int newPacketsArrayLenght = downloadingPackets.length; //todo, als dowloadingpackets niet meer 1000000 is, hoeft dit allemaal niet
        for (int i = 0; i < downloadingPackets.length; i++) {
            if (downloadingPackets[i] == null) {
                newPacketsArrayLenght--;
            }
        }
        DatagramPacket[] newPacketArray = new DatagramPacket[newPacketsArrayLenght];
        System.arraycopy(downloadingPackets, 0, newPacketArray, 0, newPacketsArrayLenght);

        byte[] allBytesTogether = new byte[0];

        for(int i = 0; i < newPacketsArrayLenght; i++){
            byte[] rawData = utils.removeHeader(newPacketArray[i].getData());
            allBytesTogether = utils.combineByteArr(allBytesTogether, rawData);
        }

        /*
        byte[] originalFakeFile = new byte[3000];//todo dit is fake
        for(int i = 0; i < originalFakeFile.length; i++){
            originalFakeFile[i]= 2;
        }

        int bytesSame = 0;
        for(int i = 0; i <allBytesTogether.length; i++){//todo weghalen
            if(allBytesTogether[i] == originalFakeFile[i]){
                bytesSame++;
            }
        }

        if(bytesSame == allBytesTogether.length){//todo weghalen
            print("zelfde packetje woop woop");
        } else{
            print("something went wrong, packetje is niet goed binnen gekomen");
        }
        */

        String filePath = folderPath + "/" + fileName;
        File file = new File(filePath);

        try{
            OutputStream os = new FileOutputStream(file);
            os.write(allBytesTogether);
            print("file saved");
        } catch (IOException e){
            print(e.getMessage());
        }
    }


    public void kill(){
        networkUser.getStatics().stoppingProcess(processID, numberOfBytesToLoad);
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
