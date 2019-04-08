package OwnCode;

import java.io.File;
import java.net.DatagramPacket;

public class DownloadProcess implements Runnable{
    private int processID;
    private String fileName;
    private int packetSize;
    private int windowSize;
    private DatagramPacket[] downloadingPackets = new DatagramPacket[100000]; //todo, kijken hoe dit op te lossen
    private File file;

    private Client client;
    private Server server;
    private String filePath; // where are the files placed

    private SlidingWindow slidingWindow;
    private Utils utils;
    private PacketWithOwnHeader packetWithOwnHeader;

    private boolean acknowledgementToStop = false;

    private Thread thread;
    private boolean fromClient = true;
    private boolean handshakeSend = false;
    private boolean stop = false;

    public DownloadProcess(int processID, String fileName, Client client, String filePath){
        this.processID = processID;
        this.fileName = fileName;
        this.client = client;
        this.packetSize = slidingWindow.getPacketSize();
        this.windowSize = slidingWindow.getWindowSize();
        this.filePath = filePath;
        thread.start();
    }

    public DownloadProcess(int processID, Server server, String filePath){
        this.processID = processID;
        this.server = server;
        this.packetSize = slidingWindow.getPacketSize();
        this.windowSize = slidingWindow.getWindowSize();
        this.filePath = filePath;
        fromClient = false;
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
            /*  rest is receiving packets and responding on them.
            these are handled in the receivePacket() & receiveLastPacket()
        */
            try{
                Thread.sleep(10);
            } catch(InterruptedException e){
                print(e.getMessage());
            }
        }
    }

    public void handshakeProcess(){
        byte[] buffer = packetWithOwnHeader.commandoFour(processID, fileName);
        DatagramPacket startPacket = new DatagramPacket(buffer, buffer.length);
        client.send(startPacket);
        print("Starting downloading process...");
        //todo timer. als niet snel genoeg packetjes binnen komen, stuur deze opnieuw
    }

    public void receivePacket(DatagramPacket packet){
        while(!Thread.currentThread().isInterrupted()){//Can only receive packets when running/not interrupted
            int packetNumber = 1;//todo: uit header lezen welk packetje het is
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
            client.send(acknowledgePacket);//todo server ook
        }
    }

    public void receiveLastPacket(DatagramPacket packet){
        while(!Thread.currentThread().isInterrupted()) {//Can only receive packets when running/not interrupted
            int packetNumber = 1;//todo: uit header lezen welk packetje het is
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
                client.send(acknowledgePacket);//todo server ook

                //create file from packetlist
                int newPacketsArrayLenght = downloadingPackets.length;
                for (int i = 0; i < downloadingPackets.length; i++) {
                    if (downloadingPackets[i] == null) {
                        newPacketsArrayLenght--;
                    }
                }
                DatagramPacket[] newPacketArray = new DatagramPacket[newPacketsArrayLenght];
                utils.packetsToFile(newPacketArray, filePath);

                //save file
                //todo

                //print download is done
                print("Downloading " + fileName + " is finished.");
                kill();

            } else {
                byte[] buffer = packetWithOwnHeader.commandoSeven(processID, packetNumberSuccessive);
                DatagramPacket acknowledgePacket = new DatagramPacket(buffer, buffer.length);
                client.send(acknowledgePacket);//todo server ook
            }
        }

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
