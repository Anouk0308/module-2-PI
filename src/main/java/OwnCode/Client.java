package OwnCode;


import java.io.IOException;
import java.net.*;

public class Client implements NetworkUser, Runnable {
    private DatagramSocket socket;
    private String destinationIPAdress;
    private int destinationPort;
    private InetAddress destinationAddress;


    private Utils utils;
    private Statistics statistics;
    private Checksum checksum;
    private ProcessManager processManager;
    private UserInputHandler userInputHandler;
    private Receiver receiver;
    private SlidingWindow slidingWindow;
    private PacketWithOwnHeader packetWithOwnHeader;

    public Client(String destinationIPAdress, int destinationPort){
        this.destinationIPAdress = destinationIPAdress;
        this.destinationPort = destinationPort;
        //todo: vul FilesClient aan met de namen in daadwerkelijke map

        utils = new Utils();
        statistics = new Statistics();
        checksum = new Checksum(statistics);
        slidingWindow = new SlidingWindow();
        packetWithOwnHeader = new PacketWithOwnHeader(this);
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
            destinationAddress = InetAddress.getByName(destinationIPAdress);
            socket = new DatagramSocket();
            receiver = new Receiver(socket, slidingWindow, this);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();

            byte[] buffer = packetWithOwnHeader.commandoZero();
            DatagramPacket handshake = new DatagramPacket(buffer, buffer.length);
            send(handshake);

        } catch (SocketException e) {
            print("Timeout error: " + e.getMessage());
        } catch (UnknownHostException e){
            print("UnknownHostError" + e.getMessage());
        }
    }

    public DatagramSocket getSocket(){ return socket;}

    public void inputHandler(DatagramPacket receivedPacketFromServer){
        DatagramPacket checkedPacket = checksum.checkingChecksum(receivedPacketFromServer);
        if(checkedPacket != null){
            byte[] data = receivedPacketFromServer.getData();
            byte commandoByte = data[1];
            int processID = 0;
            byte byteProcessID1 = data[2];
            byte byteProcessID2 = data[3];
            processID = utils.limitBytesToInteger(byteProcessID1, byteProcessID2);


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
        String[] filesArr = filesString.split("\\+");
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
        int length = p.getLength();
        DatagramPacket packet = new DatagramPacket(buf, length, destinationAddress, destinationPort);

        try {
            socket.send(packet);
        } catch (IOException e) {
            print("Client error: " + e.getMessage());
        }
    }

    public ProcessManager getProcessManager(){return processManager;}

    public Statistics getStatics(){return statistics;}

    public void print (String message){
        System.out.println(message);
    }
}