package OwnCode;

import java.io.File;
import java.net.DatagramPacket;

public class UploadProcess {
    private int processID;
    private File file;
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

    public boolean isInterrupted = false;
    private boolean receivedAnAck = false;

    public UploadProcess(int processID, File file, NetworkUser networkUser, boolean isClient){
        slidingWindow = new SlidingWindow();
        utils = new Utils();
        packetWithOwnHeader = new PacketWithOwnHeader();
        this.processID = processID;
        this.file = file;
        this.packetSize = slidingWindow.getPacketSize();
        this.windowSize = slidingWindow.getWindowSize();
        this.uploadingPackets = slidingWindow.slice(file,processID);
        this.networkUser = networkUser;
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
        for(int i = 0; i < windowSize; i++){
            DatagramPacket startPacket = uploadingPackets[i];
            networkUser.send(startPacket);
        }

        //set timer
        Utils.Timer timer = utils.new Timer(1000);
        try{
            while (!timer.isTooLate()) {
                Thread.sleep(10);
            }
            //timer went off
            if(receivedAnAck=false){//if there is still no acknowledgement packet received:
                startProcess();
            }

        } catch (InterruptedException e) {
            print("Client error: " + e.getMessage());
        }


    }

    int AckNumber = 0;//for counter
    int counter = 0;//for counter

    public void receiveAcknowledgementPacket(DatagramPacket packet){

        receivedAnAck = true;//for timer in startProcess()

        while(!isInterrupted) {//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[4], packetData[5]);

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
        DatagramPacket lastPacket = uploadingPackets[uploadingPackets.length-1];

        try {
            networkUser.send(lastPacket);

            //set timer
            Utils.Timer timer = utils.new Timer(1000);
            while(!timer.isTooLate()){//while timer didn't went of yet
                if (acknowledgementToStop) {
                    print("Uploading " + file.getName() + " is finished.");
                    kill();//process is killed, timer will not go off
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

    public void setAcknowledgementToStopTrue(){
        acknowledgementToStop = true;
    }

    public void kill(){
        networkUser.getProcessManager().stopSpecificProcess(processID);
    }

    public int getProcessID(){return processID;}

    public String getFileName(){return fileName;}

    private static void print (String message){
        System.out.println(message);
    }
}
