package OwnCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.util.Arrays;

public class UserInputHandler implements Runnable{
    private BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

    private Client client;
    private static boolean isClient = true;

    private static String folderPath = "/Users/anouk.schoenmakers/Desktop/ClientFiles"; // where are the files placed
    private File fileFolder;
    private File[] filesClient;
    private String[] filesClientNames;
    private String[] filesPI;
    private boolean updatedFilesPI = false;

    private ProcessManager processManager;
    private PacketWithOwnHeader packetWithOwnHeader;
    private Statistics statistics;


    public UserInputHandler(Client client, ProcessManager processManager, Statistics statistics){
        this.client = client;
        this.processManager = processManager;
        this.statistics = statistics;
        packetWithOwnHeader = new PacketWithOwnHeader();

        fileFolder = new File(folderPath);
        filesClient = fileFolder.listFiles();
        filesClientNames = new String[filesClient.length];
        if(filesClient!=null){
            for(int i = 0; i < filesClient.length; i++){
                filesClientNames[i]=filesClient[i].getName();
            }
        }
    }

    @Override
    public void run() {
        print("Welkom to this network");
        startMenu();
    }

    public void startMenu(){
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
                                        somethingElse();
                                        break;
                    case "6":           processManager.printPausedProcesses();
                                        somethingElse();
                                        break;
                    case "7":           processManager.printAllProcesses();
                                        somethingElse();
                                        break;
                    case "8":           pauseSpecificProcess();
                                        break;
                    case "9":           processManager.pauseAllProcesses();
                                        somethingElse();
                                        break;
                    case "10":          continueSpecificProcess();
                                        break;
                    case "11":          processManager.continueAllProcesses();
                                        somethingElse();
                                        break;
                    case "12":          stopSpecificProcess();
                                        break;
                    case "13":          processManager.stopAllProcesses();
                                        somethingElse();
                                        break;
                    case "14":          stopProgram();
                                        break;
                    case "15":          getStatistics();
                                        break;
                    default:            print("That is not a option, try again");
                                        startMenu();
                                        break;
                }
            }

        } catch (IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void somethingElse(){
        print("Would you like to do something else? Y/N");
        try{
            if (userInput != null) {
                String thisLine = userInput.readLine();
                switch (thisLine) {
                    case "Y":               startMenu();
                                            break;
                    case "N":               stopProgram();
                                            break;
                    default:                print("That is not a Y or a N, try again");
                                            somethingElse();
                }
            }
        } catch (IOException e){
            print(e.getMessage());
        }
    }

    public void printOwnFiles(){
        print("The available files on this computer are:");
        for(int i = 0; i < filesClient.length; i++){
            print(filesClientNames[i]);
        }
        print("");
        somethingElse();
    }

    public void printPIFiles(){
        byte[] buffer = packetWithOwnHeader.commandoOne();
        DatagramPacket askFiles = new DatagramPacket(buffer, buffer.length);
        try {
            client.send(askFiles);


            while (!updatedFilesPI) {//wait till FilesPI are received & updated
                Thread.sleep(10);
            }

            print("The available files on the PI are:");

            for (int i = 0; i < filesPI.length; i++) {//last in array is empty
                print(filesPI[i]);
            }
            print("");//empty row
            updatedFilesPI = false;

            somethingElse();
        } catch(InterruptedException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void uploadFile(){
        print("These are the files you can chose:");
        for(int i = 0; i < filesClient.length; i++){
            print(filesClientNames[i]);
        }
        print("");

        print("Which file would you like to upload?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                if(Arrays.asList(filesClientNames).contains(thisLine)){
                    String filename = thisLine;
                    String pathname = folderPath + "/" + filename;
                    File file = new File(pathname);
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    int numberOfBytesToLoad = fileBytes.length;

                    processManager.createUploadProcess(file, client, isClient, numberOfBytesToLoad);

                    print("The file is being uploaded");
                    somethingElse();
                } else{
                    print("That is not a correct filename, these are the files to choose from:");
                    for(int i = 0; i < filesClient.length; i++){
                        print(filesClientNames[i]);
                    }
                    print("");
                    uploadFile();
                }
            }
        } catch (IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void downloadFile(){
        print("These are the files you can chose:");
        try {
            byte[] buffer = packetWithOwnHeader.commandoOne();
            DatagramPacket askFiles = new DatagramPacket(buffer, buffer.length);

            client.send(askFiles);

            while (!updatedFilesPI) {//wait till FilesPI are received & updated
                Thread.sleep(10);
            }

            for (int i = 0; i < filesPI.length; i++) {//last in array is empty
                print(filesPI[i]);
            }
            print("");//empty row
            updatedFilesPI = false;

            print("Which file would you like to download?");
            if (userInput != null) {
                String thisLine = userInput.readLine();
                if(Arrays.asList(filesPI).contains(thisLine)){
                    String filename = thisLine;
                    int numberOfBytesToLoad = 3000;//todo naar kijken. aan PI namen en gelijk grootte vragen??
                    processManager.createDownloadProcess(filename, folderPath, client, isClient, numberOfBytesToLoad);

                    somethingElse();
                } else{
                    print("That is not a correct filename, these are the files to choose from:");
                    printPIFiles();
                    uploadFile();
                }
            }
        } catch (IOException e){
            print("Something went wrong" + e.getMessage());
        } catch (InterruptedException e){
            print(e.getMessage());
        }
    }

    public void pauseSpecificProcess(){
        print("Which process would you like to pause?");
        try {
            if (userInput != null) {
                String thisLine = userInput.readLine();
                int userIDSugested = Integer.parseInt(thisLine);
                processManager.pauseSpecificProcess(userIDSugested);
                somethingElse();
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
                somethingElse();
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
                somethingElse();
            }
        }catch(IOException e){
            print("Something went wrong" + e.getMessage());
        }
    }

    public void stopProgram(){
        try {
            processManager.stopAllProcesses();
            print("All processes are stopped");
            client.getSocket().close();
            print("There is no more connection with the server");
            userInput.close();
            print("The programm is now fully closed");
        } catch (IOException e) {
            print("Error when closing!");
            e.printStackTrace();
        }
    }

    public void getStatistics(){
        int[] statisticsintarr = statistics.getStatistics();
        print("The number corrupted packets received is: " + statisticsintarr[0]);
        print("The highest up/download speed measured is: " + statisticsintarr[1]);
        print("The average up/download speed measured is: " + statisticsintarr[2]);
        somethingElse();
    }

    public void setFilesPI(String[] filesPI) {
        this.filesPI = filesPI;
    }

    public void setUpdatedFilesPI(boolean updated){
        updatedFilesPI = updated;
    }

    private static void print (String message){
        System.out.println(message);
    }
}

