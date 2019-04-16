package OwnCode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;

public class DownloadProcess implements Process, Runnable{
    private int processID;
    private String fileName;
    private int numberOfBytesToLoad;
    private String fileNameAndNumberOfBytesToLoad;

    private DatagramPacket[] downloadingPackets = new DatagramPacket[100000]; //todo, kijken hoe dit op te lossen

    private NetworkUser networkUser;
    private String folderPath; // where are the files placed

    private Utils utils;
    private PacketWithOwnHeader packetWithOwnHeader;
    private FileOutputStream outputStream;

    public boolean isInterrupted = false;
    private boolean receivedAPacket = false; // for timer
    private boolean isClient;

    public DownloadProcess(int processID, String fileName, NetworkUser networkUser, String folderPath, boolean isClient, int numberOfBytesToLoad){
        this.processID = processID;
        this.fileName = fileName;
        this.networkUser = networkUser;
        this.folderPath = folderPath;
        utils = new Utils();
        packetWithOwnHeader = new PacketWithOwnHeader();
        this.isClient = isClient;
        this.numberOfBytesToLoad = numberOfBytesToLoad;
        this.fileNameAndNumberOfBytesToLoad = fileName + "+" + numberOfBytesToLoad;

        String filePath = folderPath + "/" + fileName;
        try{
            outputStream = new FileOutputStream(filePath);
        } catch (IOException e){
            print(e.getMessage());
        }
    }

    @Override
    public void run() {
        if(isClient){
            handshake();
        }//when server, wait for Client to send packets
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

    int packetNumberSuccessiveFirst = -1;//-1, as 0 is a number that can be used for packetNumbers
    int packetNumberSuccessiveNow = -1;
    public void receivePacket(DatagramPacket packet){
        receivedAPacket = true;//for timer in handshake()

        for(int i = 0; i < downloadingPackets.length; i++){
            if(downloadingPackets[i+1] == null){
                packetNumberSuccessiveFirst = i;
                break;
            }
        }

        if(!isInterrupted){//Can only receive packets when running (thread.pause/thread.resume are deprecated)
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);
            downloadingPackets[packetNumber] = packet;

            for(int i = 0; i < downloadingPackets.length; i++){
                if(downloadingPackets[i+1] == null){
                    packetNumberSuccessiveNow = i;
                    break;
                }
            }

            if(packetNumberSuccessiveNow > packetNumberSuccessiveFirst){

                try{
                    if(packetNumberSuccessiveNow == 0){
                        outputStream.write(utils.removeHeader(downloadingPackets[0].getData()));
                    } else {
                        for (int i = packetNumberSuccessiveFirst; i < packetNumberSuccessiveFirst + (packetNumberSuccessiveNow - packetNumberSuccessiveFirst); i++) {
                            outputStream.write(utils.removeHeader(downloadingPackets[i].getData()));
                        }
                    }
                } catch (IOException e){
                    print(e.getMessage());
                }
            }

            byte[] buffer = packetWithOwnHeader.commandoSeven(processID, packetNumberSuccessiveNow);
            DatagramPacket acknowledgePacket = new DatagramPacket(buffer, buffer.length);
            networkUser.send(acknowledgePacket);
        }
    }

    int packetNumberSuccessive = -1;//-1, a 0 is a number that can be used for packetNumbers
    public void receiveLastPacket(DatagramPacket packet){
        if(!isInterrupted) {//Can only receive packets when running/not interrupted (thread.pause/thread.resume are deprecated)
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);
            downloadingPackets[packetNumber] = packet;

            for (int i = 0; i < downloadingPackets.length; i++) {
                if (downloadingPackets[i + 1] == null) {
                    packetNumberSuccessive = i;
                    break;
                }
            }

            if (packetNumberSuccessive == packetNumber) {//everything is received
                try{
                    outputStream.write(utils.removeHeader(downloadingPackets[packetNumber-1].getData()));
                    outputStream.write(utils.removeHeader(downloadingPackets[packetNumber].getData()));
                } catch (IOException e){
                    print(e.getMessage());
                }

                //tell the other that everything is received
                byte[] buffer = packetWithOwnHeader.commandoNine(processID, packetNumberSuccessive);
                DatagramPacket acknowledgeLastPacket = new DatagramPacket(buffer, buffer.length);
                print(Arrays.toString(acknowledgeLastPacket.getData()));
                networkUser.send(acknowledgeLastPacket);
                //todo timer. als ander niet commando 9 binnen krijgt, dan moet je die nog ene keer sturen

                //stop procces self
                networkUser.getProcessManager().stopSpecificProcess(processID);

            } else {
                byte[] buffer = packetWithOwnHeader.commandoSeven(processID, packetNumberSuccessive);
                DatagramPacket acknowledgePacket = new DatagramPacket(buffer, buffer.length);
                networkUser.send(acknowledgePacket);
            }
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
