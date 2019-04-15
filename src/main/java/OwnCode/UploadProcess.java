package OwnCode;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantLock;

public class UploadProcess implements Process, Runnable {
    private int processID;
    private File file;
    private int numberOfBytesToLoad;
    private String fileName;
    private String fileNameAndNumberOfBytesToLoad;
    private byte[] byteArrToLoad;

    private int packetSize;
    private int windowSize;
    private DatagramPacket[] uploadingPackets;

    private NetworkUser networkUser;
    private SlidingWindow slidingWindow;
    private Utils utils;
    private PacketWithOwnHeader packetWithOwnHeader;
    private ReentrantLock lock = new ReentrantLock();

    private boolean acknowledgementToStart = false;
    private boolean acknowledgementToStop = false;
    private boolean isClient;

    public boolean isInterrupted = false;
    private boolean receivedAnAck = false; //is for timer

    public UploadProcess(int processID, File file, NetworkUser networkUser, boolean isClient, SlidingWindow slidingWindow, int numberOfBytesToLoad){
        this.processID = processID;

        this.file = file;
        this.fileName = file.getName();
        try {
            this.byteArrToLoad = Files.readAllBytes(file.toPath());
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        this.networkUser = networkUser;
        this.isClient = isClient;
        this.slidingWindow = slidingWindow;
        this.numberOfBytesToLoad = numberOfBytesToLoad;
        this.fileNameAndNumberOfBytesToLoad = fileName + "+" + Integer.toString(numberOfBytesToLoad);

        packetWithOwnHeader = new PacketWithOwnHeader();
        utils = new Utils();
        packetSize = slidingWindow.getPacketSize();
        windowSize = slidingWindow.getWindowSize();
        uploadingPackets = slidingWindow.slice(byteArrToLoad,processID);
    }

    @Override
    public void run() {
        if(isClient){
            handshake();
        }
        startProcess();
    }

    public void handshake(){
        byte[] buffer = packetWithOwnHeader.commandoThree(processID, fileNameAndNumberOfBytesToLoad);
        DatagramPacket startPacket = new DatagramPacket(buffer, buffer.length);
        try {
            networkUser.send(startPacket);

            print("waiting on server");
            while (!acknowledgementToStart) {//wait till PI tells that the uploading process can start
                Thread.sleep(10);
            }

        } catch (InterruptedException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public void setAcknowledgementToStartTrue(){
        acknowledgementToStart = true;
    }

    public void startProcess(){

        //send first packets
        if(uploadingPackets.length < windowSize){
            lock.lock();
            for(int i = 0; i < uploadingPackets.length-1; i++){
                DatagramPacket startPacket = uploadingPackets[i];
                networkUser.send(startPacket);
                print("packetje nummer " + i + " verzonden!!");//todo weghalen
            }
            sendLastPacket();
            lock.unlock();
        } else {
            lock.lock();
            for (int i = 0; i < windowSize; i++) {
                DatagramPacket startPacket = uploadingPackets[i];
                networkUser.send(startPacket);
                print("packetje nummer " + i + " verzonden!!");//todo weghalen
            }
            lock.unlock();

            //set timer
            Utils.Timer timer = utils.new Timer(5000);
            try {
                while (!timer.isTooLate()) {
                    Thread.sleep(10);
                }
                //timer went off
                if (receivedAnAck = false) {//if there is still no acknowledgement packet received:
                    startProcess();
                }

            } catch (InterruptedException e) {
                print("Client error: " + e.getMessage());
            }
        }
    }

    public void receiveAcknowledgementPacket(DatagramPacket packet){
        int internalAckNumber = 0;//for counter, to check if the incoming acknowledgement numbers will increase
        int counter = 0;//for counter
        receivedAnAck = true;//for timer in startProcess()
        int checkingRetransmissonNumber = 1000000000;//checking number to see that the restransmission went well

        if(!isInterrupted) {//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();
            int ackPacketNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);

            //set counter
            if(ackPacketNumber != internalAckNumber){
                internalAckNumber = ackPacketNumber;
                counter = 0;
            } else{ // already seen this acknowledgement packet
                counter++;
            }

            if(ackPacketNumber < checkingRetransmissonNumber){
                int nextPacketNumber;
                if(counter >= 3){//when received 3 times the same acknowledgementPacket, resend packet after this acknowledgementPacket
                    nextPacketNumber = ackPacketNumber + 1;
                    checkingRetransmissonNumber = ackPacketNumber;//

                } else {
                    nextPacketNumber = ackPacketNumber + windowSize;
                }

                if (nextPacketNumber < uploadingPackets.length - 1) { //not the last packet
                    DatagramPacket nextPacket = uploadingPackets[nextPacketNumber];
                    networkUser.send(nextPacket);
                    print("packetje nummer " + nextPacketNumber + " verzonden!!");//todo weghalen
                } else if (nextPacketNumber == uploadingPackets.length - 1) { //last packet
                    sendLastPacket();
                }//packetNumber greater than the lastPacketNumber does not exist, does not have to be send
            } else{ //ackPacketNumber is greater than last ackPacketNumber, which means that the retransmissioned succeeded
                checkingRetransmissonNumber = 1000000000;
                for(int i = ackPacketNumber+1; i < ackPacketNumber+windowSize; i++){
                    DatagramPacket nextPacket = uploadingPackets[i];
                    networkUser.send(nextPacket);
                    print("packetje nummer " + i + " verzonden!!");//todo weghalen
                }
            }
        }
    }

    public void sendLastPacket(){
        if(!acknowledgementToStop){
            DatagramPacket lastPacket = uploadingPackets[uploadingPackets.length-1];
            networkUser.send(lastPacket);
            print("laatste packetje verzonden");//todo weghalen

            //todo bedenken wat te doen als dit packet niet aan komt. hier een timer zetten blokkeert receiver
        }
    }

    public void setAcknowledgementToStopTrue(){
        acknowledgementToStop = true;
        print("Uploading " + fileName + " is finished.");
        networkUser.getProcessManager().stopSpecificProcess(processID);
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
