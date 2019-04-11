package OwnCode;

import java.io.File;
import java.net.DatagramPacket;

public class FakeUploadProcess implements Process, Runnable{
    private int processID;
    private byte[] byteArrToLoad;
    private int bytesToLoad;//todo dit nog uitlezen
    private String fileName = "woopwoop.woop";

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

    public FakeUploadProcess(int processID, byte[] byteArrToLoad, NetworkUser networkUser, boolean isClient, SlidingWindow slidingWindow){
        this.networkUser = networkUser;
        this.slidingWindow = slidingWindow;
        utils = new Utils();
        packetWithOwnHeader = new PacketWithOwnHeader();
        this.processID = processID;
        this.byteArrToLoad = byteArrToLoad;
        this.packetSize = slidingWindow.getPacketSize();
        this.windowSize = slidingWindow.getWindowSize();
        this.uploadingPackets = slidingWindow.fakeSlice(byteArrToLoad,processID);
        this.isClient = isClient;
        print("fakeUpload initiated");//todo weghalen
    }

    @Override
    public void run() {
        if(isClient){
            handshake();
        }
        startProcess();
    }

    public void handshake(){
        print("fake upload handshake started");//todo weghalen
        byte[] buffer = packetWithOwnHeader.commandoThree(processID, fileName);
        DatagramPacket startPacket = new DatagramPacket(buffer, buffer.length);
        try {
            networkUser.send(startPacket);

            print("waiting on server");//todo weghalen
            while (!acknowledgementToStart) {//wait till PI tells that the uploading process can start
                Thread.sleep(10);
            }
            print("Starting upload process " + processID);

        } catch (InterruptedException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public void setAcknowledgementToStartTrue(){
        print("received acknowledgement to start for process " + processID);//todo weghalen
        acknowledgementToStart = true;
    }

    public void startProcess(){
        print("fake upload startprocess started");//todo weghalen
        for(int i = 0; i < uploadingPackets.length; i++){
            System.out.println(uploadingPackets[i]);
        }

        //todo nu hier :D
        //todo uploadingPackets lijkt niks te bevatten

        //send first packets
        if(uploadingPackets.length < windowSize){
            for(int i = 0; i < uploadingPackets.length-1; i++){
                DatagramPacket startPacket = uploadingPackets[i];
                networkUser.send(startPacket);
                print("packetje verzonden!!");//todo weghalen
            }
            sendLastPacket();
        } else{
            for(int i = 0; i < windowSize; i++){
                DatagramPacket startPacket = uploadingPackets[i];
                networkUser.send(startPacket);

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
        }

    }

    int AckNumber = 0;//for counter
    int counter = 0;//for counter

    public void receiveAcknowledgementPacket(DatagramPacket packet){

        receivedAnAck = true;//for timer in startProcess()

        while(!isInterrupted) {//Can only receive packets when running/not interrupted
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
        DatagramPacket lastPacket = uploadingPackets[uploadingPackets.length-1];
        System.out.println(lastPacket);//todo weghalen

        try {
            networkUser.send(lastPacket);
            print("laatste packetje verzonden");//todo weghalen

            //set timer
            Utils.Timer timer = utils.new Timer(1000);
            while(!timer.isTooLate()){//while timer didn't went of yet
                if (acknowledgementToStop) {
                    print("Uploading " + fileName + " is finished.");
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
        networkUser.getStatics().stoppingProcess(processID, bytesToLoad);
        networkUser.getProcessManager().stopSpecificProcess(processID);
    }

    public int getProcessID(){return processID;}

    public String getFileName(){return fileName;}

    public void setIsInterrupted(boolean b){
        isInterrupted = b;
    }

    private void print (String message){
        networkUser.print(message);
    }
}
