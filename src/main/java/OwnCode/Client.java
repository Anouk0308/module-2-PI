package OwnCode;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Client implements NetworkUser, Runnable {
    private DatagramSocket socket;
    private int destinationPort;
    private int ownPort;
    private InetAddress ownAddress;
    private InetAddress destinationAddress;

    private Utils utils;
    private Statistics statistics;
    private Checksum checksum;
    private ProcessManager processManager;
    private UserInputHandler userInputHandler;
    private Receiver receiver;
    private SlidingWindow slidingWindow;
    private PacketWithOwnHeader packetWithOwnHeader;

    public Client(InetAddress destinationAddress, int destinationPort, int ownPort){
        this.destinationAddress = destinationAddress;
        this.destinationPort = destinationPort;
        this.ownPort = ownPort;

        utils = new Utils();
        statistics = new Statistics();
        checksum = new Checksum();
        slidingWindow = new SlidingWindow();
        packetWithOwnHeader = new PacketWithOwnHeader();
        processManager = new ProcessManager(this, slidingWindow);
        userInputHandler = new UserInputHandler(this, processManager, statistics);
    }

    @Override
    public void run() {
        connect();

        Thread userInputHandlerThread = new Thread(userInputHandler);
        userInputHandlerThread.start();
    }

    public void connect(){
        try {
            socket = new DatagramSocket(ownPort);
            receiver = new Receiver(socket, slidingWindow, this);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();

            /* broadcast
            byte[] buffer = packetWithOwnHeader.commandoZero();
            DatagramPacket handshake = new DatagramPacket(buffer, buffer.length);
            send(handshake);
            */

        } catch (SocketException e) {
            print("Timeout error: " + e.getMessage());
        }
    }

    public DatagramSocket getSocket(){ return socket;}

    public void inputHandler(DatagramPacket receivedPacketFromServer){//dealing with input from socket
        DatagramPacket checkedPacket = checksum.checkingChecksum(receivedPacketFromServer);
        if(checkedPacket != null){
            byte[] data = receivedPacketFromServer.getData();
            byte commandoByte = data[packetWithOwnHeader.commandoPosition];
            byte byteProcessID1 = data[packetWithOwnHeader.processIDPosition];
            byte byteProcessID2 = data[packetWithOwnHeader.processIDPosition+1];
            int processID = utils.limitBytesToInteger(byteProcessID1, byteProcessID2);


            switch (utils.fromByteToInteger(commandoByte)) {
                case 2:                 receivedFilesPI(data);
                                        break;
                case 5:                 processManager.receiveUploadAcknowledgement(processID);
                                        break;
                case 6:                 processManager.receivePacketForProcess(processID, receivedPacketFromServer);
                                        break;
                case 7:                 processManager.receiveAcknowledgementPacketForProcess(processID, receivedPacketFromServer);
                                        break;
                case 8:                 processManager.receiveLastPacketForProcess(processID, receivedPacketFromServer);
                                        break;
                case 9:                 processManager.receiveAcknowledgementLastPacketForProcess(processID);
                                        break;
                case 12:                userInputHandler.somethingElse();
                                        break;
                case 13:                receivedAckProcessPaused(processID);
                                        break;
                case 14:                receivedAckProcessContinued(processID);
                                        break;
                case 15:                receivedAckProcessStopped(processID);
                                        break;
                default:                print("Packet received without correct commando byte");
                                        break;
            }
        } else{
            statistics.foundCorruptedPacket();
        }
    }

    public void receivedFilesPI(byte[] data){
        byte[] rawData = utils.removeHeader(data);
        String filesString = utils.fromByteArrToString(rawData);
        String[] filesNameWithLenghtArr = filesString.split("\\+");
        String[] filesName = new String[filesNameWithLenghtArr.length];
        int[] filesLength = new int[filesNameWithLenghtArr.length];
        for(int i = 1; i < filesNameWithLenghtArr.length; i++){
            String tempString = filesNameWithLenghtArr[i];
            String[] temp = tempString.split(";");
            filesName[i] = temp[0];
            filesLength[i] = Integer.parseInt(temp[1]);
        }
        userInputHandler.setFilesPI(filesName, filesLength);
        userInputHandler.setUpdatedFilesPI(true);
    }

    public void receivedAckProcessPaused(int processID){
        print("Process " + processID + " is paused on the serverside as well");
    }

    public void receivedAckProcessContinued(int processID){
        print("Process " + processID + " is continued on the serverside as well");
    }

    public void receivedAckProcessStopped(int processID){
        print("Process " + processID + " is stopped on the serverside asw ell");
    }

    public void send(DatagramPacket p){
        byte[] buf = p.getData();
        int length = p.getLength();

        try {
            DatagramPacket packet = new DatagramPacket(buf, length, destinationAddress, destinationPort);
            socket.send(packet);
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public ProcessManager getProcessManager(){return processManager;}

    public Statistics getStatics(){return statistics;}

    public PacketWithOwnHeader getPacketWithOwnHeader(){return packetWithOwnHeader;}

    public void print (String message){
        System.out.println(message);
    }
}