package OwnCode;

import java.io.File;
import java.net.DatagramPacket;

public class UploadProcess implements Process, Runnable {
    private int processID;
    private File file;
    private int bytesToLoad;//todo dit nog uitlezen
    private String fileName = file.getName();

    private int packetSize;
    private int windowSize;
    private DatagramPacket[] uploadingPackets;

    private NetworkUser networkUser;
    private SlidingWindow slidingWindow;
    private Utils utils;
    private PacketWithOwnHeader packetWithOwnHeader;

    private boolean acknowledgementToStart = false;
    private boolean acknowledgementToStop = false;
    private boolean isClient;

    public boolean isInterrupted = false;
    private boolean receivedAnAck = false; //is for timer

    public UploadProcess(int processID, File file, NetworkUser networkUser, boolean isClient, SlidingWindow slidingWindow){
        this.slidingWindow = slidingWindow;
        packetWithOwnHeader = new PacketWithOwnHeader();
        this.processID = processID;
        this.file = file;
        this.packetSize = slidingWindow.getPacketSize();
        this.windowSize = slidingWindow.getWindowSize();
        this.uploadingPackets = slidingWindow.slice(file,processID);
        this.networkUser = networkUser;
        utils = new Utils();
    }

    @Override
    public void run() {
        if(isClient){
            handshake();
        }
        startProcess();
    }

    public void handshake(){
        byte[] buffer = packetWithOwnHeader.commandoThree(processID, fileName);
        DatagramPacket startPacket = new DatagramPacket(buffer, buffer.length);
        try {
            networkUser.send(startPacket);

            while (!acknowledgementToStart) {//wait till PI tells that the uploading process can start
                Thread.sleep(10);
            }
            print("Starting upload process " + processID);

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
            for(int i = 0; i < uploadingPackets.length-1; i++){
                DatagramPacket startPacket = uploadingPackets[i];
                networkUser.send(startPacket);
                print("packetje verzonden!!");//todo weghalen
            }
            sendLastPacket();
        } else {
            for (int i = 0; i < windowSize; i++) {
                DatagramPacket startPacket = uploadingPackets[i];
                networkUser.send(startPacket);
            }

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

    int AckNumber = 0;//for counter
    int counter = 0;//for counter

    public void receiveAcknowledgementPacket(DatagramPacket packet){

        receivedAnAck = true;//for timer in startProcess()

        if(!isInterrupted) {//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);

            //set counter
            if(packetNumber != AckNumber){
                AckNumber = packetNumber;
                counter = 0;
            } else{ // already seen this acknowledgement packet
                counter++;
            }

            int nextPacketNumber;
            if(counter >= 5){//when received 5 times the same acknowledgementPacket, send packet after this acknowledgementPacket
                nextPacketNumber = packetNumber + 1;
            } else {
                nextPacketNumber = packetNumber + windowSize;
            }

            if (nextPacketNumber < uploadingPackets.length - 1) { //not the last packet
                DatagramPacket nextPacket = uploadingPackets[nextPacketNumber];
                networkUser.send(nextPacket);
            } else if (nextPacketNumber == uploadingPackets.length - 1) { //last packet
                sendLastPacket();
            } //packetNumber that does not exist, does noet have to be send
        }
    }

    public void sendLastPacket(){
        if(!acknowledgementToStop) {
            DatagramPacket lastPacket = uploadingPackets[uploadingPackets.length - 1];

            try {
                networkUser.send(lastPacket);

                //set timer
                Utils.Timer timer = utils.new Timer(1000);
                while (!timer.isTooLate()) {//while timer didn't went of yet
                    if (acknowledgementToStop) {
                        print("Uploading " + file.getName() + " is finished.");
                        networkUser.getProcessManager().stopSpecificProcess(processID);
                    } else { //wait till PI tells that the uploading process can stop
                        Thread.sleep(10);
                    }
                }
                //timer went off, still no acknowledgement to stop
                sendLastPacket();
            } catch (InterruptedException e) {
                print("Client error: " + e.getMessage());
            }
        }
    }

    public void setAcknowledgementToStopTrue(){
        acknowledgementToStop = true;
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
