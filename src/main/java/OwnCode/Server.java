package OwnCode;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Server implements NetworkUser, Runnable{
    private static boolean isClient = false;
    private DatagramSocket socket;

    private static String folderPath = "/Users/anouk.schoenmakers/Desktop/ServerFiles/"; // where are the files placed //todo dit is voor computer
    private File fileFolder;
    private File[] filesOnPI;
    private String[] filesOnPINames;

    private PacketWithOwnHeader packetWithOwnHeader;
    private Utils utils;
    private Statistics statistics;
    private Checksum checksum;
    private ProcessManager processManager;
    private SlidingWindow slidingWindow;
    private Receiver receiver;
    private int destinationPort;
    private int ownPort;
    private InetAddress destinationAddress;

    public Server(InetAddress destinationAddress,int destinationPort, int ownPort) {
        print("Starting server");
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
        this.ownPort = ownPort;
        packetWithOwnHeader = new PacketWithOwnHeader();
        utils = new Utils();
        statistics = new Statistics();
        checksum = new Checksum();
        slidingWindow = new SlidingWindow();
        processManager = new ProcessManager(this, slidingWindow);

        fileFolder = new File(folderPath);
        filesOnPI = fileFolder.listFiles();
        filesOnPINames = new String[filesOnPI.length];
        if(filesOnPI.length>0){
            for(int i = 0; i < filesOnPI.length; i++){
                filesOnPINames[i]=filesOnPI[i].getName();
            }
        }

    }

    @Override
    public void run() {
        connect();
        print("Connected");
    }

    public void connect(){
        try {
            socket = new DatagramSocket(ownPort);
            receiver = new Receiver(socket, slidingWindow, this);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();
        } catch (SocketException e) {
            print("SocketException: " + e.getMessage());
        }
    }

    public void inputHandler(DatagramPacket receivedPacketFromClient){
        //DatagramPacket checkedPacket = checksum.checkingChecksum(receivedPacketFromClient);

        if(true) {//checkedPacket != null
            byte[] data = receivedPacketFromClient.getData();
            byte commandoByte = data[packetWithOwnHeader.commandoPosition];
            print("server received packet with commando: " + commandoByte );//todo weghalen
            int processID = 0;
            if(data.length > packetWithOwnHeader.processIDPosition){
                byte byteProcessID1 = data[packetWithOwnHeader.processIDPosition];
                byte byteProcessID2 = data[packetWithOwnHeader.processIDPosition+1];
                processID = utils.limitBytesToInteger(byteProcessID1, byteProcessID2);
            }
            byte[] rawData = utils.removeHeader(data);

            switch (utils.fromByteToInteger(commandoByte)) {

                case 100:               handshake(receivedPacketFromClient, rawData);
                                        break;
                case 1:                 requestSendFileNames();
                                        break;
                case 3:                 requestStartDownloadProcess(rawData, processID);
                                        break;
                case 4:                 requestStartUploadProcess(rawData, processID);
                                        break;
                case 6:                 processManager.receivePacketForProcess(processID, receivedPacketFromClient);
                                        break;
                case 7:                 processManager.receiveAcknowledgementPacketForProcess(processID, receivedPacketFromClient);
                                        break;
                case 8:                 processManager.receiveLastPacketForProcess(processID, receivedPacketFromClient);
                                        break;
                case 9:                 processManager.receiveAcknowledgementLastPacketForProcess(processID);
                                        break;
                case 10:                sendAckProcessPaused(processID);
                                        break;
                case 11:                sendAckProcessContinued(processID);
                                        break;
                case 12:                sendAckProcessStopped(processID);
                                        break;
                default:                print("Packet received without correct commando byte");
                                        break;
            }
        }
        else{
            statistics.foundCorruptedPacket();
        }
    }

    public void handshake(DatagramPacket packet, byte[] inetAddressBytes){
        print("handshake received");
       /*
        try{
            destinationAddress = InetAddress.getByAddress(inetAddressBytes);//todo voor PI
        }catch (UnknownHostException e){
            print(e.getMessage());
        }
        */
    }

    public void requestSendFileNames(){
        String files = filesToString();
        byte[] bytes = utils.fromStringToByteArr(files);
        byte[] buffer = packetWithOwnHeader.commandoTwo(bytes);
        DatagramPacket giveFiles = new DatagramPacket(buffer, buffer.length);
        send(giveFiles);
    }

    public void requestStartDownloadProcess(byte[] rawData, int processID){
        String fileNameAndNumberOfBytesToLoad = utils.fromByteArrToString(rawData);
        String[] stringArr = fileNameAndNumberOfBytesToLoad.split("\\+");
        String fileName = stringArr[0];
        int numberOfBytesToLoad = Integer.parseInt(stringArr[1]);
        processManager.createDownloadProcessWithProcessID(fileName, folderPath, this, processID, isClient, numberOfBytesToLoad);

        byte[] buffer = packetWithOwnHeader.commandoFive(processID);

        DatagramPacket acknowlegement = new DatagramPacket(buffer, buffer.length);
        send(acknowlegement);
    }

    public void requestStartUploadProcess(byte[] rawData, int processID){
        String fileNameAndNumberOfBytesToLoad = utils.fromByteArrToString(rawData);
        String[] stringArr = fileNameAndNumberOfBytesToLoad.split("\\+");
        String fileName = stringArr[0];
        int numberOfBytesToLoad = Integer.parseInt(stringArr[1]);
        String filePath = folderPath + "/" + fileName;
        File file = new File(filePath);


        processManager.createUploadProcess(file, this, isClient, numberOfBytesToLoad);//todo dit is fake


        /*
        File file = new File(filePath+fileName);//todo: kijken of dit zo werkt + dit is voor echte packetjes
        processManager.createUploadProcessWithProcessID(file, this, processID, isClient);
        */
    }

    public void sendAckProcessPaused(int processID){
        processManager.pauseSpecificProcess(processID);

        byte[] buffer = packetWithOwnHeader.commandoThirteen(processID);
        DatagramPacket acknowlegement = new DatagramPacket(buffer, buffer.length);
        send(acknowlegement);
    }

    public void sendAckProcessContinued(int processID){
        processManager.continueSpecificProcess(processID);

        byte[] buffer = packetWithOwnHeader.commandoFourteen(processID);
        DatagramPacket acknowlegement = new DatagramPacket(buffer, buffer.length);
        send(acknowlegement);
    }

    public void sendAckProcessStopped(int processID){
        if(processManager.containsProcess(processID)){
            processManager.stopSpecificProcess(processID);

            byte[] buffer = packetWithOwnHeader.commandoFiveteen(processID);
            DatagramPacket acknowlegement = new DatagramPacket(buffer, buffer.length);
            send(acknowlegement);
        }

    }



    public DatagramSocket getSocket(){ return socket;}

    public String filesToString(){
        String s = "";
        if(filesOnPINames.length == 0){
            s="There are no files on the PI";
        } else{
            for(int i = 0; i < filesOnPINames.length; i++){
                s = s + "+" + filesOnPINames[i];
            }
        }

        s = s + "+";
        return s;
    }

    public void send(DatagramPacket p){
        byte[] buf = p.getData();
        int length = p.getLength();
        try {
        DatagramPacket packet = new DatagramPacket(buf, length, destinationAddress, destinationPort);
        System.out.println(destinationAddress + "+"+destinationPort);//todo weghalen
        print("verzend nu packet met commando nummer:" + buf[packetWithOwnHeader.commandoPosition]);//todo weghalen


            socket.send(packet);
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public ProcessManager getProcessManager(){return processManager;}

    public Statistics getStatics(){return statistics;}

    public PacketWithOwnHeader getPacketWithOwnHeader(){return packetWithOwnHeader;}

    public void print (String message){
        System.out.println("[PIVanAnouk]" + message);
    }

}

