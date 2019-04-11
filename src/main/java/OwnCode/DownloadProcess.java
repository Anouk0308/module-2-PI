package OwnCode;

import java.io.File;
import java.net.DatagramPacket;

public class DownloadProcess implements Process, Runnable{
    private int processID;
    private String fileName;
    private int bytesToLoad;//todo dit nog uitlezen

    private DatagramPacket[] downloadingPackets = new DatagramPacket[100000]; //todo, kijken hoe dit op te lossen

    private NetworkUser networkUser;
    private String filePath; // where are the files placed

    private SlidingWindow slidingWindow;
    private Utils utils;
    private PacketWithOwnHeader packetWithOwnHeader;

    public boolean isInterrupted = false;
    private boolean receivedAPacket = false; // for timer
    private boolean isClient;

    public DownloadProcess(int processID, String fileName, NetworkUser networkUser, String filePath, boolean isClient, SlidingWindow slidingWindow){
        this.processID = processID;
        this.fileName = fileName;
        this.networkUser = networkUser;
        this.filePath = filePath;
        this.slidingWindow = slidingWindow;
        utils = new Utils();
        packetWithOwnHeader = new PacketWithOwnHeader();
        this.isClient = isClient;
    }

    @Override
    public void run() {
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

        if(!isInterrupted){//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);
            print("com6, packetje ontvangen met packetnummer" + packetNumber);//todo weghalen
            downloadingPackets[packetNumber] = packet;

            int packetNumberSuccessive = -1;
            for(int i = 0; i < downloadingPackets.length; i++){
                if(downloadingPackets[i+1] == null){
                    packetNumberSuccessive = i;
                    break;
                }
            }
            print("packetnumber succesive:" + packetNumberSuccessive);//todo weghalen

            byte[] buffer = packetWithOwnHeader.commandoSeven(processID, packetNumberSuccessive);
            DatagramPacket acknowledgePacket = new DatagramPacket(buffer, buffer.length);
            networkUser.send(acknowledgePacket);
        }
    }

    public void receiveLastPacket(DatagramPacket packet){
        if(!isInterrupted) {//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);
            print("com8, packetje ontvangen met packetnummer" + packetNumber);//todo weghalen
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



                createFile();


                //save file
                //todo savennnnn

                //print download is done
                print("Downloading " + fileName + " is finished.");
                networkUser.getProcessManager().stopSpecificProcess(processID);

            } else {
                byte[] buffer = packetWithOwnHeader.commandoSeven(processID, packetNumberSuccessive);
                DatagramPacket acknowledgePacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(acknowledgePacket);
            }
        }

    }

    public void createFile(){
        for (int i = 0; i < downloadingPackets.length; i++) {
            if (downloadingPackets[i] != null) {
                System.out.println(downloadingPackets[i]);
            }
        }


        int newPacketsArrayLenght = downloadingPackets.length; //todo, als dowloadingpackets niet meer 1000000 is, hoeft dit allemaal niet
        for (int i = 0; i < downloadingPackets.length; i++) {
            if (downloadingPackets[i] == null) {
                newPacketsArrayLenght--;
            }
        }
        DatagramPacket[] newPacketArray = new DatagramPacket[newPacketsArrayLenght];
        System.arraycopy(downloadingPackets, 0, newPacketArray, 0, newPacketsArrayLenght);

        int offspring = 0;
        byte[] allBytesTogether = new byte[newPacketsArrayLenght * slidingWindow.getRawDataSpace()];
        for(int i = 0; i < newPacketsArrayLenght; i++){
            byte[] rawData = utils.removeHeader(newPacketArray[i].getData());
            System.arraycopy(rawData, 0, allBytesTogether, offspring, rawData.length);
            offspring = rawData.length;
        }

        byte[] originalFakeFile = new byte[3000];//todo dit is fake
        for(int i = 0; i < 3000; i++){
            originalFakeFile[i]= 2;
        }

        if(allBytesTogether.equals(originalFakeFile)){//todo weghalen
            print("zelfde packetje woop woop");
        } else{
            print("something went wrong, packetje is niet goed binnen gekomen");
        }





        // File file = utils.packetsToFile(newPacketArray, filePath, slidingWindow.getRawDataSpace());

    }

    public void kill(){
        networkUser.getStatics().stoppingProcess(processID, bytesToLoad);
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
