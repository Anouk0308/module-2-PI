package OwnCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Arrays;

public class Client implements NetworkUser {
    private static String IPAddress = "127.16.1.1";
    private static int Port = 8000;

    private DatagramSocket socket;
    private String destinationIPAdress;
    private int destinationPort;
    private InetAddress destinationAddress;


    private Utils utils;
    private Statistics statistics;
    private Checksum checksum;
    private ProcessManager processManager;
    private UserInputHandler userInputHandler;

    public static void main(String[] args) {
        Client client = new Client(IPAddress, Port);
    }

    public Client(String destinationIPAdress, int destinationPort){
        this.destinationIPAdress = destinationIPAdress;
        this.destinationPort = destinationPort;
        //todo: vul FilesClient aan met de namen in daadwerkelijke map

        utils = new Utils();
        statistics = new Statistics();
        checksum = new Checksum();
        processManager = new ProcessManager(this);

        userInputHandler = new UserInputHandler(this, processManager, statistics);
        connect();
    }

    public void connect(){
        try {
            destinationAddress = InetAddress.getByName(destinationIPAdress);
            socket = new DatagramSocket();

            print("trying to connect");

            while (true) { //receive
                byte[] buffer = new byte[512];//packet grootte
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);
                inputHandler(receivePacket);
            }
        } catch (SocketException e) {
            print("Timeout error: " + e.getMessage());
        } catch (IOException e) {
         print("Client error: " + e.getMessage());
        }
    }

    public DatagramSocket getSocket(){ return socket;}

    public void inputHandler(DatagramPacket receivedPacketFromServer){
        DatagramPacket checkedPacket = checksum.checkingChecksum(receivedPacketFromServer);
        while(checkedPacket != null) {
            byte[] data = receivedPacketFromServer.getData();
            byte commandoByte = data[1];
            byte byteProcessID1 = data[2];
            byte byteProcessID2 = data[3];
            byte bytePacketNumber1 = data[4];
            byte bytePacketNumber2 = data[5];
            byte[] rawData = utils.removeHeader(data);
            int processID = utils.limitBytesToInteger(byteProcessID1, byteProcessID2);
            int packetNumber = utils.limitBytesToInteger(bytePacketNumber1, bytePacketNumber2);
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
                case 9:                 processManager.receiveAcknowledgementLastPacketForProcess(processID, receivedPacketFromServer);
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
        }
    }

    public void receivedFilesPI(byte[] data){
        byte[] rawData = utils.removeHeader(data);
        String filesString = utils.fromByteArrToString(rawData);
        String[] filesArr = filesString.split("//+");
        userInputHandler.setFilesPI(filesArr);
        userInputHandler.setUpdatedFilesPI(true);
    }

    public void receivedAckProcessPaused(int processID){
        print("Process " + processID + " is paused on the serverside aswell");
    }

    public void receivedAckProcessContinued(int processID){
        print("Process " + processID + " is continued on the serverside aswell");
    }

    public void receivedAckProcessStopped(int processID){
        print("Process " + processID + " is stopped on the serverside aswell");
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