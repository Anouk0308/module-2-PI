package OwnCode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.Map;

public class DownloadProcess implements Process, Runnable{
    private int processID;
    private String fileName;
    private int numberOfBytesToLoad;
    private String fileNameAndNumberOfBytesToLoad;

    private Map<Integer, DatagramPacket> downloadingPackets; //todo, kijken hoe dit op te lossen

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
        downloadingPackets = new HashMap<>();

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

        for(int i = 0; i < downloadingPackets.size(); i++){
            if(!downloadingPackets.containsKey(i+1)){//if their is no successive packet
                packetNumberSuccessiveFirst = i;
                break;
            }
        }

        if(!isInterrupted){//Can only receive packets when running (thread.pause/thread.resume are deprecated)
            byte[] packetData = packet.getData();
            int packetNumber = utils.limitBytesToInteger(packetData[packetWithOwnHeader.packetNumberPosition], packetData[packetWithOwnHeader.packetNumberPosition+1]);
            downloadingPackets.put(packetNumber, packet);

            for(int i = 0; i < downloadingPackets.size(); i++){
                if(!downloadingPackets.containsKey(i+1)){//if their is no successive packet
                    packetNumberSuccessiveNow = i;
                    break;
                }
            }

            System.out.println("packet with number"+ packetNumber);//todo weghalen
            System.out.println("successive now"+packetNumberSuccessiveNow + ">succesive first"+packetNumberSuccessiveFirst);//todo weghalen
            if(packetNumberSuccessiveNow > packetNumberSuccessiveFirst){

                try{
                    if(packetNumberSuccessiveNow == 0){
                        outputStream.write(utils.removeHeader(downloadingPackets.get(0).getData()));
                        System.out.println("Schrijf nu packetjeeee"+ downloadingPackets.get(0)+"met packetnummer"+0);//todo weghalen
                    } else {
                        for (int i = packetNumberSuccessiveFirst; i < packetNumberSuccessiveFirst + (packetNumberSuccessiveNow - packetNumberSuccessiveFirst); i++) {
                            if(i>0){//packet with number 0 is already written
                                outputStream.write(utils.removeHeader(downloadingPackets.get(i).getData()));
                                System.out.println("Schrijf nu packetje"+ downloadingPackets.get(i)+"met packetnummer"+i);//todo weghalen
                            }
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
            downloadingPackets.put(packetNumber, packet);

            for(int i = 0; i < downloadingPackets.size(); i++){
                if(!downloadingPackets.containsKey(i+1)){//if their is no successive packet
                    packetNumberSuccessive = i;
                    break;
                }
            }

            if (packetNumberSuccessive == packetNumber) {//everything is received
                try{
                    outputStream.write(utils.removeHeader(downloadingPackets.get(packetNumber-1).getData()));
                    outputStream.write(utils.removeHeader(downloadingPackets.get(packetNumber).getData()));
                } catch (IOException e){
                    print(e.getMessage());
                }

                //tell the other that everything is received
                byte[] buffer = packetWithOwnHeader.commandoNine(processID, packetNumberSuccessive);
                DatagramPacket acknowledgeLastPacket = new DatagramPacket(buffer, buffer.length);
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
