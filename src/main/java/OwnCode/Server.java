package OwnCode;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server implements NetworkUser {
    private static boolean isClient = false;
    private String[] filesOnPI;
    private DatagramSocket socket;
    private int port;
    private String filePath = "pi/src/"; // where are the files placed
    private PacketWithOwnHeader packetWithOwnHeader;
    private Utils utils;
    private Statistics statistics;
    private Checksum checksum;
    private ProcessManager processManager;
    private SlidingWindow slidingWindow;
    private int packetSize;
    private InetAddress destinationAddress;
    private int destinationPort;

    public Server(int port) {
        this.port = port;
        packetWithOwnHeader = new PacketWithOwnHeader();
        utils = new Utils();
        statistics = new Statistics();
        checksum = new Checksum();
        slidingWindow = new SlidingWindow();
        packetSize = slidingWindow.getPacketSize();

        filesOnPI = new String[2];
        filesOnPI[0] = "PIfile.java";
        filesOnPI[1] = "PItext.txt";

        connect();
    }

    public void connect(){
        try {
            socket = new DatagramSocket(port);
            while (true) { //receive
                DatagramPacket receivePacket = new DatagramPacket(new byte[packetSize], packetSize);
                socket.receive(receivePacket);
                InetAddress destinationAddress = receivePacket.getAddress();
                int destinationPort = receivePacket.getPort();
                processManager = new ProcessManager(this);
                inputHandler(receivePacket);
            }
        } catch (SocketException e) {
            print("Timeout error: " + e.getMessage());
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public void inputHandler(DatagramPacket receivedPacketFromClient){
        DatagramPacket checkedPacket = checksum.checkingChecksum(receivedPacketFromClient);
        while(checkedPacket != null) {
            byte[] data = receivedPacketFromClient.getData();
            byte commandoByte = data[1];
            byte byteProcessID1 = data[2];
            byte byteProcessID2 = data[3];
            byte bytePacketNumber1 = data[4];
            byte bytePacketNumber2 = data[5];
            byte[] rawData = utils.removeHeader(data);
            int processID = utils.limitBytesToInteger(byteProcessID1, byteProcessID2);
            int packetNumber = utils.limitBytesToInteger(bytePacketNumber1, bytePacketNumber2);
            switch (utils.fromByteToInteger(commandoByte)) {

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
                case 9:                 processManager.receiveAcknowledgementLastPacketForProcess(processID, receivedPacketFromClient);
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
    }

    public void requestSendFileNames(){
        String files = filesToString();
        byte[] bytes = utils.fromStringToByteArr(files);

        byte[] buffer = packetWithOwnHeader.commandoTwo(bytes);
        DatagramPacket giveFiles = new DatagramPacket(buffer, buffer.length);
        send(giveFiles);
    }

    public void requestStartDownloadProcess(byte[] rawData, int processID){
        String fileName = utils.fromByteArrToString(rawData);
        processManager.createDownloadProcessWithProcessID(fileName, filePath, this, processID, isClient);

        byte[] buffer = packetWithOwnHeader.commandoFive(processID);
        DatagramPacket acknowlegement = new DatagramPacket(buffer, buffer.length);
        send(acknowlegement);
    }

    public void requestStartUploadProcess(byte[] rawData, int processID){
        String fileName = utils.fromByteArrToString(rawData);
        File file = new File(filePath+fileName);//todo: kijken of dit zo werkt
        processManager.createUploadProcessWithProcessID(file, this, processID, isClient);
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
        processManager.stopSpecificProcess(processID);

        byte[] buffer = packetWithOwnHeader.commandoFiveteen(processID);
        DatagramPacket acknowlegement = new DatagramPacket(buffer, buffer.length);
        send(acknowlegement);
    }



    public DatagramSocket getSocket(){ return socket;}

    public String filesToString(){
        String s = "";
        for(int i = 0; i < filesOnPI.length; i++){
            s = s + "+" + filesOnPI[i];
        }
        return s;
    }

    public void send(DatagramPacket p){
        byte[] buf = p.getData();
        int lenght = p.getLength();
        DatagramPacket packet = new DatagramPacket(buf, lenght, destinationAddress, destinationPort);

        try {
            socket.send(packet);
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public ProcessManager getProcessManager(){return processManager;}

    private static void print (String message){
        System.out.println(message);
    }

}

