package OwnCode;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class Server implements NetworkUser, Runnable{
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
    private Receiver receiver;
    private int destinationPort;
    private InetAddress destinationAddress;

    public Server(int port) {
        print("Starting server");
        this.port = port;
        packetWithOwnHeader = new PacketWithOwnHeader();
        utils = new Utils();
        statistics = new Statistics();
        checksum = new Checksum();
        slidingWindow = new SlidingWindow();
        processManager = new ProcessManager(this, slidingWindow);

        filesOnPI = new String[2];
        filesOnPI[0] = "PIfile.java";
        filesOnPI[1] = "PItext.txt";
    }

    @Override
    public void run() {
        connect();
        print("Connected");
    }

    public void connect(){
        try {
            socket = new DatagramSocket(port);
            receiver = new Receiver(socket, slidingWindow, this);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();
        } catch (SocketException e) {
            print("SocketException: " + e.getMessage());
        }
    }

    public void inputHandler(DatagramPacket receivedPacketFromClient){
        print("packet in IH:" + receivedPacketFromClient.toString());//todo weghalen
        DatagramPacket checkedPacket = checksum.checkingChecksum(receivedPacketFromClient);
        print("checkedPacket in IH:" + checkedPacket.toString());//todo weghalen
        if(checkedPacket != null) {
            byte[] data = receivedPacketFromClient.getData();
            print("data lenght" + data.length);//todo weghalen
            byte commandoByte = data[1];
            int processID = 0;
            byte[] rawData = null;

            if(data.length >= 4){
                byte byteProcessID1 = data[2];
                byte byteProcessID2 = data[3];
                processID = utils.limitBytesToInteger(byteProcessID1, byteProcessID2);
            }

            if(data.length > 6){
                rawData = utils.removeHeader(data);
            }

            switch (utils.fromByteToInteger(commandoByte)) {

                case 0:                 handshake(receivedPacketFromClient);
                                        break;
                case 1:                 print("we zijn nu al bij case 1");//todo weghalen
                                        requestSendFileNames();
                                        break;
                case 2:                 print("verstuurd naar zichzelf");//todo weghalen
                                        break;//todo weghalen
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
    }

    public void handshake(DatagramPacket packet){
        destinationAddress = packet.getAddress();
        destinationPort = packet.getPort();
    }

    public void requestSendFileNames(){
        String files = filesToString();
        print("1");//todo weghalen
        byte[] bytes = utils.fromStringToByteArr(files);

        print("2"); //todo weghalen
        byte[] buffer = packetWithOwnHeader.commandoTwo(bytes);
        print("3");//todo weghalen
        DatagramPacket giveFiles = new DatagramPacket(buffer, buffer.length);
        print("4"); //todo weghalen
        send(giveFiles);
        print("5"); // todo weghalen
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
            print("packetje verzonden");//todo weghalen
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public ProcessManager getProcessManager(){return processManager;}

    public void print (String message){
        System.out.println("[PIVanAnouk]" + message);
    }

}

