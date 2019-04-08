package OwnCode;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UploadProcess {
    private int processID;
    private File file;
    private String fileName = file.getName();
    private int packetSize;
    private int windowSize;
    private DatagramPacket[] uploadingPackets;

    private Client client;
    private Server server;
    private SlidingWindow slidingWindow;
    private Utils utils;
    private PacketWithOwnHeader packetWithOwnHeader;

    private boolean acknowledgementToStart = false;
    private boolean acknowledgementToStop = false;

    private Thread thread;
    private boolean fromClient = true;
    private boolean handshakeSend = false;
    private boolean stop = false;

    public UploadProcess(int processID, File file, Client client){
        this.processID = processID;
        this.file = file;
        this.packetSize = slidingWindow.getPacketSize();
        this.windowSize = slidingWindow.getWindowSize();
        this.uploadingPackets = utils.fileToPackets(file);//todo ff kijken naar headers
        this.client = client;
        thread.start();
    }

    public UploadProcess(int processID, File file, Server server){
        this.processID = processID;
        this.file = file;
        this.packetSize = slidingWindow.getPacketSize();
        this.windowSize = slidingWindow.getWindowSize();
        this.uploadingPackets = utils.fileToPackets(file);//todo: nu ervan uitgegaan dat header hier al bij zit of iets slims bedenken met die headers toevoegen
        this.server = server;
        thread.start();
    }

    public Thread getThread(){return thread;}

    public void run() {
        while(!stop) {
            if (fromClient) {
                if(!handshakeSend){
                    handshakeProcess();
                }
            }
            startProcess();
            /*  rest is receiving packets and responding on them.
                these are handled in the receiveAcknowledgementPacket()
            */
            try{
                Thread.sleep(10);
            } catch(InterruptedException e){
                print(e.getMessage());
            }
        }
    }

    public void handshakeProcess(){
        byte[] buffer = packetWithOwnHeader.commandoThree(processID, fileName);
        DatagramPacket startPacket = new DatagramPacket(buffer, buffer.length);
        try {
            client.send(startPacket);

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

            int packetNumber = i;
            byte[] rawData = new byte[10]; //todo iets met utils en data krijgen


            byte[] buffer = packetWithOwnHeader.commandoSix(processID, packetNumber, rawData);
            DatagramPacket startPacket = new DatagramPacket(buffer, buffer.length);
            client.send(startPacket);
            //todo: kijken hoe dit te handelen, aangezien ook server kan zijn
        }
    }

    public void receiveAcknowledgementPacket(DatagramPacket packet){
        //todo: counter hierop. als je 5 keer zelfde acknowledgement krijgt, nextPacketNumber = packetNumber + 1
        //todo: timer erop. als je na x aantl secondes na startProcess() niks binnen krijgt, dan nog een keer start process;

        while(!Thread.currentThread().isInterrupted()) {//Can only receive packets when running/not interrupted
            int packetNumber = 1;//todo: uit header lezen welk packetje het is
            int nextPacketNumber = packetNumber + windowSize;
            if (nextPacketNumber < uploadingPackets.length - 1) { //not the last packet

                byte[] rawData = new byte[10]; //todo kijken hoe dit te krijgen met utils, en uploading packets
                byte[] buffer = packetWithOwnHeader.commandoSix(processID, nextPacketNumber, rawData);
                DatagramPacket startPacket = new DatagramPacket(buffer, buffer.length);
                client.send(startPacket);
                //todo: kijken hoe dit te handelen, aangezien ook server kan zijn

            } else if (nextPacketNumber == uploadingPackets.length - 1) { //last packet
                sendLastPacket();
            } //packetNumber that does not exist, does noet have to be send
        }
    }

    public void sendLastPacket(){
        int packetNumber = 1; //todo
        byte[] rawData = new byte[1]; //todo
        byte[] buffer = packetWithOwnHeader.commandoEight(processID, packetNumber, rawData);
        DatagramPacket lastPacket = new DatagramPacket(buffer, buffer.length);

        try {
            client.send(lastPacket);
            //todo: kijken hoe dit te handelen, aangezien ook server kan zijn

            while (!acknowledgementToStop) {//wait till PI tells that the uploading process can stop todo: timer erop. als je te lang moet wachten, doe dan nog een keer sendLastPacket()
                Thread.sleep(10);
            }
            print("Uploading " + file.getName() + " is finished.");
            kill();

        } catch (InterruptedException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public void setAcknowledgementToStopTrue(){
        acknowledgementToStop = true;
    }

    public void kill(){
        stop = true;
    }

    public int getProcessID(){return processID;}

    public String getFileName(){return fileName;}

    private static void print (String message){
        System.out.println(message);
    }
}
