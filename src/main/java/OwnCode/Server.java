package OwnCode;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Server {
    private String[] fileonPI;
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
    private Client client;//todo dit is nu alleen omdat upload en download alleen met client kunen werken!!!!!!
    //todo
    //todo

    public Server(int port) throws SocketException {
        this.port = port;
        packetWithOwnHeader = new PacketWithOwnHeader();
        utils = new Utils();
        statistics = new Statistics();
        checksum = new Checksum();
        slidingWindow = new SlidingWindow();
        packetSize = slidingWindow.getPacketSize();
        connect();
    }

    public void connect(){
        try {
            socket = new DatagramSocket(port);
            while (true) { //receive

                //TODO: kijken of handshake nodig is
               /* DatagramPacket request = new DatagramPacket(new byte[1], 1, address, port);
                socket.send(request);
                */


                DatagramPacket receivePacket = new DatagramPacket(new byte[packetSize], packetSize);
                socket.receive(receivePacket);
                InetAddress destinationAddress = receivePacket.getAddress();
                int destinationPort = receivePacket.getPort();
                processManager = new ProcessManager(socket, destinationPort, destinationAddress);
                clientInputHandler(receivePacket);
            }
        } catch (SocketException e) {
            print("Timeout error: " + e.getMessage());
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public void clientInputHandler(DatagramPacket receivedPacket){
        DatagramPacket checkedPacket = checksum.checkingChecksum(receivedPacket);
        while(checkedPacket != null) {
            byte[] data = receivedPacket.getData();
            byte commandoByte = data[0]; //todo: kijken of dit klopt met checksum enzo
            byte byteProcessID = data[1];//todo: kijken of dit klopt
            byte bytePacketNumber = data[2];//todo same
            byte[] rawData = utils.removeHeader(data);
            int processID = utils.fromByteToInteger(byteProcessID);
            int packetNumber = utils.fromByteToInteger(bytePacketNumber);
            switch (utils.fromByteToInteger(commandoByte)) {

                case 1:                 requestSendFileNames();
                                        break;
                case 3:                 requestStartDownloadProcess(rawData, processID);
                                        break;
                case 4:                 requestStartUploadProcess(rawData, processID);
                                        break;
                case 6:                 processManager.receivePacketForProcess(processID, receivedPacket);
                                        break;
                case 7:                 processManager.receiveAcknowledgementPacketForProcess(processID, receivedPacket);
                                        break;
                case 8:                 processManager.receiveLastPacketForProcess(processID, receivedPacket);
                                        break;
                case 9:                 processManager.receiveAcknowledgementLastPacketForProcess(processID, receivedPacket);
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
        processManager.createDownloadProcessWithProcessID(fileName, filePath, client, processID);
        //todo client veranderen in this (server)

        byte[] buffer = packetWithOwnHeader.commandoFive(processID);
        DatagramPacket acknowlegement = new DatagramPacket(buffer, buffer.length);
        send(acknowlegement);
    }

    public void requestStartUploadProcess(byte[] rawData, int processID){
        String fileName = utils.fromByteArrToString(rawData);
        File file = new File(filePath+fileName);//todo: kijken of dit zo werkt
        processManager.createUploadProcessWithProcessID(file, client, processID);
        //todo client veranderen in this (server)
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
        for(int i = 0; i < fileonPI.length; i++){
            s = s + "+" + fileonPI[i];
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

    private static void print (String message){
        System.out.println(message);
    }

}

