package OwnCode;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Arrays;

public class Client{
    private String[] filesClient;
    private String filePath = "Macintosh HD/Users/anouk.schoenmakers/Documents/raspberry/nu-module-2/src/"; // where are the files placed
    private String[] filesPI;
    private boolean updatedFilesPI = false;

    private DatagramSocket socket;
    private String destinationIPAdress;
    private int destinationPort;
    private InetAddress destinationAddress;

    private BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

    private PacketWithOwnHeader packetWithOwnHeader;
    private Utils utils;
    private Statistics statistics;
    private Checksum checksum;
    private ProcessManager processManager;

    public Client(String destinationIPAdress, int destinationPort){
        this.destinationIPAdress = destinationIPAdress;
        this.destinationPort = destinationPort;
        //todo: vul FilesClient aan met de namen in daadwerkelijke map
        packetWithOwnHeader = new PacketWithOwnHeader();
        utils = new Utils();
        statistics = new Statistics();
        checksum = new Checksum();
        connect();
        menu();
    }

    public void connect(){
        try {
            destinationAddress = InetAddress.getByName(destinationIPAdress);
            socket = new DatagramSocket();
            processManager = new ProcessManager(socket, destinationPort, destinationAddress);

            while (true) { //receive

                //TODO: kijken of handshake nodig is
               /* DatagramPacket request = new DatagramPacket(new byte[1], 1, address, port);
                socket.send(request);
                */

                byte[] buffer = new byte[512];//packet grootte
                DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(receivePacket);
                serverInputHandler(receivePacket);
            }
        } catch (SocketException e) {
            print("Timeout error: " + e.getMessage());
        } catch (IOException e) {
         print("Client error: " + e.getMessage());
        }
    }



    public DatagramSocket getSocket(){ return socket;}


    public void menu(){
        print("Welkom to this network");
        print("What would you like to do?");
        print("1. Get the filelist from this computer");
        print("2. Get the filelist from the PI");
        print("3. Upload a file to the PI");
        print("4. Download a file from the PI");
        print("5. See all running processes");
        print("6. See all paused processes");
        print("7. See all processes");
        print("8. Pause a specific process");
        print("9. Pause all processes");
        print("10. Continue a specific process");
        print("11. Continue all processes");
        print("12. Stop a specific process");
        print("13. Stop all processes");
        print("14. Close this program");
        userInputHandler();
    }

    public void userInputHandler(){
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                switch (thisLine){
                    case "1":           printOwnFiles();
                                        break;
                    case "2":           printPIFiles();
                                        break;
                    case "3":           uploadFile();
                                        break;
                    case "4":           downloadFile();
                                        break;
                    case "5":           processManager.printRunningProcesses();
                                        break;
                    case "6":           processManager.printPausedProcesses();
                                        break;
                    case "7":           processManager.printAllProcesses();
                                        break;
                    case "8":           pauseSpecificProcess();
                                        break;
                    case "9":           processManager.pauseAllProcesses();
                                        break;
                    case "10":          continueSpecificProcess();
                                        break;
                    case "11":          processManager.continueAllProcesses();
                                        break;
                    case "12":          stopSpecificProcess();
                                        break;
                    case "13":          processManager.stopAllProcesses();
                                        break;
                    case "14":          stopProgram();
                                        break;
                    case "15":          getStatistics();
                                        break;
                    default:            menu();
                                        break;
                }
            }

        } catch (IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void printOwnFiles(){
        for(int i = 0; i < filesClient.length; i++){
            print(filesClient[i]);
        }
    }

    public void printPIFiles(){
        byte[] buffer = packetWithOwnHeader.commandoOne();
        DatagramPacket askFiles = new DatagramPacket(buffer, buffer.length);
        try {
            send(askFiles);

            while (!updatedFilesPI) {//wait till FilesPI are received & updated
                Thread.sleep(10);
            }

            for (int i = 0; i < filesPI.length; i++) {
                print(filesPI[i]);
            }
            updatedFilesPI = false;

            print("Would you like to do something else?");
            menu(); //todo: kijken of dit niet mooier kan, dus bv met yes/no. yes = menu, no = close program

        } catch(InterruptedException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void uploadFile(){
        print("Which file would you like to upload?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                if(Arrays.asList(filesClient).contains(thisLine)){
                    String filename = thisLine;
                    String pathname = filePath + filename;//todo:kijken of dit zo werkt?
                    File file = new File(pathname);
                    processManager.createUploadProcess(file, this);
                } else{
                    print("That is not a correct filename, these are the files to choose from:");
                    printOwnFiles();
                    uploadFile();
                }
            }
        } catch (IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void downloadFile(){
        print("Which file would you like to download?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                if(Arrays.asList(filesClient).contains(thisLine)){
                    String filename = thisLine;
                    processManager.createDownloadProcess(filename, filePath, this);
                } else{
                    print("That is not a correct filename, these are the files to choose from:");
                    printPIFiles();
                    uploadFile();
                }
            }
        } catch (IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void pauseSpecificProcess(){
        print("Which process would you like to pause?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                int userIDSugested = Integer.parseInt(thisLine);
                processManager.pauseSpecificProcess(userIDSugested);
            }
        } catch(IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void continueSpecificProcess() {
        print("Which process would you like to continue?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                int userIDSugested = Integer.parseInt(thisLine);
                processManager.continueSpecificProcess(userIDSugested);
            }
        }catch(IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void stopSpecificProcess() {
        print("Which process would you like to continue?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                int userIDSugested = Integer.parseInt(thisLine);
                processManager.stopSpecificProcess(userIDSugested);
            }
        }catch(IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

   public void stopProgram(){
       try {
           processManager.stopAllProcesses();
           socket.close();
           print("There is no more connection with the server");
           userInput.close();
           print("The programm is now fully closed");
       } catch (IOException e) {
           print("Error when closing!");
           e.printStackTrace();
       }
    }
   public void getStatistics(){
        statistics.getStatistics();
   }

    public void serverInputHandler(DatagramPacket receivedPacket){
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
                case 2:                 receivedFilesPI(data);
                                        break;
                case 5:                 processManager.receiveUploadAcknowledgement(processID);
                                        break;
                case 6:                 processManager.receivePacketForProcess(processID, receivedPacket);
                                        break;
                case 7:                 processManager.receiveAcknowledgementPacketForProcess(processID, receivedPacket);
                                        break;
                case 8:                 processManager.receiveLastPacketForProcess(processID, receivedPacket);
                                        break;
                case 9:                 processManager.receiveAcknowledgementLastPacketForProcess(processID, receivedPacket);
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
        filesPI = filesArr;
        updatedFilesPI = true;
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

    private static void print (String message){
        System.out.println(message);
    }
}