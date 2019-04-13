package OwnCode;

import java.io.File;
import java.net.DatagramPacket;
import java.util.concurrent.locks.ReentrantLock;

public class FakeUploadProcess implements Process, Runnable{
    private int processID;
    private byte[] byteArrToLoad;
    private int numberOfBytesToLoad;//todo dit nog uitlezen
    private String fileName = "woopwoop.woop";
    private String fileNameAndNumberOfBytesToLoad;

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

    public FakeUploadProcess(int processID, byte[] byteArrToLoad, NetworkUser networkUser, boolean isClient, SlidingWindow slidingWindow, int numberOfBytesToLoad){
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
        this.fileNameAndNumberOfBytesToLoad = fileName + "+" + Integer.toString(numberOfBytesToLoad);
        this.numberOfBytesToLoad = numberOfBytesToLoad;
    }

    @Override
    public void run() {
        print("3");//todo weghalen
        if(isClient){
            handshake();
        }
        startProcess();
    }

    public void handshake(){
        print("fake upload handshake started");//todo weghalen
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
        print("received acknowledgement to start for process " + processID);//todo weghalen
        acknowledgementToStart = true;
    }

    public void startProcess(){
        print("start upload process");//todo weghalen

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


        } else{
            lock.lock();
            for(int i = 0; i < windowSize; i++) {
                DatagramPacket startPacket = uploadingPackets[i];
                networkUser.send(startPacket);
                print("packetje nummer " + i + " verzonden!!");//todo weghalen
            }
            lock.unlock();

            //set timer
            Utils.Timer timer = utils.new Timer(5000);
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

    int AckNumber = 0;//for counter
    int counter = 0;//for counter

    public void receiveAcknowledgementPacket(DatagramPacket packet){

        receivedAnAck = true;//for timer in startProcess()

        if(!isInterrupted) {//Can only receive packets when running/not interrupted
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);

            System.out.println("acknowledgement for number" + packetNumber);//todo weghalen

            //set counter
            //todo, als je dan een ackPacketnumber terug krijgt die groter is dan deze ackPacketnumber, stuur er dan weer aantal(windowsize) tegelijk, want anders blijf je na 1 keer packet loss maar windowsixe=1 sturen

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
                print("packetje nummer " + nextPacketNumber + " verzonden!!");//todo weghalen
            } else if (nextPacketNumber >= uploadingPackets.length - 1) { //last packet
                sendLastPacket();
            } //packetNumber that does not exist, does noet have to be send
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

    private void print (String message){
        networkUser.print(message);
    }
}
